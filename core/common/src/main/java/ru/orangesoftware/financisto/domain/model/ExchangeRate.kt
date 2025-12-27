package ru.orangesoftware.financisto.domain.model

import java.math.BigDecimal

/**
 * Domain model for Exchange Rate.
 * 
 * Represents the conversion rate between two currencies at a specific point in time.
 * Exchange rates are used for multi-currency transactions and reporting.
 * 
 * Domain models are:
 * - Framework-agnostic (no Android/Room/UI dependencies)
 * - Focused on business logic and validation
 * - Immutable data classes with business methods
 * - Use Long timestamps (milliseconds since epoch) for Android compatibility
 * - Used by use cases for business operations
 */
data class ExchangeRate(
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val rate: BigDecimal,
    val date: Long = System.currentTimeMillis(),
    val source: ExchangeRateSource = ExchangeRateSource.MANUAL
) {
    
    /**
     * Business logic: Convert amount from source currency to target currency
     */
    fun convert(amount: Money): Money {
        // Convert Money to BigDecimal using source currency decimals
        val sourceAmount = amount.getAmount(fromCurrency)
        val convertedAmount = sourceAmount.multiply(rate)
        return Money(convertedAmount, toCurrency)
    }
    
    /**
     * Business logic: Get the inverse exchange rate
     */
    fun inverse(): ExchangeRate {
        val inverseRate = BigDecimal.ONE.divide(rate, 10, BigDecimal.ROUND_HALF_UP)
        return ExchangeRate(
            fromCurrency = toCurrency,
            toCurrency = fromCurrency,
            rate = inverseRate,
            date = date,
            source = source
        )
    }
    
    /**
     * Business logic: Check if exchange rate is current (within last 24 hours)
     */
    fun isCurrent(): Boolean {
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000L
        val twentyFourHoursAgo = System.currentTimeMillis() - twentyFourHoursInMillis
        return date > twentyFourHoursAgo
    }
    
    /**
     * Business logic: Check if exchange rate is reasonable (not zero or negative)
     */
    fun isReasonable(): Boolean {
        return rate > BigDecimal.ZERO && rate < BigDecimal.valueOf(1000000)
    }
    
    /**
     * Business logic: Get age of exchange rate in hours
     */
    fun getAgeInHours(): Long {
        val ageInMillis = System.currentTimeMillis() - date
        return ageInMillis / (1000 * 60 * 60) // Convert to hours
    }
    
    /**
     * Business logic: Validate exchange rate
     */
    fun validate(): ExchangeRateValidationResult {
        val errors = mutableListOf<String>()
        
        if (fromCurrency == toCurrency) {
            errors.add("From and to currencies cannot be the same")
        }
        
        if (rate <= BigDecimal.ZERO) {
            errors.add("Exchange rate must be positive")
        }
        
        if (rate >= BigDecimal.valueOf(1000000)) {
            errors.add("Exchange rate is unreasonably high")
        }
        
        if (date > System.currentTimeMillis()) {
            errors.add("Exchange rate date cannot be in the future")
        }
        
        return if (errors.isEmpty()) {
            ExchangeRateValidationResult.Valid
        } else {
            ExchangeRateValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Business logic: Format rate for display
     */
    fun formatRate(): String {
        return "1 ${fromCurrency.symbol} = ${rate.setScale(4, BigDecimal.ROUND_HALF_UP)} ${toCurrency.symbol}"
    }
    
    companion object {
        /**
         * Factory method for creating manual exchange rates
         */
        fun manual(
            fromCurrency: Currency,
            toCurrency: Currency,
            rate: BigDecimal
        ): ExchangeRate {
            return ExchangeRate(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rate = rate,
                source = ExchangeRateSource.MANUAL
            )
        }
        
        /**
         * Factory method for creating automatic exchange rates
         */
        fun automatic(
            fromCurrency: Currency,
            toCurrency: Currency,
            rate: BigDecimal,
            source: ExchangeRateSource = ExchangeRateSource.ONLINE
        ): ExchangeRate {
            return ExchangeRate(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                rate = rate,
                source = source
            )
        }
    }
}

/**
 * Business enumeration for exchange rate sources
 */
enum class ExchangeRateSource(val displayName: String) {
    MANUAL("Manual"),
    ONLINE("Online"),
    BANK("Bank"),
    GOVERNMENT("Government"),
    OTHER("Other");
    
    /**
     * Business logic: Check if source requires internet connection
     */
    fun requiresInternet(): Boolean = this == ONLINE
    
    /**
     * Business logic: Check if source is considered reliable
     */
    fun isReliable(): Boolean = this != MANUAL && this != OTHER
}

/**
 * Validation result for exchange rate
 */
sealed class ExchangeRateValidationResult {
    object Valid : ExchangeRateValidationResult()
    data class Invalid(val errors: List<String>) : ExchangeRateValidationResult()
}
