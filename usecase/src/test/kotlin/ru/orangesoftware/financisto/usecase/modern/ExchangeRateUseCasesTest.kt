package ru.orangesoftware.financisto.usecase.modern

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests for GetLatestExchangeRateUseCase.
 * 
 * Tests retrieval of exchange rates between currencies.
 */
class GetLatestExchangeRateUseCaseTest {

    private lateinit var useCase: GetLatestExchangeRateUseCase

    @Before
    fun setup() {
        useCase = GetLatestExchangeRateUseCase()
    }

    @Test
    fun `execute returns rate of 1 for same currency`() = runTest {
        // When
        val result = useCase.execute(fromCurrencyId = 1, toCurrencyId = 1)

        // Then
        expectThat(result).isNotNull().and {
            get { fromCurrencyId }.isEqualTo(1L)
            get { toCurrencyId }.isEqualTo(1L)
            get { rate }.isEqualTo(1.0f)
        }
    }

    @Test
    fun `execute returns null for different currencies without repository`() = runTest {
        // When (no repository integration yet)
        val result = useCase.execute(fromCurrencyId = 1, toCurrencyId = 2)

        // Then
        expectThat(result).isNull()
    }
}

/**
 * Unit tests for ConvertCurrencyUseCase.
 * 
 * Tests currency conversion using exchange rates.
 */
class ConvertCurrencyUseCaseTest {

    private lateinit var getLatestExchangeRateUseCase: GetLatestExchangeRateUseCase
    private lateinit var useCase: ConvertCurrencyUseCase

    @Before
    fun setup() {
        getLatestExchangeRateUseCase = mockk()
        useCase = ConvertCurrencyUseCase(getLatestExchangeRateUseCase)
    }

    @Test
    fun `execute returns same amount for same currency`() = runTest {
        // When
        val result = useCase.execute(amount = 10000, fromCurrencyId = 1, toCurrencyId = 1)

        // Then
        expectThat(result).isEqualTo(10000)
    }

    @Test
    fun `execute converts amount using exchange rate`() = runTest {
        // Given
        val rate = ExchangeRate(
            fromCurrencyId = 1,
            toCurrencyId = 2,
            rate = 0.85f,
            rateDate = System.currentTimeMillis()
        )
        coEvery { getLatestExchangeRateUseCase.execute(1, 2) } returns rate

        // When
        val result = useCase.execute(amount = 10000, fromCurrencyId = 1, toCurrencyId = 2)

        // Then
        expectThat(result).isEqualTo(8500) // 10000 * 0.85
    }

    @Test
    fun `execute returns null when exchange rate not available`() = runTest {
        // Given
        coEvery { getLatestExchangeRateUseCase.execute(1, 2) } returns null

        // When
        val result = useCase.execute(amount = 10000, fromCurrencyId = 1, toCurrencyId = 2)

        // Then
        expectThat(result).isNull()
    }

    @Test
    fun `execute handles fractional conversion correctly`() = runTest {
        // Given
        val rate = ExchangeRate(
            fromCurrencyId = 1,
            toCurrencyId = 2,
            rate = 1.25f,
            rateDate = System.currentTimeMillis()
        )
        coEvery { getLatestExchangeRateUseCase.execute(1, 2) } returns rate

        // When
        val result = useCase.execute(amount = 10000, fromCurrencyId = 1, toCurrencyId = 2)

        // Then
        expectThat(result).isEqualTo(12500) // 10000 * 1.25
    }
}

/**
 * Unit tests for CalculateTotalInHomeCurrencyUseCase.
 * 
 * Tests calculation of total balance across accounts in home currency.
 */
class CalculateTotalInHomeCurrencyUseCaseTest {

    private lateinit var getHomeCurrencyUseCase: GetHomeCurrencyUseCase
    private lateinit var convertCurrencyUseCase: ConvertCurrencyUseCase
    private lateinit var useCase: CalculateTotalInHomeCurrencyUseCase

    @Before
    fun setup() {
        getHomeCurrencyUseCase = mockk()
        convertCurrencyUseCase = mockk()
        useCase = CalculateTotalInHomeCurrencyUseCase(
            getHomeCurrencyUseCase,
            convertCurrencyUseCase
        )
    }

    @Test
    fun `execute returns null when no home currency is set`() = runTest {
        // Given
        coEvery { getHomeCurrencyUseCase.execute() } returns null

        // When
        val result = useCase.execute(emptyList())

        // Then
        expectThat(result).isNull()
    }

    @Test
    fun `execute calculates total for single currency accounts`() = runTest {
        // Given
        val homeCurrency = createCurrency(1, "USD")
        coEvery { getHomeCurrencyUseCase.execute() } returns homeCurrency
        coEvery { convertCurrencyUseCase.execute(any(), any(), any()) } answers { 
            firstArg() // Same currency, no conversion
        }

        val balances = listOf(
            createAccountBalance(amount = 10000, currencyId = 1, includeInTotals = true),
            createAccountBalance(amount = 20000, currencyId = 1, includeInTotals = true),
            createAccountBalance(amount = 30000, currencyId = 1, includeInTotals = true)
        )

        // When
        val result = useCase.execute(balances)

        // Then
        expectThat(result).isNotNull().and {
            get { total }.isEqualTo(60000)
            get { homeCurrencyId }.isEqualTo(1L)
            get { hasConversionWarnings }.isFalse()
            get { unconvertedAccounts }.isEmpty()
        }
    }

