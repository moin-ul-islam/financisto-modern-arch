package ru.orangesoftware.financisto.domain.model

/**
 * Domain model for Transaction entity.
 * 
 * Represents the core business concept of a financial transaction without any 
 * database or UI framework concerns. Contains business logic and validation rules.
 * 
 * Uses Long timestamps (milliseconds since epoch) for Android compatibility.
 */
data class Transaction(
    val id: TransactionId = TransactionId.NONE,
    val fromAccount: Account,
    val toAccount: Account? = null, // null for non-transfer transactions
    val category: Category? = null,
    val amount: Money,
    val originalAmount: Money? = null, // for multi-currency transactions
    val exchangeRate: ExchangeRate? = null,
    val datetime: Long = System.currentTimeMillis(),
    val payee: Payee? = null,
    val note: String? = null,
    val location: Location? = null,
    val project: Project? = null,
    val status: TransactionStatus = TransactionStatus.CLEARED,
    val parentTransaction: TransactionId? = null, // for split transactions
    val splitTransactions: List<Transaction> = emptyList(),
    val attachmentFileName: String? = null,
    val isTemplate: Boolean = false,
    val templateName: String? = null,
    val recurrence: RecurrenceRule? = null
) {
    
    /**
     * Business logic: Check if this is a transfer transaction
     */
    fun isTransfer(): Boolean = toAccount != null
    
    /**
     * Business logic: Check if this is an income transaction
     */
    fun isIncome(): Boolean = !isTransfer() && amount.isPositive()
    
    /**
     * Business logic: Check if this is an expense transaction
     */
    fun isExpense(): Boolean = !isTransfer() && amount.isNegative()
    
    /**
     * Business logic: Check if this is a split transaction (has children)
     */
    fun isSplitTransaction(): Boolean = splitTransactions.isNotEmpty()
    
    /**
     * Business logic: Check if this is part of a split (has parent)
     */
    fun isPartOfSplit(): Boolean = parentTransaction != null
    
    /**
     * Business logic: Check if this is a multi-currency transaction
     */
    fun isMultiCurrency(): Boolean = originalAmount != null && exchangeRate != null
    
    /**
     * Business logic: Get the effective amount for the from account
     */
    fun getFromAccountAmount(): Money {
        return if (isTransfer()) -amount.abs() else amount
    }
    
    /**
     * Business logic: Get the effective amount for the to account (transfers only)
     */
    fun getToAccountAmount(): Money? {
        return if (isTransfer()) {
            if (isMultiCurrency()) {
                originalAmount ?: amount.abs()
            } else {
                amount.abs()
            }
        } else null
    }
    
    /**
     * Business logic: Calculate total split amount
     */
    fun getTotalSplitAmount(): Money {
        return splitTransactions.fold(Money.ZERO) { acc, split -> acc + split.amount }
    }
    
    /**
     * Business logic: Validate transaction data
     */
    fun validate(): TransactionValidationResult {
        val errors = mutableListOf<String>()
        
        if (amount.isZero()) {
            errors.add("Transaction amount cannot be zero")
        }
        
        if (isTransfer() && toAccount == null) {
            errors.add("Transfer transactions must have a destination account")
        }
        
        if (isTransfer() && fromAccount.id == toAccount?.id) {
            errors.add("Cannot transfer to the same account")
        }
        
        if (isSplitTransaction()) {
            val totalSplit = getTotalSplitAmount()
            if (totalSplit != amount.abs()) {
                errors.add("Split transaction amounts must equal total amount")
            }
        }
        
        if (isMultiCurrency()) {
            if (exchangeRate == null) {
                errors.add("Multi-currency transactions must have exchange rate")
            }
            if (originalAmount == null) {
                errors.add("Multi-currency transactions must have original amount")
            }
        }
        
        return if (errors.isEmpty()) {
            TransactionValidationResult.Valid
        } else {
            TransactionValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Business logic: Create a transfer transaction
     */
    fun createTransfer(
        fromAccount: Account,
        toAccount: Account,
        amount: Money,
        datetime: Long = System.currentTimeMillis(),
        note: String? = null
    ): Transaction {
        return Transaction(
            fromAccount = fromAccount,
            toAccount = toAccount,
            amount = amount,
            datetime = datetime,
            note = note
        )
    }
    
    companion object {
        /**
         * Factory method for creating expense transactions
         */
        fun createExpense(
            fromAccount: Account,
            category: Category?,
            amount: Money,
            payee: Payee? = null,
            note: String? = null
        ): Transaction {
            return Transaction(
                fromAccount = fromAccount,
                category = category,
                amount = -amount.abs(), // Expenses are negative
                payee = payee,
                note = note
            )
        }
        
        /**
         * Factory method for creating income transactions
         */
        fun createIncome(
            toAccount: Account,
            category: Category?,
            amount: Money,
            payee: Payee? = null,
            note: String? = null
        ): Transaction {
            return Transaction(
                fromAccount = toAccount,
                category = category,
                amount = amount.abs(), // Income is positive
                payee = payee,
                note = note
            )
        }
    }
}

/**
 * Value object for Transaction ID
 */
@JvmInline
value class TransactionId(val value: Long) {
    companion object {
        val NONE = TransactionId(0L)
    }
}

/**
 * Business enumeration for transaction status
 */
enum class TransactionStatus {
    PENDING,
    CLEARED,
    RECONCILED
}

/**
 * Validation result for transaction
 */
sealed class TransactionValidationResult {
    object Valid : TransactionValidationResult()
    data class Invalid(val errors: List<String>) : TransactionValidationResult()
}
