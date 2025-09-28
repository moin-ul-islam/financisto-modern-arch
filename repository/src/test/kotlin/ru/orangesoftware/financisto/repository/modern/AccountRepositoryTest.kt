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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.orangesoftware.financisto.data.dao.AccountDao
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.repository.CoroutineTestRule
import ru.orangesoftware.financisto.repository.TestConstants
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Sample test class demonstrating MockK + Strikt setup for AccountRepository testing.
 * 
 * This test class shows:
 * - MockK setup for mocking DAOs
 * - Strikt assertions for expressive testing
 * - Coroutines testing with test dispatcher
 * - Flow testing patterns
 * - Repository testing best practices
 * 
 * NOTE: This is a sample/template - actual test implementations are not included yet.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    // MockK mocks
    private lateinit var mockAccountDao: AccountDao
    private lateinit var repository: AccountRepositoryImpl

    @Before
    fun setUp() {
        // Setup MockK mocks
        mockAccountDao = mockk()
        
        // Create repository instance with mocked dependencies
        repository = AccountRepositoryImpl(
            accountDao = mockAccountDao,
            ioDispatcher = coroutineTestRule.testDispatcher
        )
    }

    @Test
    fun `getAllAccounts - should return accounts from DAO`() = runTest {
        // Arrange
        val mockAccounts = listOf(
            AccountEntity(
                id = TestConstants.ACCOUNT_ID_1,
                title = "Checking Account",
                type = "BANK",
                currencyId = TestConstants.CURRENCY_ID_USD,
                totalAmount = 50000L, // $500.00
                isActive = true,
                isIncludeIntoTotals = true
            ),
            AccountEntity(
                id = TestConstants.ACCOUNT_ID_2,
                title = "Savings Account", 
                type = "BANK",
                currencyId = TestConstants.CURRENCY_ID_USD,
                totalAmount = 100000L, // $1000.00
                isActive = true,
                isIncludeIntoTotals = true
            )
        )
        
        coEvery { mockAccountDao.getAllAccounts() } returns mockAccounts
        
        // Act
        val result = repository.getAllAccounts()
        
        // Assert: Repository correctly delegates to DAO and returns the same data
        expectThat(result) {
            hasSize(2)
            get { first().id }.isEqualTo(TestConstants.ACCOUNT_ID_1)
            get { first().title }.isEqualTo("Checking Account")
            get { first().type }.isEqualTo("BANK")
            get { last().id }.isEqualTo(TestConstants.ACCOUNT_ID_2)
            get { last().totalAmount }.isEqualTo(100000L)
        }
        
        // Verify: Repository calls DAO exactly once with no parameters
        coVerify(exactly = 1) { mockAccountDao.getAllAccounts() }
    }

    @Test
    fun `getAllAccountsFlow - should return Flow from DAO`() = runTest {
        // Arrange
        val mockAccounts = listOf(
            AccountEntity(
                id = TestConstants.ACCOUNT_ID_1,
                title = "Cash Wallet",
                type = "CASH",
                currencyId = TestConstants.CURRENCY_ID_USD,
                totalAmount = 25000L, // $250.00
                isActive = true,
                isIncludeIntoTotals = true
            )
        )
        val mockFlow = flowOf(mockAccounts)
        
        coEvery { mockAccountDao.getAllAccountsFlow() } returns mockFlow
        
        // Act
        val result = repository.getAllAccountsFlow().toList()
        
        // Assert: Repository correctly exposes DAO Flow and applies IO dispatcher
        expectThat(result) {
            hasSize(1) // One flow emission
            first().hasSize(1) // One account in the emission
            first().first().get { title }.isEqualTo("Cash Wallet")
            first().first().get { type }.isEqualTo("CASH")
        }
        
        // Verify: Repository gets Flow from DAO exactly once
        coVerify(exactly = 1) { mockAccountDao.getAllAccountsFlow() }
    }

    @Test
    fun `getAccountById - should return account when found`() = runTest {
        // Arrange
        val expectedAccount = AccountEntity(
            id = TestConstants.ACCOUNT_ID_1,
            title = "Credit Card",
            type = "CREDIT_CARD",
            currencyId = TestConstants.CURRENCY_ID_USD,
            totalAmount = -15000L, // -$150.00 debt
            limitAmount = 200000L, // $2000.00 limit
            isActive = true,
            isIncludeIntoTotals = true
        )
        
        coEvery { mockAccountDao.getAccountById(TestConstants.ACCOUNT_ID_1) } returns expectedAccount
        
        // Act
        val result = repository.getAccountById(TestConstants.ACCOUNT_ID_1)
        
        // Assert: Repository returns the exact account from DAO when found
        expectThat(result).isNotNull().and {
            get { id }.isEqualTo(TestConstants.ACCOUNT_ID_1)
            get { title }.isEqualTo("Credit Card")
            get { type }.isEqualTo("CREDIT_CARD")
            get { totalAmount }.isEqualTo(-15000L)
            get { limitAmount }.isEqualTo(200000L)
        }
        
        // Verify: Repository passes the correct ID to DAO
        coVerify(exactly = 1) { mockAccountDao.getAccountById(TestConstants.ACCOUNT_ID_1) }
    }

    @Test
    fun `getAccountById - should return null when not found`() = runTest {
        // Arrange
        val nonExistentId = 999L
        coEvery { mockAccountDao.getAccountById(nonExistentId) } returns null
        
        // Act
        val result = repository.getAccountById(nonExistentId)
        
        // Assert: Repository correctly propagates null from DAO
        expectThat(result).isNull()
        
        // Verify: Repository attempts to find account with correct ID
        coVerify(exactly = 1) { mockAccountDao.getAccountById(nonExistentId) }
    }

    @Test
    fun `insertAccount - should return generated ID on success`() = runTest {
        // Arrange
        val newAccount = AccountEntity(
            id = 0, // Auto-generated
            title = "New Investment Account",
            type = "INVESTMENT",
            currencyId = TestConstants.CURRENCY_ID_USD,
            totalAmount = 0L,
            isActive = true,
            isIncludeIntoTotals = false
        )
        val generatedId = 42L
        
        coEvery { mockAccountDao.insertAccount(newAccount) } returns generatedId
        
        // Act
        val result = repository.insertAccount(newAccount)
        
        // Assert: Repository returns the generated ID from DAO
        expectThat(result).isEqualTo(generatedId)
        
        // Verify: Repository passes the exact account entity to DAO
        coVerify(exactly = 1) { mockAccountDao.insertAccount(newAccount) }
    }

    @Test
    fun `updateAccount - should return true on successful update`() = runTest {
        // Arrange
        val updatedAccount = AccountEntity(
            id = TestConstants.ACCOUNT_ID_1,
            title = "Updated Account Name",
            type = "BANK",
            currencyId = TestConstants.CURRENCY_ID_USD,
            totalAmount = 75000L,
            isActive = true,
            isIncludeIntoTotals = true
        )
        
        // DAO update methods return void, so we just mock successful execution
        coEvery { mockAccountDao.updateAccount(updatedAccount) } just Runs
        
        // Act
        val result = repository.updateAccount(updatedAccount)
        
        // Assert: Repository returns true when DAO operation succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository passes the exact updated account to DAO
        coVerify(exactly = 1) { mockAccountDao.updateAccount(updatedAccount) }
    }

    @Test
    fun `updateAccount - should return false when DAO throws exception`() = runTest {
        // Arrange
        val problematicAccount = AccountEntity(
            id = TestConstants.ACCOUNT_ID_1,
            title = "Account With Issues",
            type = "BANK",
            currencyId = TestConstants.CURRENCY_ID_USD,
            totalAmount = 0L,
            isActive = true,
            isIncludeIntoTotals = true
        )
        
        coEvery { mockAccountDao.updateAccount(problematicAccount) } throws RuntimeException("Database constraint violation")
        
        // Act
        val result = repository.updateAccount(problematicAccount)
        
        // Assert: Repository catches exception and returns false (doesn't crash app)
        expectThat(result).isFalse()
        
        // Verify: Repository attempted the update despite the failure
        coVerify(exactly = 1) { mockAccountDao.updateAccount(problematicAccount) }
    }

    @Test
    fun `deleteAccount - should return true on successful deletion`() = runTest {
        // Arrange
        val accountIdToDelete = TestConstants.ACCOUNT_ID_1
        coEvery { mockAccountDao.deleteAccountById(accountIdToDelete) } just Runs
        
        // Act
        val result = repository.deleteAccount(accountIdToDelete)
        
        // Assert: Repository returns true when DAO deletion succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO with the correct account ID
        coVerify(exactly = 1) { mockAccountDao.deleteAccountById(accountIdToDelete) }
    }

    @Test
    fun `deleteAccount - should return false when DAO throws exception`() = runTest {
        // Arrange
        val accountIdToDelete = TestConstants.ACCOUNT_ID_1
        coEvery { mockAccountDao.deleteAccountById(accountIdToDelete) } throws RuntimeException("Foreign key constraint")
        
        // Act
        val result = repository.deleteAccount(accountIdToDelete)
        
        // Assert: Repository handles deletion failure gracefully
        expectThat(result).isFalse()
        
        // Verify: Repository attempted deletion with correct ID
        coVerify(exactly = 1) { mockAccountDao.deleteAccountById(accountIdToDelete) }
    }

    @Test
    fun `getAccountsIncludedInTotals - should return only accounts included in totals`() = runTest {
        // Arrange
        val includedAccounts = listOf(
            AccountEntity(
                id = TestConstants.ACCOUNT_ID_1,
                title = "Checking (Included)",
                type = "BANK",
                currencyId = TestConstants.CURRENCY_ID_USD,
                totalAmount = 100000L,
                isActive = true,
                isIncludeIntoTotals = true
            ),
            AccountEntity(
                id = TestConstants.ACCOUNT_ID_2,
                title = "Savings (Included)",
                type = "BANK",
                currencyId = TestConstants.CURRENCY_ID_USD,
                totalAmount = 200000L,
                isActive = true,
                isIncludeIntoTotals = true
            )
        )
        
        coEvery { mockAccountDao.getAccountsIncludedInTotals() } returns includedAccounts
        
        // Act
        val result = repository.getAccountsIncludedInTotals()
        
        // Assert: Repository returns filtered accounts from DAO
        expectThat(result) {
            hasSize(2)
            all { get { isIncludeIntoTotals }.isTrue() }
            all { get { isActive }.isTrue() }
        }
        
        // Verify: Repository delegates to specialized DAO method
        coVerify(exactly = 1) { mockAccountDao.getAccountsIncludedInTotals() }
    }

    @Test
    fun `searchAccounts - should return matching accounts from DAO`() = runTest {
        // Arrange
        val searchQuery = "checking"
        val matchingAccounts = listOf(
            AccountEntity(
                id = TestConstants.ACCOUNT_ID_1,
                title = "Personal Checking",
                type = "BANK",
                currencyId = TestConstants.CURRENCY_ID_USD,
                totalAmount = 50000L,
                isActive = true,
                isIncludeIntoTotals = true
            ),
            AccountEntity(
                id = TestConstants.ACCOUNT_ID_2,
                title = "Business Checking",
                type = "BANK",
                currencyId = TestConstants.CURRENCY_ID_USD,
                totalAmount = 75000L,
                isActive = true,
                isIncludeIntoTotals = true
            )
        )
        
        coEvery { mockAccountDao.searchAccounts(searchQuery) } returns matchingAccounts
        
        // Act
        val result = repository.searchAccounts(searchQuery)
        
        // Assert: Repository returns search results from DAO
        expectThat(result) {
            hasSize(2)
            all { get { title }.contains("Checking") }
        }
        
        // Verify: Repository passes exact search query to DAO
        coVerify(exactly = 1) { mockAccountDao.searchAccounts(searchQuery) }
    }

    @Test
    fun `updateAccountBalance - should call DAO with correct parameters`() = runTest {
        // Arrange
        val accountId = TestConstants.ACCOUNT_ID_1
        val newBalance = 125000L // $1,250.00
        val transactionDate = System.currentTimeMillis()
        
        coEvery { mockAccountDao.updateAccountBalance(accountId, newBalance, transactionDate) } just Runs
        
        // Act
        repository.updateAccountBalance(accountId, newBalance, transactionDate)
        
        // Assert: No return value to check, verify interaction occurred
        // Verify: Repository passes all parameters correctly to DAO
        coVerify(exactly = 1) { 
            mockAccountDao.updateAccountBalance(accountId, newBalance, transactionDate) 
        }
    }
}