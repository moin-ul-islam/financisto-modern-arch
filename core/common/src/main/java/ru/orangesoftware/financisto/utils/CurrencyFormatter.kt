package ru.orangesoftware.financisto.utils

import ru.orangesoftware.financisto.domain.model.Currency
import ru.orangesoftware.financisto.domain.model.Money
import ru.orangesoftware.financisto.domain.model.SymbolFormat

/**
 * Utility object for formatting currency amounts.
 * 
 * This provides a centralized way to format money amounts with proper
 * currency symbols, decimal places, and sign handling across all features.
 */
object CurrencyFormatter {
    
    /**
     * Format a long amount (in cents) with the given currency.
     * 
     * @param amount Amount in cents (smallest currency unit)
     * @param currency The currency to use for formatting
     * @return Formatted string like "$100.00", "€50,00", etc.
     */
    fun formatAmount(amount: Long, currency: Currency): String {
        val money = Money(amount)
        return currency.formatAmount(money)
    }
    
    /**
     * Format a Money object with the given currency.
     * 
     * @param money The money amount to format
     * @param currency The currency to use for formatting
     * @return Formatted string like "$100.00", "€50,00", etc.
     */
    fun formatAmount(money: Money, currency: Currency): String {
        return currency.formatAmount(money)
    }
    
    /**
     * Format an amount with explicit currency parameters.
     * Useful when you don't have a full Currency object.
     * 
     * @param amount Amount in cents
     * @param symbol Currency symbol (e.g., "$", "€", "₹")
     * @param decimals Number of decimal places (0-4)
     * @param symbolFormat Position of symbol relative to amount
     * @return Formatted string
     */
    fun formatAmount(
        amount: Long,
        symbol: String,
        decimals: Int = 2,
        symbolFormat: SymbolFormat = SymbolFormat.RS
    ): String {
        val absAmount = kotlin.math.abs(amount) / 100.0
        val sign = if (amount < 0) "-" else ""
        val formattedAmount = String.format("%.${decimals}f", absAmount)
        
        return when (symbolFormat) {
            SymbolFormat.RS -> "$sign$symbol$formattedAmount"
            SymbolFormat.LS -> "$sign$formattedAmount$symbol"
            SymbolFormat.RSP -> "$sign$symbol $formattedAmount"
            SymbolFormat.LSP -> "$sign$formattedAmount $symbol"
        }
    }
    
    /**
     * Format an amount with just the numeric part (no currency symbol).
     * 
     * @param amount Amount in cents
     * @param decimals Number of decimal places
     * @return Formatted string like "100.00", "-50.50"
     */
    fun formatAmountWithoutSymbol(amount: Long, decimals: Int = 2): String {
        val absAmount = kotlin.math.abs(amount) / 100.0
        val sign = if (amount < 0) "-" else ""
        return "$sign${String.format("%.${decimals}f", absAmount)}"
    }
}