    @Test
    fun `execute excludes accounts not included in totals`() = runTest {
        // Given
        val homeCurrency = createCurrency(1, "USD")
        coEvery { getHomeCurrencyUseCase.execute() } returns homeCurrency
        coEvery { convertCurrencyUseCase.execute(any(), any(), any()) } answers { 
            firstArg()
        }

        val balances = listOf(
            createAccountBalance(amount = 10000, currencyId = 1, includeInTotals = true),
            createAccountBalance(amount = 20000, currencyId = 1, includeInTotals = false), // Excluded
            createAccountBalance(amount = 30000, currencyId = 1, includeInTotals = true)
        )

        // When
        val result = useCase.execute(balances)

        // Then
        expectThat(result).isNotNull().and {
            get { total }.isEqualTo(40000) // Only includes first and third
        }
    }

    @Test
    fun `execute converts multi-currency accounts to home currency`() = runTest {
        // Given
        val homeCurrency = createCurrency(1, "USD")
        coEvery { getHomeCurrencyUseCase.execute() } returns homeCurrency
        
        // Mock conversions
        coEvery { convertCurrencyUseCase.execute(10000, 1, 1) } returns 10000 // USD to USD
        coEvery { convertCurrencyUseCase.execute(20000, 2, 1) } returns 17000 // EUR to USD (0.85 rate)
        coEvery { convertCurrencyUseCase.execute(30000, 3, 1) } returns 37500 // GBP to USD (1.25 rate)

        val balances = listOf(
            createAccountBalance(amount = 10000, currencyId = 1, includeInTotals = true),
            createAccountBalance(amount = 20000, currencyId = 2, includeInTotals = true),
            createAccountBalance(amount = 30000, currencyId = 3, includeInTotals = true)
        )

        // When
        val result = useCase.execute(balances)

        // Then
        expectThat(result).isNotNull().and {
            get { total }.isEqualTo(64500) // 10000 + 17000 + 37500
            get { homeCurrencyId }.isEqualTo(1L)
            get { hasConversionWarnings }.isFalse()
        }
    }

    @Test
    fun `execute tracks unconverted accounts when exchange rate missing`() = runTest {
        // Given
        val homeCurrency = createCurrency(1, "USD")
        coEvery { getHomeCurrencyUseCase.execute() } returns homeCurrency
        
        coEvery { convertCurrencyUseCase.execute(10000, 1, 1) } returns 10000
        coEvery { convertCurrencyUseCase.execute(20000, 2, 1) } returns null // No rate
        coEvery { convertCurrencyUseCase.execute(30000, 3, 1) } returns 37500

        val balances = listOf(
            createAccountBalance(amount = 10000, currencyId = 1, includeInTotals = true),
            createAccountBalance(amount = 20000, currencyId = 2, includeInTotals = true),
            createAccountBalance(amount = 30000, currencyId = 3, includeInTotals = true)
        )

        // When
        val result = useCase.execute(balances)

        // Then
        expectThat(result).isNotNull().and {
            get { total }.isEqualTo(47500) // Only USD and GBP
            get { hasConversionWarnings }.isTrue()
            get { unconvertedAccounts }.containsExactly(2L) // EUR currency ID
        }
    }

    @Test
    fun `execute handles negative balances correctly`() = runTest {
        // Given
        val homeCurrency = createCurrency(1, "USD")
        coEvery { getHomeCurrencyUseCase.execute() } returns homeCurrency
        coEvery { convertCurrencyUseCase.execute(any(), any(), any()) } answers { 
            firstArg()
        }

        val balances = listOf(
            createAccountBalance(amount = 10000, currencyId = 1, includeInTotals = true),
            createAccountBalance(amount = -5000, currencyId = 1, includeInTotals = true), // Negative
            createAccountBalance(amount = 20000, currencyId = 1, includeInTotals = true)
        )

        // When
        val result = useCase.execute(balances)

        // Then
        expectThat(result).isNotNull().and {
            get { total }.isEqualTo(25000) // 10000 - 5000 + 20000
        }
    }

    @Test
    fun `execute returns zero total for empty account list`() = runTest {
        // Given
        val homeCurrency = createCurrency(1, "USD")
        coEvery { getHomeCurrencyUseCase.execute() } returns homeCurrency

        // When
        val result = useCase.execute(emptyList())

        // Then
        expectThat(result).isNotNull().and {
            get { total }.isEqualTo(0)
            get { hasConversionWarnings }.isFalse()
        }
    }

    private fun createCurrency(id: Long, name: String) =
        ru.orangesoftware.financisto.data.model.CurrencyEntity(
            id = id,
            name = name,
            title = name,
            symbol = "$",
            isDefault = true,
            decimals = 2,
            decimalSeparator = ".",
            groupSeparator = ",",
            symbolFormat = "RS"
        )

    private fun createAccountBalance(
        amount: Long,
        currencyId: Long,
        includeInTotals: Boolean
    ) = CalculateTotalInHomeCurrencyUseCase.AccountBalance(
        amount = amount,
        currencyId = currencyId,
        includeInTotals = includeInTotals
    )
}
