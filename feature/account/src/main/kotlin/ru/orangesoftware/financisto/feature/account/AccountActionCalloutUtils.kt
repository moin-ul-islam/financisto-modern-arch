package ru.orangesoftware.financisto.feature.account

/**
 * Utility object to create account action callout items based on account state.
 * This recreates the logic from AccountListActivity.prepareAccountActionGrid().
 */
object AccountActionCalloutUtils {
    
    /**
     * Creates the list of action items for an account based on its state.
     * Maintains the same order and logic as the legacy implementation.
     */
    fun createAccountActions(isAccountActive: Boolean): List<AccountActionCalloutItem> {
        val actions = mutableListOf<AccountActionCalloutItem>()
        
        // 1. Info
        actions.add(AccountActionCalloutItem(
            iconResId = R.drawable.ic_action_info,
            title = "Info", // Using hardcoded string for now, will be localized
            action = AccountAction.INFO
        ))
        
        // 2. Blotter (transaction list)
        actions.add(AccountActionCalloutItem(
            iconResId = R.drawable.ic_action_list,
            title = "Blotter",
            action = AccountAction.BLOTTER
        ))
        
        // 3. Edit
        actions.add(AccountActionCalloutItem(
            iconResId = R.drawable.ic_action_edit,
            title = "Edit",
            action = AccountAction.EDIT
        ))
        
        // 4. Add Transaction
        actions.add(AccountActionCalloutItem(
            iconResId = R.drawable.ic_action_add,
            title = "Transaction",
            action = AccountAction.TRANSACTION
        ))
        
        // 5. Add Transfer
        actions.add(AccountActionCalloutItem(
            iconResId = R.drawable.ic_action_transfer,
            title = "Transfer",
            action = AccountAction.TRANSFER
        ))
        
        // 6. Update Balance
        actions.add(AccountActionCalloutItem(
            iconResId = R.drawable.ic_action_tick,
            title = "Balance",
            action = AccountAction.BALANCE
        ))
        
        // 7. Delete Old Transactions
        actions.add(AccountActionCalloutItem(
            iconResId = R.drawable.ic_action_flash,
            title = "Purge", // Shorter title
            action = AccountAction.PURGE
        ))
        
        // 8. Close/Reopen Account (conditional based on account state)
        if (isAccountActive) {
            actions.add(AccountActionCalloutItem(
                iconResId = R.drawable.ic_action_lock_closed,
                title = "Close", // Shorter title
                action = AccountAction.CLOSE_REOPEN
            ))
        } else {
            actions.add(AccountActionCalloutItem(
                iconResId = R.drawable.ic_action_lock_open,
                title = "Reopen", // Shorter title
                action = AccountAction.CLOSE_REOPEN
            ))
        }
        
        // 9. Delete Account
        actions.add(AccountActionCalloutItem(
            iconResId = R.drawable.ic_action_trash,
            title = "Delete", // Shorter title
            action = AccountAction.DELETE
        ))
        
        return actions
    }
}

/**
 * Data class for representing Account state needed for creating actions.
 */
data class AccountState(
    val id: Long,
    val isActive: Boolean,
    val title: String
)
