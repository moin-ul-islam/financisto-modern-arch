package ru.orangesoftware.financisto.feature.blotter

/**
 * Enhanced UI state management for the Blotter screen.
 * Uses sealed classes to represent different screen states clearly.
 */
sealed class BlotterScreenState {
    object Loading : BlotterScreenState()
    object Empty : BlotterScreenState()
    data class Content(val data: BlotterContentData) : BlotterScreenState()
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val canRetry: Boolean = true
    ) : BlotterScreenState()
}

/**
 * UI state specifically for the Blotter screen (transaction list).
 * Combines screen state with additional UI properties.
 */
data class BlotterUiState(
    val screenState: BlotterScreenState = BlotterScreenState.Loading,
    val isRefreshing: Boolean = false,
    val totalCalculationState: TotalCalculationState = TotalCalculationState.Idle,
    val searchQuery: String = "",
    val isFilterActive: Boolean = false,
    val selectedAccountId: Long = -1,
    val totalAmount: String = "",
    val showIntegrityError: Boolean = false
)

/**
 * Content data for successful state
 */
data class BlotterContentData(
    val transactions: List<BlotterTransactionItem> = emptyList(),
    val hasMoreItems: Boolean = false,
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
 * Represents a transaction item in the blotter list with UI-specific formatting.
 */
data class BlotterTransactionItem(
    val id: Long,
    val accountId: Long,
    val categoryName: String,
    val amount: String,
    val formattedAmount: String,
    val dateTime: Long,
    val formattedDate: String,
    val payee: String,
    val note: String,
    val fromAccountTitle: String,
    val toAccountTitle: String,
    val isTemplate: Boolean,
    val isTransfer: Boolean,
    val categoryIconResId: Int,
    val accountIconResId: Int,
    val currencySymbol: String
)

/**
 * User actions that can be performed on the Blotter screen.
 */
sealed class BlotterAction {
    object LoadTransactions : BlotterAction()
    object RefreshTransactions : BlotterAction()
    object RetryLoading : BlotterAction()
    data class SearchTransactions(val query: String) : BlotterAction()
    data class FilterByAccount(val accountId: Long) : BlotterAction()
    data class DeleteTransaction(val transactionId: Long) : BlotterAction()
    data class DuplicateTransaction(val transactionId: Long) : BlotterAction()
    data class EditTransaction(val transactionId: Long) : BlotterAction()
    object ClearFilter : BlotterAction()
    object OpenFilter : BlotterAction()
    object CreateNewTransaction : BlotterAction()
    object CreateNewTransfer : BlotterAction()
    object DismissIntegrityError : BlotterAction()
    object CalculateTotals : BlotterAction()
}
