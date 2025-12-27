package ru.orangesoftware.financisto.feature.blotter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.domain.model.Transaction
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsForAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.DeleteTransactionUseCase
import javax.inject.Inject

/**
 * ViewModel for the Blotter screen (transaction list).
 * 
 * This ViewModel demonstrates modern MVVM architecture:
 * - Uses StateFlow for reactive UI state management
 * - Coordinates between multiple use cases
 * - Handles loading states, errors, and user actions
 * - Provides clear separation between business logic and UI logic
 * - Supports configuration changes seamlessly
 * - Uses domain models for clean architecture
 */
@HiltViewModel
class BlotterViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionsForAccountUseCase: GetTransactionsForAccountUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlotterUiState())
    val uiState: StateFlow<BlotterUiState> = _uiState.asStateFlow()

    /**
     * Public method to handle user actions.
     * This is the single entry point for all UI interactions.
     */
    fun handleAction(action: BlotterAction) {
        when (action) {
            is BlotterAction.LoadTransactions -> loadTransactions()
            is BlotterAction.RefreshTransactions -> refreshTransactions()
            is BlotterAction.RetryLoading -> retryLoading()
            is BlotterAction.SearchTransactions -> searchTransactions(action.query)
            is BlotterAction.FilterByAccount -> filterByAccount(action.accountId)
            is BlotterAction.DeleteTransaction -> deleteTransaction(action.transactionId)
            is BlotterAction.DuplicateTransaction -> duplicateTransaction(action.transactionId)
            is BlotterAction.EditTransaction -> editTransaction(action.transactionId)
            is BlotterAction.ClearFilter -> clearFilter()
            is BlotterAction.OpenFilter -> openFilter()
            is BlotterAction.CreateNewTransaction -> createNewTransaction()
            is BlotterAction.CreateNewTransfer -> createNewTransfer()
            is BlotterAction.DismissIntegrityError -> dismissIntegrityError()
            is BlotterAction.CalculateTotals -> calculateTotals()
        }
    }

    /**
     * Load transactions from the data source.
     * Updates the UI state with loading, success, or error states using sealed classes.
     */
    private fun loadTransactions() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(screenState = BlotterScreenState.Loading)
            
            try {
                // Use existing use cases which return TransactionEntity
                // Convert to domain models for UI presentation
                val dataEntities = if (_uiState.value.selectedAccountId > 0) {
                    getTransactionsForAccountUseCase.execute(_uiState.value.selectedAccountId)
                } else {
                    getTransactionsUseCase.execute()
                }
                
                if (dataEntities.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        screenState = BlotterScreenState.Empty
                    )
                    return@launch
                }
                
                // Convert data entities to UI presentation items
                val transactionItems = dataEntities.map { entity ->
                    BlotterTransactionItem(
                        id = entity.id,
                        accountId = entity.fromAccountId,
                        categoryName = if (entity.categoryId > 0) "Category ${entity.categoryId}" else "No Category", // TODO: Get actual category name
                        amount = entity.fromAmount.toString(),
                        formattedAmount = formatAmount(entity.fromAmount, entity.originalCurrencyId),
                        dateTime = entity.datetime,
                        formattedDate = formatDate(entity.datetime),
                        payee = if (entity.payeeId > 0) "Payee ${entity.payeeId}" else "", // TODO: Get actual payee name
                        note = entity.note ?: "",
                        fromAccountTitle = "Account ${entity.fromAccountId}", // TODO: Get actual account name
                        toAccountTitle = if (entity.toAccountId > 0) "Account ${entity.toAccountId}" else "", // TODO: Get actual account name
                        isTemplate = entity.isTemplate,
                        isTransfer = entity.toAccountId > 0,
                        categoryIconResId = getCategoryIcon(entity.categoryId),
                        accountIconResId = getAccountIcon(entity.fromAccountId),
                        currencySymbol = getCurrencySymbol(entity.originalCurrencyId)
                    )
                }
                
                val contentData = BlotterContentData(
                    transactions = transactionItems,
                    hasMoreItems = false, // TODO: Implement pagination
                    lastUpdateTime = System.currentTimeMillis()
                )
                
                _uiState.value = _uiState.value.copy(
                    screenState = BlotterScreenState.Content(contentData)
                )
                
                // Start total calculation
                calculateTotals()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = BlotterScreenState.Error(
                        message = "Failed to load transactions: ${e.message}",
                        exception = e,
                        canRetry = true
                    )
                )
            }
        }
    }

    private fun refreshTransactions() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadTransactions()
    }
    
    private fun retryLoading() {
        loadTransactions()
    }

    private fun searchTransactions(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // TODO: Implement search logic when search use case is available
        loadTransactions()
    }

    private fun filterByAccount(accountId: Long) {
        _uiState.value = _uiState.value.copy(
            selectedAccountId = accountId,
            isFilterActive = accountId > 0
        )
        loadTransactions()
    }

    private fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch(ioDispatcher) {
            try {
                deleteTransactionUseCase.execute(transactionId)
                loadTransactions() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = BlotterScreenState.Error(
                        message = "Failed to delete transaction: ${e.message}",
                        exception = e,
                        canRetry = false
                    )
                )
            }
        }
    }

    private fun duplicateTransaction(transactionId: Long) {
        // TODO: Implement duplicate logic when use case is available
    }

    private fun editTransaction(transactionId: Long) {
        // TODO: Navigate to edit transaction screen
    }

    private fun clearFilter() {
        _uiState.value = _uiState.value.copy(
            selectedAccountId = -1,
            isFilterActive = false,
            searchQuery = ""
        )
        loadTransactions()
    }

    private fun openFilter() {
        // TODO: Navigate to filter screen
    }

    private fun createNewTransaction() {
        // TODO: Navigate to create transaction screen
    }

    private fun createNewTransfer() {
        // TODO: Navigate to create transfer screen
    }
    
    private fun dismissIntegrityError() {
        _uiState.value = _uiState.value.copy(showIntegrityError = false)
    }
    
    private fun calculateTotals() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(
                totalCalculationState = TotalCalculationState.Calculating
            )
            
            try {
                // Get current transactions from content state
                val currentState = _uiState.value.screenState
                if (currentState is BlotterScreenState.Content) {
                    val totalAmount = calculateTotalAmount(currentState.data.transactions)
                    _uiState.value = _uiState.value.copy(
                        totalCalculationState = TotalCalculationState.Completed(
                            total = totalAmount,
                            warningMessage = null // TODO: Add currency warnings if needed
                        ),
                        totalAmount = totalAmount
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        totalCalculationState = TotalCalculationState.Completed(
                            total = "$0.00",
                            warningMessage = null
                        ),
                        totalAmount = "$0.00"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    totalCalculationState = TotalCalculationState.Failed(
                        error = "Failed to calculate totals: ${e.message}"
                    )
                )
            }
        }
    }

    // Helper functions for UI formatting
    private fun formatAmount(amount: Long, currencyId: Long): String {
        // TODO: Implement proper amount formatting with currency
        return "$${amount / 100}.${String.format("%02d", amount % 100)}"
    }

    private fun formatDate(timestamp: Long): String {
        // TODO: Implement proper date formatting
        return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }

    private fun getCategoryIcon(categoryId: Long): Int {
        // TODO: Implement category icon resolution
        return android.R.drawable.ic_menu_info_details
    }

    private fun getAccountIcon(accountId: Long): Int {
        // TODO: Implement account icon resolution  
        return android.R.drawable.ic_menu_save
    }

    private fun getCurrencySymbol(currencyId: Long): String {
        // TODO: Implement currency symbol resolution
        return "$"
    }

    private fun calculateTotalAmount(transactions: List<BlotterTransactionItem>): String {
        val total = transactions.sumOf { it.amount.toLongOrNull() ?: 0L }
        return formatAmount(total, 1) // Default currency for now
    }

    companion object {
        private const val TAG = "BlotterViewModel"
    }
}
