package ru.orangesoftware.financisto.feature.account

/**
 * UI state for the Account List screen.
 * Contains data and states relevant to account list display.
 */
data class AccountListUiState(
    val accounts: List<AccountListItem> = emptyList(),
    val totalBalance: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val selectedSortOrder: AccountSortOrder = AccountSortOrder.NAME
)

/**
 * Represents an account item in the list with UI-specific formatting.
 */
data class AccountListItem(
    val id: Long,
    val title: String,
    val balance: String,
    val formattedBalance: String,
    val currencySymbol: String,
    val accountType: String,
    val iconResId: Int,
    val isActive: Boolean,
    val isIncludeIntoTotals: Boolean,
    val lastTransactionDate: Long,
    val formattedLastTransactionDate: String,
    val transactionCount: Int,
    val note: String
)

/**
 * Sort orders for account list.
 */
enum class AccountSortOrder {
    NAME,
    BALANCE,
    TYPE,
    LAST_TRANSACTION_DATE
}

/**
 * User actions that can be performed on the Account List screen.
 */
sealed class AccountListAction {
    object LoadAccounts : AccountListAction()
    object RefreshAccounts : AccountListAction()
    data class EditAccount(val accountId: Long) : AccountListAction()
    data class DeleteAccount(val accountId: Long) : AccountListAction()
    data class ToggleAccountStatus(val accountId: Long) : AccountListAction()
    data class ViewAccountTransactions(val accountId: Long) : AccountListAction()
    data class SortBy(val sortOrder: AccountSortOrder) : AccountListAction()
    object CreateNewAccount : AccountListAction()
    object ViewAccountTotals : AccountListAction()
    object IntegrityCheck : AccountListAction()
}
