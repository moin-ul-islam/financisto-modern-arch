package ru.orangesoftware.financisto.domain.model

/**
 * Domain model for Currency entity.
 * 
 * Represents the core business concept of a currency without any 
 * database or UI framework concerns.
 */
data class Currency(
    val id: CurrencyId = CurrencyId.NONE,
    val name: String,
    val title: String,
    val symbol: String,
    val isDefault: Boolean = false,
    val decimals: Int = 2,
    val decimalSeparator: String = ".",
    val groupSeparator: String = ",",
    val symbolFormat: SymbolFormat = SymbolFormat.RS,
    val isActive: Boolean = true
) {
    /**
     * Business logic: Format money amount according to currency rules
     */
    fun formatAmount(money: Money): String {
        val amount = money.toDecimal()
        return if (decimals == 0) {
            "$symbol${amount.toLong()}"
        } else {
            "$symbol$amount"
        }
    }
    
    /**
     * Business logic: Validate currency data
     */
    fun validate(): CurrencyValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Currency name cannot be empty")
        }
        
        if (symbol.isBlank()) {
            errors.add("Currency symbol cannot be empty")
        }
        
        if (decimals < 0 || decimals > 4) {
            errors.add("Currency decimals must be between 0 and 4")
        }
        
        return if (errors.isEmpty()) {
            CurrencyValidationResult.Valid
        } else {
            CurrencyValidationResult.Invalid(errors)
        }
    }
    
    companion object {
        val DEFAULT_CURRENCY = Currency(
            id = CurrencyId(1),
            name = "US Dollar",
            title = "USD",
            symbol = "$",
            isDefault = true
        )
        
        /**
         * Factory method for creating standard currencies
         */
        fun createStandard(
            name: String,
            title: String,
            symbol: String,
            decimals: Int = 2
        ): Currency {
            return Currency(
                name = name,
                title = title,
                symbol = symbol,
                decimals = decimals
            )
        }
        
        /**
         * Common currencies
         */
        val USD = createStandard("US Dollar", "USD", "$")
        val EUR = createStandard("Euro", "EUR", "€")
        val GBP = createStandard("British Pound", "GBP", "£")
    }
}

/**
 * Value object for Currency ID
 */
@JvmInline
value class CurrencyId(val value: Long) {
    companion object {
        val NONE = CurrencyId(0L)
    }
}

/**
 * Business enumeration for symbol format
 */
enum class SymbolFormat(val displayName: String) {
    RS("Right Symbol"),  // $100
    LS("Left Symbol"),   // 100$
    RSP("Right Symbol with Space"),  // $ 100
    LSP("Left Symbol with Space");   // 100 $
}

/**
 * Validation result for currency
 */
sealed class CurrencyValidationResult {
    object Valid : CurrencyValidationResult()
    data class Invalid(val errors: List<String>) : CurrencyValidationResult()
}
