package ru.orangesoftware.financisto.domain.model

/**
 * Domain model for Account entity.
 * 
 * This represents the core business concept of an Account without any 
 * database or UI framework concerns. Contains only business logic and rules.
 * 
 * Uses Long timestamps (milliseconds since epoch) for Android compatibility.
 * 
 * Domain models are:
 * - Framework-agnostic (no Android/Room/UI dependencies)
 * - Focused on business logic and validation
 * - Immutable data classes with business methods
 * - Used by use cases for business operations
 */
data class Account(
    val id: AccountId = AccountId.NONE,
    val title: String,
    val type: AccountType,
    val currency: Currency,
    val totalAmount: Money = Money.ZERO,
    val totalLimit: Money = Money.ZERO,
    val isActive: Boolean = true,
    val isIncludeIntoTotals: Boolean = true,
    val creationDate: Long = System.currentTimeMillis(),
    val lastTransactionDate: Long? = null,
    val cardIssuer: String? = null,
    val issuer: String? = null,
    val number: String? = null,
    val note: String? = null,
    val sortOrder: Int = 0
) {
    /**
     * Business logic: Check if account can accept transactions
     */
    fun canAcceptTransactions(): Boolean = isActive
    
    /**
     * Business logic: Check if account should be included in totals calculation
     */
    fun isIncludedInTotals(): Boolean = isActive && isIncludeIntoTotals
    
    /**
     * Business logic: Get available balance for spending
     */
    fun getAvailableBalance(): Money {
        return when (type) {
            AccountType.CREDIT_CARD -> totalLimit + totalAmount // For credit cards, negative amount means available credit
            else -> totalAmount
        }
    }
    
    /**
     * Business logic: Check if account is over limit
     */
    fun isOverLimit(): Boolean {
        return when (type) {
            AccountType.CREDIT_CARD -> totalAmount < -totalLimit
            else -> false
        }
    }
    
    /**
     * Business logic: Update total amount after transaction
     */
    fun withUpdatedAmount(amount: Money): Account {
        return copy(
            totalAmount = totalAmount + amount,
            lastTransactionDate = System.currentTimeMillis()
        )
    }
    
    /**
     * Business logic: Validate account data
     */
    fun validate(): AccountValidationResult {
        val errors = mutableListOf<String>()
        
        if (title.isBlank()) {
            errors.add("Account title cannot be empty")
        }
        
        if (type == AccountType.CREDIT_CARD && totalLimit < Money.ZERO) {
            errors.add("Credit card limit cannot be negative")
        }
        
        return if (errors.isEmpty()) {
            AccountValidationResult.Valid
        } else {
            AccountValidationResult.Invalid(errors)
        }
    }
    
    companion object {
        /**
         * Factory method for creating new accounts
         */
        fun createNew(
            title: String,
            type: AccountType,
            currency: Currency,
            totalLimit: Money = Money.ZERO
        ): Account {
            return Account(
                title = title,
                type = type,
                currency = currency,
                totalLimit = totalLimit
            )
        }
    }
}

/**
 * Value object for Account ID
 */
@JvmInline
value class AccountId(val value: Long) {
    companion object {
        val NONE = AccountId(0L)
    }
}

/**
 * Business enumeration for account types
 */
enum class AccountType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    CREDIT_CARD("Credit Card"),
    ASSET("Asset"),
    LIABILITY("Liability"),
    INVESTMENT("Investment"),
    OTHER("Other");
    
    /**
     * Business logic: Check if account type supports credit limit
     */
    fun supportsCreditLimit(): Boolean = this == CREDIT_CARD
    
    /**
     * Business logic: Check if account type is typically included in totals
     */
    fun isTypicallyIncludedInTotals(): Boolean {
        return when (this) {
            CASH, BANK, CREDIT_CARD, ASSET, INVESTMENT -> true
            LIABILITY, OTHER -> false
        }
    }
}

/**
 * Validation result for account
 */
sealed class AccountValidationResult {
    object Valid : AccountValidationResult()
    data class Invalid(val errors: List<String>) : AccountValidationResult()
}
