package ru.orangesoftware.financisto.usecase.modern

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests for GetHomeCurrencyUseCase.
 * 
 * Tests the retrieval of the home (default) currency.
 */
class GetHomeCurrencyUseCaseTest {

    private lateinit var getCurrenciesUseCase: GetCurrenciesUseCase
    private lateinit var useCase: GetHomeCurrencyUseCase

    @Before
    fun setup() {
        getCurrenciesUseCase = mockk()
        useCase = GetHomeCurrencyUseCase(getCurrenciesUseCase)
    }

    @Test
    fun `execute returns home currency when one exists`() = runTest {
        // Given
        val usdCurrency = createCurrency(1, "USD", "$", isDefault = false)
        val eurCurrency = createCurrency(2, "EUR", "€", isDefault = true)
        val gbpCurrency = createCurrency(3, "GBP", "£", isDefault = false)
        
        coEvery { getCurrenciesUseCase.execute() } returns listOf(usdCurrency, eurCurrency, gbpCurrency)

        // When
        val result = useCase.execute()

        // Then
        expectThat(result).isNotNull().and {
            get { id }.isEqualTo(2L)
            get { name }.isEqualTo("EUR")
            get { isDefault }.isTrue()
        }
    }

    @Test
    fun `execute returns null when no home currency exists`() = runTest {
        // Given
        val usdCurrency = createCurrency(1, "USD", "$", isDefault = false)
        val eurCurrency = createCurrency(2, "EUR", "€", isDefault = false)
        
        coEvery { getCurrenciesUseCase.execute() } returns listOf(usdCurrency, eurCurrency)

        // When
        val result = useCase.execute()

        // Then
        expectThat(result).isNull()
    }

    @Test
    fun `execute returns null when no currencies exist`() = runTest {
        // Given
        coEvery { getCurrenciesUseCase.execute() } returns emptyList()

        // When
        val result = useCase.execute()

        // Then
        expectThat(result).isNull()
    }

    @Test
    fun `observe returns home currency flow when one exists`() = runTest {
        // Given
        val usdCurrency = createCurrency(1, "USD", "$", isDefault = false)
        val eurCurrency = createCurrency(2, "EUR", "€", isDefault = true)
        
        every { getCurrenciesUseCase.executeAsFlow() } returns flowOf(listOf(usdCurrency, eurCurrency))

        // When
        val result = useCase.observe().first()

        // Then
        expectThat(result).isNotNull().and {
            get { id }.isEqualTo(2L)
            get { isDefault }.isTrue()
        }
    }

    @Test
    fun `observe returns null when no home currency exists`() = runTest {
        // Given
        val usdCurrency = createCurrency(1, "USD", "$", isDefault = false)
        
        every { getCurrenciesUseCase.executeAsFlow() } returns flowOf(listOf(usdCurrency))

        // When
        val result = useCase.observe().first()

        // Then
        expectThat(result).isNull()
    }

    private fun createCurrency(
        id: Long,
        name: String,
        symbol: String,
        isDefault: Boolean = false
    ) = CurrencyEntity(
        id = id,
        name = name,
        title = name,
        symbol = symbol,
        isDefault = isDefault,
        decimals = 2,
        decimalSeparator = ".",
        groupSeparator = ",",
        symbolFormat = "RS"
    )
}
