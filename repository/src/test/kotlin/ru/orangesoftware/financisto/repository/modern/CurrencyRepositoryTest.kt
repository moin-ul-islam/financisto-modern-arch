package ru.orangesoftware.financisto.repository.modern

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.orangesoftware.financisto.data.dao.CurrencyDao
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import ru.orangesoftware.financisto.repository.CoroutineTestRule
import ru.orangesoftware.financisto.repository.TestConstants
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests for CurrencyRepositoryImpl.
 * 
 * These tests verify that the repository correctly:
 * 1. Delegates operations to the CurrencyDao
 * 2. Handles both success and error scenarios gracefully
 * 3. Applies proper IO dispatcher context switching
 * 4. Provides reactive data streams via Flow
 * 5. Implements currency-specific business logic (default currency, existence checks)
 * 
 * The repository acts as a bridge between use cases and data access,
 * focusing on context switching, error handling, and simple business logic
 * transformations rather than complex business rules.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    // MockK mocks
    private lateinit var mockCurrencyDao: CurrencyDao
    private lateinit var repository: CurrencyRepositoryImpl

    @Before
    fun setUp() {
        // Setup MockK mocks
        mockCurrencyDao = mockk()
        
        // Create repository instance with mocked dependencies
        repository = CurrencyRepositoryImpl(
            currencyDao = mockCurrencyDao,
            ioDispatcher = coroutineTestRule.testDispatcher
        )
    }

    @Test
    fun `getAllCurrencies - should return currencies from DAO`() = runTest {
        // Arrange
        val mockCurrencies = listOf(
            CurrencyEntity(
                id = TestConstants.CURRENCY_ID_USD,
                name = "USD",
                title = "US Dollar",
                symbol = "$",
                isDefault = true,
                decimals = 2,
                decimalSeparator = ".",
                groupSeparator = ",",
                symbolFormat = "RS"
            ),
            CurrencyEntity(
                id = TestConstants.CURRENCY_ID_EUR,
                name = "EUR",
                title = "Euro",
                symbol = "€",
                isDefault = false,
                decimals = 2,
                decimalSeparator = ".",
                groupSeparator = ",",
                symbolFormat = "LS"
            )
        )
        
        coEvery { mockCurrencyDao.getAllCurrencies() } returns mockCurrencies
        
        // Act
        val result = repository.getAllCurrencies()
        
        // Assert: Repository correctly delegates to DAO and returns the same data
        expectThat(result) {
            hasSize(2)
            get { first().id }.isEqualTo(TestConstants.CURRENCY_ID_USD)
            get { first().name }.isEqualTo("USD")
            get { first().symbol }.isEqualTo("$")
            get { first().isDefault }.isTrue()
            get { last().id }.isEqualTo(TestConstants.CURRENCY_ID_EUR)
            get { last().name }.isEqualTo("EUR")
            get { last().symbol }.isEqualTo("€")
            get { last().isDefault }.isFalse()
        }
        
        // Verify: Repository calls DAO exactly once with no parameters
        coVerify(exactly = 1) { mockCurrencyDao.getAllCurrencies() }
    }

    @Test
    fun `getAllCurrenciesFlow - should return Flow from DAO`() = runTest {
        // Arrange
        val mockCurrencies = listOf(
            CurrencyEntity(
                id = TestConstants.CURRENCY_ID_USD,
                name = "GBP",
                title = "British Pound",
                symbol = "£",
                isDefault = false,
                decimals = 2,
                decimalSeparator = ".",
                groupSeparator = ",",
                symbolFormat = "RS"
            )
        )
        val mockFlow = flowOf(mockCurrencies)
        
        coEvery { mockCurrencyDao.getAllCurrenciesFlow() } returns mockFlow
        
        // Act
        val result = repository.getAllCurrenciesFlow().toList()
        
        // Assert: Repository correctly exposes DAO Flow and applies IO dispatcher
        expectThat(result) {
            hasSize(1) // One flow emission
            first().hasSize(1) // One currency in the emission
            first().first().get { name }.isEqualTo("GBP")
            first().first().get { symbol }.isEqualTo("£")
        }
        
        // Verify: Repository gets Flow from DAO exactly once
        coVerify(exactly = 1) { mockCurrencyDao.getAllCurrenciesFlow() }
    }

    @Test
    fun `getCurrencyById - should return currency when found`() = runTest {
        // Arrange
        val expectedCurrency = CurrencyEntity(
            id = TestConstants.CURRENCY_ID_USD,
            name = "CAD",
            title = "Canadian Dollar",
            symbol = "C$",
            isDefault = false,
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "RSP"
        )
        
        coEvery { mockCurrencyDao.getCurrencyById(TestConstants.CURRENCY_ID_USD) } returns expectedCurrency
        
        // Act
        val result = repository.getCurrencyById(TestConstants.CURRENCY_ID_USD)
        if (result == null) {
            fail("Result is null")
            return@runTest
        }
        // Assert: Repository returns the exact currency from DAO when found
        expectThat(result) {
            get { id }.isEqualTo(TestConstants.CURRENCY_ID_USD)
            get { name }.isEqualTo("CAD")
            get { title }.isEqualTo("Canadian Dollar")
            get { symbol }.isEqualTo("C$")
            get { symbolFormat }.isEqualTo("RSP")
        }
        
        // Verify: Repository passes the correct ID to DAO
        coVerify(exactly = 1) { mockCurrencyDao.getCurrencyById(TestConstants.CURRENCY_ID_USD) }
    }

    @Test
    fun `getCurrencyById - should return null when not found`() = runTest {
        // Arrange
        val nonExistentId = 999L
        coEvery { mockCurrencyDao.getCurrencyById(nonExistentId) } returns null
        
        // Act
        val result = repository.getCurrencyById(nonExistentId)
        
        // Assert: Repository correctly propagates null from DAO
        expectThat(result).isNull()
        
        // Verify: Repository attempts to find currency with correct ID
        coVerify(exactly = 1) { mockCurrencyDao.getCurrencyById(nonExistentId) }
    }

    @Test
    fun `getDefaultCurrency - should return default currency from DAO`() = runTest {
        // Arrange
        val defaultCurrency = CurrencyEntity(
            id = TestConstants.CURRENCY_ID_USD,
            name = "USD",
            title = "US Dollar",
            symbol = "$",
            isDefault = true,
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "RS"
        )
        
        coEvery { mockCurrencyDao.getDefaultCurrency() } returns defaultCurrency
        
        // Act
        val result = repository.getDefaultCurrency()
        if (result == null) {
            fail("Result is null")
            return@runTest
        }
        // Assert: Repository returns the default currency from DAO
        expectThat(result) {
            get { isDefault }.isTrue()
            get { name }.isEqualTo("USD")
            get { symbol }.isEqualTo("$")
        }
        
        // Verify: Repository delegates to DAO default currency method
        coVerify(exactly = 1) { mockCurrencyDao.getDefaultCurrency() }
    }

    @Test
    fun `getDefaultCurrency - should return null when no default currency exists`() = runTest {
        // Arrange
        coEvery { mockCurrencyDao.getDefaultCurrency() } returns null
        
        // Act
        val result = repository.getDefaultCurrency()
        
        // Assert: Repository correctly handles case when no default currency is set
        expectThat(result).isNull()
        
        // Verify: Repository calls DAO method
        coVerify(exactly = 1) { mockCurrencyDao.getDefaultCurrency() }
    }

    @Test
    fun `insertCurrency - should return generated ID on success`() = runTest {
        // Arrange
        val newCurrency = CurrencyEntity(
            id = 0, // Auto-generated
            name = "JPY",
            title = "Japanese Yen",
            symbol = "¥",
            isDefault = false,
            decimals = 0, // Yen has no decimals
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "RS"
        )
        val generatedId = 42L
        
        coEvery { mockCurrencyDao.insertCurrency(newCurrency) } returns generatedId
        
        // Act
        val result = repository.insertCurrency(newCurrency)
        
        // Assert: Repository returns the generated ID from DAO
        expectThat(result).isEqualTo(generatedId)
        
        // Verify: Repository passes the exact currency entity to DAO
        coVerify(exactly = 1) { mockCurrencyDao.insertCurrency(newCurrency) }
    }

    @Test
    fun `updateCurrency - should return true on successful update`() = runTest {
        // Arrange
        val updatedCurrency = CurrencyEntity(
            id = TestConstants.CURRENCY_ID_USD,
            name = "USD",
            title = "United States Dollar",
            symbol = "$",
            isDefault = true,
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "RS"
        )
        
        // DAO update methods return void, so we just mock successful execution
        coEvery { mockCurrencyDao.updateCurrency(updatedCurrency) } just Runs
        
        // Act
        val result = repository.updateCurrency(updatedCurrency)
        
        // Assert: Repository returns true when DAO operation succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository passes the exact updated currency to DAO
        coVerify(exactly = 1) { mockCurrencyDao.updateCurrency(updatedCurrency) }
    }

    @Test
    fun `updateCurrency - should return false when DAO throws exception`() = runTest {
        // Arrange
        val problematicCurrency = CurrencyEntity(
            id = TestConstants.CURRENCY_ID_USD,
            name = "USD",
            title = "US Dollar",
            symbol = "$",
            isDefault = true,
            decimals = -1, // Invalid decimal count
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "INVALID"
        )
        
        coEvery { mockCurrencyDao.updateCurrency(problematicCurrency) } throws RuntimeException("Invalid currency data")
        
        // Act
        val result = repository.updateCurrency(problematicCurrency)
        
        // Assert: Repository catches exception and returns false (doesn't crash app)
        expectThat(result).isFalse()
        
        // Verify: Repository attempted the update despite the failure
        coVerify(exactly = 1) { mockCurrencyDao.updateCurrency(problematicCurrency) }
    }

    @Test
    fun `deleteCurrency - should return true on successful deletion`() = runTest {
        // Arrange
        val currencyToDelete = CurrencyEntity(
            id = TestConstants.CURRENCY_ID_EUR,
            name = "EUR",
            title = "Euro",
            symbol = "€",
            isDefault = false,
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "LS"
        )
        
        coEvery { mockCurrencyDao.deleteCurrency(currencyToDelete) } just Runs
        
        // Act
        val result = repository.deleteCurrency(currencyToDelete)
        
        // Assert: Repository returns true when DAO deletion succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO with the exact currency entity
        coVerify(exactly = 1) { mockCurrencyDao.deleteCurrency(currencyToDelete) }
    }

    @Test
    fun `deleteCurrency - should return false when DAO throws exception`() = runTest {
        // Arrange
        val currencyToDelete = CurrencyEntity(
            id = TestConstants.CURRENCY_ID_USD,
            name = "USD",
            title = "US Dollar",
            symbol = "$",
            isDefault = true, // Default currency - should not be deletable
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "RS"
        )
        
        coEvery { mockCurrencyDao.deleteCurrency(currencyToDelete) } throws RuntimeException("Cannot delete default currency")
        
        // Act
        val result = repository.deleteCurrency(currencyToDelete)
        
        // Assert: Repository handles deletion failure gracefully
        expectThat(result).isFalse()
        
        // Verify: Repository attempted deletion with correct currency
        coVerify(exactly = 1) { mockCurrencyDao.deleteCurrency(currencyToDelete) }
    }

    @Test
    fun `currencyExistsByName - should return true when currency exists`() = runTest {
        // Arrange
        val currencyName = "USD"
        val existingCount = 1
        
        coEvery { mockCurrencyDao.currencyExistsByName(currencyName) } returns existingCount
        
        // Act
        val result = repository.currencyExistsByName(currencyName)
        
        // Assert: Repository implements business logic (count > 0 = exists)
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO with correct currency name
        coVerify(exactly = 1) { mockCurrencyDao.currencyExistsByName(currencyName) }
    }

    @Test
    fun `currencyExistsByName - should return false when currency does not exist`() = runTest {
        // Arrange
        val currencyName = "XYZ"
        val existingCount = 0
        
        coEvery { mockCurrencyDao.currencyExistsByName(currencyName) } returns existingCount
        
        // Act
        val result = repository.currencyExistsByName(currencyName)
        
        // Assert: Repository correctly identifies non-existing currencies
        expectThat(result).isFalse()
        
        // Verify: Repository checks existence with correct name
        coVerify(exactly = 1) { mockCurrencyDao.currencyExistsByName(currencyName) }
    }

    @Test
    fun `currencyExistsByName - should handle case-sensitive names correctly`() = runTest {
        // Arrange
        val upperCaseName = "USD"
        val lowerCaseName = "usd"
        
        coEvery { mockCurrencyDao.currencyExistsByName(upperCaseName) } returns 1
        coEvery { mockCurrencyDao.currencyExistsByName(lowerCaseName) } returns 0
        
        // Act
        val upperCaseResult = repository.currencyExistsByName(upperCaseName)
        val lowerCaseResult = repository.currencyExistsByName(lowerCaseName)
        
        // Assert: Repository delegates exact case handling to DAO
        expectThat(upperCaseResult).isTrue()
        expectThat(lowerCaseResult).isFalse()
        
        // Verify: Repository calls DAO with exact names provided
        coVerify(exactly = 1) { mockCurrencyDao.currencyExistsByName(upperCaseName) }
        coVerify(exactly = 1) { mockCurrencyDao.currencyExistsByName(lowerCaseName) }
    }

    @Test
    fun `getCurrenciesCount - should return count from DAO`() = runTest {
        // Arrange
        val expectedCount = 15
        
        coEvery { mockCurrencyDao.getCurrenciesCount() } returns expectedCount
        
        // Act
        val result = repository.getCurrenciesCount()
        
        // Assert: Repository returns the count from DAO
        expectThat(result).isEqualTo(expectedCount)
        
        // Verify: Repository delegates to DAO count method
        coVerify(exactly = 1) { mockCurrencyDao.getCurrenciesCount() }
    }

    @Test
    fun `getCurrenciesCount - should return zero when no currencies exist`() = runTest {
        // Arrange
        val emptyCount = 0
        
        coEvery { mockCurrencyDao.getCurrenciesCount() } returns emptyCount
        
        // Act
        val result = repository.getCurrenciesCount()
        
        // Assert: Repository correctly handles empty currency table
        expectThat(result).isEqualTo(emptyCount)
        
        // Verify: Repository calls count method
        coVerify(exactly = 1) { mockCurrencyDao.getCurrenciesCount() }
    }

    // ========== Business Logic Edge Cases ==========

    @Test
    fun `insertCurrency - should handle multiple default currencies scenario`() = runTest {
        // Arrange: Simulating a scenario where multiple default currencies exist
        val newDefaultCurrency = CurrencyEntity(
            id = 0,
            name = "EUR",
            title = "Euro",
            symbol = "€",
            isDefault = true, // Making this the new default
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "LS"
        )
        val generatedId = 3L
        
        coEvery { mockCurrencyDao.insertCurrency(newDefaultCurrency) } returns generatedId
        
        // Act
        val result = repository.insertCurrency(newDefaultCurrency)
        
        // Assert: Repository doesn't handle default currency business logic - delegates to use case layer
        expectThat(result).isEqualTo(generatedId)
        
        // Verify: Repository simply delegates to DAO without additional logic
        coVerify(exactly = 1) { mockCurrencyDao.insertCurrency(newDefaultCurrency) }
    }

    @Test
    fun `updateCurrency - should handle symbol format validation at DAO level`() = runTest {
        // Arrange
        val currencyWithInvalidFormat = CurrencyEntity(
            id = TestConstants.CURRENCY_ID_USD,
            name = "USD",
            title = "US Dollar",
            symbol = "$",
            isDefault = true,
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "INVALID_FORMAT"
        )
        
        coEvery { mockCurrencyDao.updateCurrency(currencyWithInvalidFormat) } throws RuntimeException("Invalid symbol format")
        
        // Act
        val result = repository.updateCurrency(currencyWithInvalidFormat)
        
        // Assert: Repository catches DAO-level validation errors and returns false
        expectThat(result).isFalse()
        
        // Verify: Repository attempts update and handles DAO validation failure
        coVerify(exactly = 1) { mockCurrencyDao.updateCurrency(currencyWithInvalidFormat) }
    }

    @Test
    fun `deleteCurrency - should handle foreign key constraints gracefully`() = runTest {
        // Arrange: Currency that is referenced by accounts
        val currencyUsedByAccounts = CurrencyEntity(
            id = TestConstants.CURRENCY_ID_USD,
            name = "USD",
            title = "US Dollar",
            symbol = "$",
            isDefault = false,
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "RS"
        )
        
        coEvery { mockCurrencyDao.deleteCurrency(currencyUsedByAccounts) } throws RuntimeException("Foreign key constraint violation")
        
        // Act
        val result = repository.deleteCurrency(currencyUsedByAccounts)
        
        // Assert: Repository handles foreign key constraint violations gracefully
        expectThat(result).isFalse()
        
        // Verify: Repository attempted deletion despite constraint violation
        coVerify(exactly = 1) { mockCurrencyDao.deleteCurrency(currencyUsedByAccounts) }
    }
}