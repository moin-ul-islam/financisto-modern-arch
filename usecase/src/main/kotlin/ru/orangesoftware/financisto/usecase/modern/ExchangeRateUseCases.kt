package ru.orangesoftware.financisto.usecase.modern

import javax.inject.Inject

/**
 * Domain model for currency exchange rate.
 * 
 * Represents an exchange rate between two currencies at a specific date.
 */
data class ExchangeRate(
    val fromCurrencyId: Long,
    val toCurrencyId: Long,
    val rate: Float,
    val rateDate: Long
)

/**
 * Use case to get the latest exchange rate between two currencies.
 * 
 * This is used for converting amounts from one currency to another
 * when calculating totals in the home currency.
 */
class GetLatestExchangeRateUseCase @Inject constructor(
    // We'll inject the repository when it's available
    // For now, this is just a placeholder
) {
    /**
     * Get the latest exchange rate from one currency to another.
     * Returns null if no rate is available.
     */
    suspend fun execute(fromCurrencyId: Long, toCurrencyId: Long): ExchangeRate? {
        // If currencies are the same, return rate of 1.0
        if (fromCurrencyId == toCurrencyId) {
            return ExchangeRate(
                fromCurrencyId = fromCurrencyId,
                toCurrencyId = toCurrencyId,
                rate = 1.0f,
                rateDate = System.currentTimeMillis()
            )
        }
        
        // TODO: Get rate from repository
        // For now, return null (no rate available)
        return null
    }
}

/**
 * Use case to convert an amount from one currency to another using exchange rates.
 */
class ConvertCurrencyUseCase @Inject constructor(
    private val getLatestExchangeRateUseCase: GetLatestExchangeRateUseCase
) {
    /**
     * Convert an amount from one currency to another.
     * Returns null if conversion is not possible (no exchange rate available).
     */
    suspend fun execute(amount: Long, fromCurrencyId: Long, toCurrencyId: Long): Long? {
        // If currencies are the same, no conversion needed
        if (fromCurrencyId == toCurrencyId) {
            return amount
        }
        
        // Get the exchange rate
        val rate = getLatestExchangeRateUseCase.execute(fromCurrencyId, toCurrencyId)
            ?: return null
        
        // Convert the amount
        return (amount * rate.rate).toLong()
    }
}

/**
 * Use case to calculate total balance in home currency from multiple account balances.
 * 
 * This handles conversion of balances from different currencies to the home currency.
 */
class CalculateTotalInHomeCurrencyUseCase @Inject constructor(
    private val getHomeCurrencyUseCase: GetHomeCurrencyUseCase,
    private val convertCurrencyUseCase: ConvertCurrencyUseCase
) {
    /**
     * Account balance data for calculation
     */
    data class AccountBalance(
        val amount: Long,
        val currencyId: Long,
        val includeInTotals: Boolean
    )
    
    /**
     * Result of total calculation
     */
    data class TotalResult(
        val total: Long,
        val homeCurrencyId: Long,
        val hasConversionWarnings: Boolean,
        val unconvertedAccounts: List<Long> = emptyList()
    )
    
    /**
     * Calculate total balance in home currency.
     * Only includes accounts where includeInTotals is true.
     * Returns null if no home currency is set.
     */
    suspend fun execute(accountBalances: List<AccountBalance>): TotalResult? {
        // Get home currency
        val homeCurrency = getHomeCurrencyUseCase.execute() ?: return null
        
        var total = 0L
        val unconvertedAccounts = mutableListOf<Long>()
        
        // Sum all account balances, converting to home currency
        accountBalances.forEach { account ->
            if (account.includeInTotals) {
                val convertedAmount = convertCurrencyUseCase.execute(
                    amount = account.amount,
                    fromCurrencyId = account.currencyId,
                    toCurrencyId = homeCurrency.id
                )
                
                if (convertedAmount != null) {
                    total += convertedAmount
                } else {
                    // Could not convert - missing exchange rate
                    unconvertedAccounts.add(account.currencyId)
                }
            }
        }
        
        return TotalResult(
            total = total,
            homeCurrencyId = homeCurrency.id,
            hasConversionWarnings = unconvertedAccounts.isNotEmpty(),
            unconvertedAccounts = unconvertedAccounts
        )
    }
}
