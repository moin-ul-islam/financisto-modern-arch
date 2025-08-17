package ru.orangesoftware.financisto.domain.model

/**
 * Domain model for Payee entity.
 * 
 * Represents a payee/recipient in transactions - the person, company, or entity
 * that money is paid to or received from. Payees help in categorizing and
 * tracking recurring transaction partners.
 * 
 * Domain models are:
 * - Framework-agnostic (no Android/Room/UI dependencies)
 * - Focused on business logic and validation
 * - Immutable data classes with business methods
 * - Used by use cases for business operations
 */
data class Payee(
    val id: PayeeId = PayeeId.NONE,
    val name: String,
    val isActive: Boolean = true,
    val lastCategoryId: CategoryId? = null,
    val sortOrder: Int = 0,
    val defaultAmount: Money? = null,
    val notes: String? = null
) {
    
    /**
     * Business logic: Check if payee can be used in transactions
     */
    fun canBeUsedInTransactions(): Boolean = isActive && name.isNotBlank()
    
    /**
     * Business logic: Get display name with formatting
     */
    fun getDisplayName(): String {
        return if (name.isBlank()) "<No Payee>" else name
    }
    
    /**
     * Business logic: Check if this payee has a preferred category
     */
    fun hasPreferredCategory(): Boolean = lastCategoryId != null && lastCategoryId != CategoryId.NONE
    
    /**
     * Business logic: Check if this payee has a default amount
     */
    fun hasDefaultAmount(): Boolean = defaultAmount != null && !defaultAmount.isZero()
    
    /**
     * Business logic: Update preferred category based on transaction usage
     */
    fun updatePreferredCategory(categoryId: CategoryId): Payee {
        return copy(lastCategoryId = categoryId)
    }
    
    /**
     * Business logic: Validate payee data
     */
    fun validate(): PayeeValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Payee name cannot be empty")
        }
        
        if (name.length > 255) {
            errors.add("Payee name cannot exceed 255 characters")
        }
        
        if (sortOrder < 0) {
            errors.add("Sort order cannot be negative")
        }
        
        return if (errors.isEmpty()) {
            PayeeValidationResult.Valid
        } else {
            PayeeValidationResult.Invalid(errors)
        }
    }
    
    companion object {
        val EMPTY = Payee(
            id = PayeeId.NONE,
            name = "No Payee",
            isActive = true
        )
        
        /**
         * Factory method for creating new payees
         */
        fun create(
            name: String,
            defaultAmount: Money? = null,
            notes: String? = null
        ): Payee {
            return Payee(
                name = name.trim(),
                defaultAmount = defaultAmount,
                notes = notes?.trim()
            )
        }
    }
}

/**
 * Value object for Payee ID
 */
@JvmInline
value class PayeeId(val value: Long) {
    companion object {
        val NONE = PayeeId(0L)
    }
}

/**
 * Validation result for payee
 */
sealed class PayeeValidationResult {
    object Valid : PayeeValidationResult()
    data class Invalid(val errors: List<String>) : PayeeValidationResult()
}
