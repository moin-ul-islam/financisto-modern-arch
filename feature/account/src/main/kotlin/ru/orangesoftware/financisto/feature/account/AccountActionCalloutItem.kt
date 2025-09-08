package ru.orangesoftware.financisto.feature.account

/**
 * Data class representing an action item in the account action callout.
 * 
 * This represents each action button in the 3x3 grid that appears when 
 * long-clicking an account in the account list.
 */
data class AccountActionCalloutItem(
    val iconResId: Int,
    val title: String,
    val action: AccountAction
)

/**
 * Enum representing the different actions that can be performed on an account.
 * These correspond to the actions from the legacy QuickActionGrid implementation.
 */
enum class AccountAction {
    INFO,              // Show account info
    BLOTTER,           // View account transactions  
    EDIT,              // Edit account
    TRANSACTION,       // Add transaction
    TRANSFER,          // Add transfer
    BALANCE,           // Update balance
    PURGE,             // Delete old transactions
    CLOSE_REOPEN,      // Close/reopen account
    DELETE             // Delete account
}
