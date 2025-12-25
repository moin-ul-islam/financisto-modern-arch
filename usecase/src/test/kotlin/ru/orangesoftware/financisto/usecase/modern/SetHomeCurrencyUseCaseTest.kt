package ru.orangesoftware.financisto.usecase.modern

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests for SetHomeCurrencyUseCase.
 * 
 * Tests setting a currency as the home currency.
 */
class SetHomeCurrencyUseCaseTest {

    private lateinit var getCurrencyByIdUseCase: GetCurrencyByIdUseCase
    private lateinit var updateCurrencyUseCase: UpdateCurrencyUseCase
    private lateinit var getCurrenciesUseCase: GetCurrenciesUseCase
    private lateinit var useCase: SetHomeCurrencyUseCase

    @Before
    fun setup() {
        getCurrencyByIdUseCase = mockk()
        updateCurrencyUseCase = mockk()
        getCurrenciesUseCase = mockk()
        useCase = SetHomeCurrencyUseCase(
            getCurrencyByIdUseCase,
            updateCurrencyUseCase,
            getCurrenciesUseCase
        )
    }

    @Test
    fun `execute sets currency as home and unsets previous home`() = runTest {
        // Given
        val usdCurrency = createCurrency(1, "USD", "$", isDefault = true)
        val eurCurrency = createCurrency(2, "EUR", "€", isDefault = false)
        val gbpCurrency = createCurrency(3, "GBP", "£", isDefault = false)
        
        coEvery { getCurrenciesUseCase.execute() } returns listOf(usdCurrency, eurCurrency, gbpCurrency)
        coEvery { getCurrencyByIdUseCase.execute(2) } returns eurCurrency
        coEvery { updateCurrencyUseCase.execute(any()) } returns Result.success(true)

        // When
        val result = useCase.execute(2)

        // Then
        expectThat(result.isSuccess).isTrue()
        
        // Verify USD was unset
        coVerify { updateCurrencyUseCase.execute(match { it.id == 1L && !it.isDefault }) }
        
        // Verify EUR was set
        coVerify { updateCurrencyUseCase.execute(match { it.id == 2L && it.isDefault }) }
    }

    @Test
    fun `execute returns failure when currency not found`() = runTest {
        // Given
        coEvery { getCurrenciesUseCase.execute() } returns emptyList()
        coEvery { getCurrencyByIdUseCase.execute(99) } returns null

        // When
        val result = useCase.execute(99)

        // Then
        expectThat(result.isFailure).isTrue()
        expectThat(result.exceptionOrNull())
            .isNotNull()
            .get { message }.isNotNull().contains("Currency not found")
    }

    @Test
    fun `execute only unsets currencies that are currently default`() = runTest {
        // Given
        val usdCurrency = createCurrency(1, "USD", "$", isDefault = false)
        val eurCurrency = createCurrency(2, "EUR", "€", isDefault = true)
        val gbpCurrency = createCurrency(3, "GBP", "£", isDefault = false)
        
        coEvery { getCurrenciesUseCase.execute() } returns listOf(usdCurrency, eurCurrency, gbpCurrency)
        coEvery { getCurrencyByIdUseCase.execute(3) } returns gbpCurrency
        coEvery { updateCurrencyUseCase.execute(any()) } returns Result.success(true)

        // When
        val result = useCase.execute(3)

        // Then
        expectThat(result.isSuccess).isTrue()
        
        // Verify EUR was unset
        coVerify(exactly = 1) { updateCurrencyUseCase.execute(match { it.id == 2L && !it.isDefault }) }
        
        // Verify USD was NOT touched (already not default)
        coVerify(exactly = 0) { updateCurrencyUseCase.execute(match { it.id == 1L }) }
        
        // Verify GBP was set
        coVerify { updateCurrencyUseCase.execute(match { it.id == 3L && it.isDefault }) }
    }

    @Test
    fun `execute does not unset same currency being set`() = runTest {
        // Given
        val usdCurrency = createCurrency(1, "USD", "$", isDefault = true)
        
        coEvery { getCurrenciesUseCase.execute() } returns listOf(usdCurrency)
        coEvery { getCurrencyByIdUseCase.execute(1) } returns usdCurrency
        coEvery { updateCurrencyUseCase.execute(any()) } returns Result.success(true)

        // When
        val result = useCase.execute(1)

        // Then
        expectThat(result.isSuccess).isTrue()
        
        // Should only be called once (to set, not to unset the same currency)
        coVerify(exactly = 1) { updateCurrencyUseCase.execute(any()) }
    }

    @Test
    fun `execute handles exceptions gracefully`() = runTest {
        // Given
        coEvery { getCurrenciesUseCase.execute() } throws RuntimeException("Database error")

        // When
        val result = useCase.execute(1)

        // Then
        expectThat(result.isFailure).isTrue()
        expectThat(result.exceptionOrNull())
            .isNotNull()
            .isA<RuntimeException>()
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
