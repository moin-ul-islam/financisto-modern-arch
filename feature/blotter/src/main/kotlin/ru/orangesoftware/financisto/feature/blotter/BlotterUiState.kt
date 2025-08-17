package ru.orangesoftware.financisto.feature.blotter

/**
 * Base UI state for all screens in the modernized architecture.
 * Provides common states like Loading, Error, and Success.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : UiState<Nothing>()
}

/**
 * UI state specifically for the Blotter screen (transaction list).
 * Contains data and states relevant to transaction list display.
 */
data class BlotterUiState(
    val transactions: List<BlotterTransactionItem> = emptyList(),
    val totalAmount: String = "",
    val isLoading: Boolean = false,
    val isFilterActive: Boolean = false,
    val searchQuery: String = "",
    val selectedAccountId: Long = -1,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

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
    data class SearchTransactions(val query: String) : BlotterAction()
    data class FilterByAccount(val accountId: Long) : BlotterAction()
    data class DeleteTransaction(val transactionId: Long) : BlotterAction()
    data class DuplicateTransaction(val transactionId: Long) : BlotterAction()
    data class EditTransaction(val transactionId: Long) : BlotterAction()
    object ClearFilter : BlotterAction()
    object OpenFilter : BlotterAction()
    object CreateNewTransaction : BlotterAction()
    object CreateNewTransfer : BlotterAction()
}
