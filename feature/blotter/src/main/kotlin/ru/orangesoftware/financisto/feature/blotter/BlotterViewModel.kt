package ru.orangesoftware.financisto.feature.blotter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.usecase.modern.BlotterItem
import ru.orangesoftware.financisto.usecase.modern.GetBlotterAllAccountsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetBlotterForAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.ObserveBlotterForAccountUseCase
import javax.inject.Inject

/**
 * ViewModel for the Blotter screen following modern architecture principles.
 * 
 * The blotter is a list of transactions showing:
 * - Transaction details (date, amount, category, payee, note)
 * - Running balance after each transaction (for account-specific view)
 * - Ordered by most recent first
 * 
 * Architecture:
 * - Single Input interface for all user actions
 * - Single ViewData StateFlow for all UI state
 * - Delegates to use cases (never calls repositories directly)
 * - Uses domain models (BlotterItem) for presentation
 */
@HiltViewModel
class BlotterViewModel @Inject constructor(
    private val getBlotterForAccountUseCase: GetBlotterForAccountUseCase,
    private val getBlotterAllAccountsUseCase: GetBlotterAllAccountsUseCase,
    private val observeBlotterForAccountUseCase: ObserveBlotterForAccountUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _viewData = MutableStateFlow(ViewData())
    val viewData: StateFlow<ViewData> = _viewData.asStateFlow()

    /**
     * Sealed interface for all user inputs/actions.
     */
    sealed interface Input {
        /**
         * Load transactions for all accounts
         */
        data object LoadAllTransactions : Input
        
        /**
         * Load transactions for a specific account with running balance
         */
        data class LoadAccountTransactions(val accountId: Long) : Input
        
        /**
         * Refresh the current view
         */
        data object Refresh : Input
        
        /**
         * Navigate to transaction details
         */
        data class ShowTransactionDetails(val transactionId: Long) : Input
        
        /**
         * Navigate to edit transaction
         */
        data class EditTransaction(val transactionId: Long) : Input
        
        /**
         * Delete a transaction
         */
        data class DeleteTransaction(val transactionId: Long) : Input
        
        /**
         * Clear any error state
         */
        data object ClearError : Input
    }

    /**
     * Complete view state for the blotter screen.
     */
    data class ViewData(
        val items: List<BlotterItem> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null,
        val accountId: Long? = null, // null = all accounts, otherwise specific account
        val showRunningBalance: Boolean = false, // true when viewing single account
        val totalBalance: Long? = null, // Account balance (only for single account view)
        val formattedTotalBalance: String? = null // Formatted total balance with currency
    )

    /**
     * Single entry point for all user actions.
     */
    fun onInput(input: Input) {
        when (input) {
            is Input.LoadAllTransactions -> loadAllTransactions()
            is Input.LoadAccountTransactions -> loadAccountTransactions(input.accountId)
            Input.Refresh -> refresh()
            is Input.ShowTransactionDetails -> showTransactionDetails(input.transactionId)
            is Input.EditTransaction -> editTransaction(input.transactionId)
            is Input.DeleteTransaction -> deleteTransaction(input.transactionId)
            Input.ClearError -> clearError()
        }
    }

    /**
     * Load transactions for all accounts.
     */
    private fun loadAllTransactions() {
        viewModelScope.launch(ioDispatcher) {
            _viewData.value = _viewData.value.copy(
                isLoading = true,
                error = null,
                accountId = null,
                showRunningBalance = false
            )
            
            getBlotterAllAccountsUseCase.execute()
                .onSuccess { items ->
                    _viewData.value = _viewData.value.copy(
                        items = items,
                        isLoading = false,
                        isRefreshing = false,
                        totalBalance = null
                    )
                }
                .onFailure { exception ->
                    _viewData.value = _viewData.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Failed to load transactions: ${exception.message}"
                    )
                }
        }
    }

    /**
     * Load transactions for a specific account with running balance.
     */
    private fun loadAccountTransactions(accountId: Long) {
        viewModelScope.launch(ioDispatcher) {
            _viewData.value = _viewData.value.copy(
                isLoading = true,
                error = null,
                accountId = accountId,
                showRunningBalance = true
            )
            
            getBlotterForAccountUseCase.execute(
                accountId = accountId,
                ensureBalanceCalculated = false
            )
                .onSuccess { items ->
                    // Get total balance from first item (most recent transaction has current balance)
                    val totalBalance = items.firstOrNull()?.runningBalance
                    val formattedTotalBalance = items.firstOrNull()?.formattedRunningBalance
                    
                    _viewData.value = _viewData.value.copy(
                        items = items,
                        isLoading = false,
                        isRefreshing = false,
                        totalBalance = totalBalance,
                        formattedTotalBalance = formattedTotalBalance
                    )
                }
                .onFailure { exception ->
                    _viewData.value = _viewData.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Failed to load account transactions: ${exception.message}"
                    )
                }
        }
    }

    /**
     * Refresh the current view (reload whatever is currently showing).
     */
    private fun refresh() {
        val currentAccountId = _viewData.value.accountId
        
        _viewData.value = _viewData.value.copy(isRefreshing = true)
        
        if (currentAccountId != null) {
            loadAccountTransactions(currentAccountId)
        } else {
            loadAllTransactions()
        }
    }

    /**
     * Show transaction details.
     * This would typically navigate to a detail screen.
     */
    private fun showTransactionDetails(transactionId: Long) {
        // TODO: Implement navigation to transaction details
        // This would typically use a navigation component
    }

    /**
     * Edit a transaction.
     * This would typically navigate to the edit screen.
     */
    private fun editTransaction(transactionId: Long) {
        // TODO: Implement navigation to edit transaction
        // This would typically use a navigation component
    }

    /**
     * Delete a transaction.
     * This would typically show a confirmation dialog first.
     */
    private fun deleteTransaction(transactionId: Long) {
        // TODO: Implement delete with confirmation
        // This would use DeleteTransactionUseCase after confirmation
    }

    /**
     * Clear error state.
     */
    private fun clearError() {
        _viewData.value = _viewData.value.copy(error = null)
    }

    /**
     * Observe transactions for an account reactively.
     * Use this for real-time updates when transactions change.
     */
    fun observeAccountTransactions(accountId: Long) {
        viewModelScope.launch(ioDispatcher) {
            _viewData.value = _viewData.value.copy(
                isLoading = true,
                accountId = accountId,
                showRunningBalance = true
            )
            
            observeBlotterForAccountUseCase.execute(accountId)
                .catch { exception ->
                    _viewData.value = _viewData.value.copy(
                        isLoading = false,
                        error = "Failed to observe transactions: ${exception.message}"
                    )
                }
                .collect { items ->
                    val totalBalance = items.firstOrNull()?.runningBalance
                    val formattedTotalBalance = items.firstOrNull()?.formattedRunningBalance
                    
                    _viewData.value = _viewData.value.copy(
                        items = items,
                        isLoading = false,
                        isRefreshing = false,
                        totalBalance = totalBalance,
                        formattedTotalBalance = formattedTotalBalance,
                        error = null
                    )
                }
        }
    }
}
