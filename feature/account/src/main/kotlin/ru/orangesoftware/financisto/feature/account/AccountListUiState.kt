package ru.orangesoftware.financisto.feature.account

/**
 * Enhanced UI state management for the Account List screen.
 * Uses sealed classes to represent different screen states clearly.
 */
sealed class AccountListScreenState {
    object Loading : AccountListScreenState()
    object Empty : AccountListScreenState()
    data class Content(val data: AccountListContentData) : AccountListScreenState()
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val canRetry: Boolean = true
    ) : AccountListScreenState()
}

/**
 * UI state for the Account List screen.
 * Combines screen state with additional UI properties.
 */
data class AccountListUiState(
    val screenState: AccountListScreenState = AccountListScreenState.Loading,
    val isRefreshing: Boolean = false,
    val totalCalculationState: TotalCalculationState = TotalCalculationState.Idle,
    val selectedSortOrder: AccountSortOrder = AccountSortOrder.NAME,
    val showIntegrityError: Boolean = false,
    val showMenuButton: Boolean = true
)

/**
 * Content data for successful state
 */
data class AccountListContentData(
    val accounts: List<AccountListItem> = emptyList(),
    val totalBalance: String = "",
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * States for total calculation (async operation in legacy)
 */
sealed class TotalCalculationState {
    object Idle : TotalCalculationState()
    object Calculating : TotalCalculationState()
    data class Completed(val total: String, val warningMessage: String? = null) : TotalCalculationState()
    data class Failed(val error: String) : TotalCalculationState()
}

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
    val note: String,
    // Additional UI properties for Compose implementation
    val topText: String = "", // Account type/issuer info
    val formattedDate: String = formattedLastTransactionDate,
    val balanceAmount: Long = 0L, // Raw amount for color determination
    val showCreditInfo: Boolean = false,
    val formattedCreditBalance: String = "",
    val showProgressBar: Boolean = false,
    val creditUtilization: Float = 0f
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
    object RetryLoading : AccountListAction()
    data class EditAccount(val accountId: Long) : AccountListAction()
    data class DeleteAccount(val accountId: Long) : AccountListAction()
    data class ToggleAccountStatus(val accountId: Long) : AccountListAction()
    data class ViewAccountTransactions(val accountId: Long) : AccountListAction()
    data class SortBy(val sortOrder: AccountSortOrder) : AccountListAction()
    data class ShowAccountInfo(val accountId: Long) : AccountListAction()
    data class UpdateAccountBalance(val accountId: Long) : AccountListAction()
    data class PurgeAccount(val accountId: Long) : AccountListAction()
    object CreateNewAccount : AccountListAction()
    object ViewAccountTotals : AccountListAction()
    object IntegrityCheck : AccountListAction()
    object DismissIntegrityError : AccountListAction()
    object CalculateTotals : AccountListAction()
}
