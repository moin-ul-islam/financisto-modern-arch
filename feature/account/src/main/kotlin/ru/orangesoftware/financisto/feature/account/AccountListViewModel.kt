package ru.orangesoftware.financisto.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetAccountByIdUseCase
import ru.orangesoftware.financisto.usecase.modern.DeleteAccountUseCase
import javax.inject.Inject

/**
 * ViewModel for the Account List screen.
 * 
 * This ViewModel demonstrates modern MVVM architecture:
 * - Uses StateFlow for reactive UI state management
 * - Coordinates between multiple use cases
 * - Handles loading states, errors, and user actions
 * - Provides clear separation between business logic and UI logic
 * - Supports configuration changes seamlessly
 * - Uses existing use cases that return data entities for Phase 4.1
 */
@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountListUiState())
    val uiState: StateFlow<AccountListUiState> = _uiState.asStateFlow()

    /**
     * Public method to handle user actions.
     * This is the single entry point for all UI interactions.
     */
    fun handleAction(action: AccountListAction) {
        when (action) {
            is AccountListAction.LoadAccounts -> loadAccounts()
            is AccountListAction.RefreshAccounts -> refreshAccounts()
            is AccountListAction.EditAccount -> editAccount(action.accountId)
            is AccountListAction.DeleteAccount -> deleteAccount(action.accountId)
            is AccountListAction.ToggleAccountStatus -> toggleAccountStatus(action.accountId)
            is AccountListAction.ViewAccountTransactions -> viewAccountTransactions(action.accountId)
            is AccountListAction.SortBy -> sortBy(action.sortOrder)
            is AccountListAction.CreateNewAccount -> createNewAccount()
            is AccountListAction.ViewAccountTotals -> viewAccountTotals()
            is AccountListAction.IntegrityCheck -> performIntegrityCheck()
        }
    }

    /**
     * Load accounts from the data source.
     * Updates the UI state with loading, success, or error states.
     * 
     * Note: Uses existing use cases that return AccountEntity.
     * Future phases will migrate to pure domain models.
     */
    private fun loadAccounts() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val accounts = getAccountsUseCase.execute()
                
                val accountItems = accounts.map { account ->
                    AccountListItem(
                        id = account.id,
                        title = account.title,
                        balance = account.totalAmount.toString(),
                        formattedBalance = formatAmount(account.totalAmount, account.currencyId),
                        currencySymbol = getCurrencySymbol(account.currencyId),
                        accountType = getAccountTypeDisplayName(account.type),
                        iconResId = getAccountTypeIcon(account.type),
                        isActive = account.isActive,
                        isIncludeIntoTotals = account.isIncludeIntoTotals,
                        lastTransactionDate = account.lastTransactionDate,
                        formattedLastTransactionDate = formatDate(account.lastTransactionDate),
                        transactionCount = 0, // TODO: Get actual transaction count from use case
                        note = account.note ?: ""
                    )
                }
                
                val sortedAccounts = sortAccounts(accountItems, _uiState.value.selectedSortOrder)
                val totalBalance = calculateTotalBalance(accountItems)
                
                _uiState.value = _uiState.value.copy(
                    accounts = sortedAccounts,
                    totalBalance = totalBalance,
                    isLoading = false,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load accounts: ${e.message}"
                )
            }
        }
    }

    private fun refreshAccounts() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadAccounts()
    }

    private fun editAccount(accountId: Long) {
        // TODO: Navigate to edit account screen
    }

    private fun deleteAccount(accountId: Long) {
        viewModelScope.launch(ioDispatcher) {
            try {
                deleteAccountUseCase.execute(accountId)
                loadAccounts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete account: ${e.message}"
                )
            }
        }
    }

    private fun toggleAccountStatus(accountId: Long) {
        // TODO: Implement toggle account status when use case is available
        viewModelScope.launch(ioDispatcher) {
            try {
                val account = getAccountByIdUseCase.execute(accountId)
                if (account != null) {
                    // TODO: Update account status
                    loadAccounts() // Refresh the list
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update account: ${e.message}"
                )
            }
        }
    }

    private fun viewAccountTransactions(accountId: Long) {
        // TODO: Navigate to account transactions (Blotter with account filter)
    }

    private fun sortBy(sortOrder: AccountSortOrder) {
        val currentAccounts = _uiState.value.accounts
        val sortedAccounts = sortAccounts(currentAccounts, sortOrder)
        
        _uiState.value = _uiState.value.copy(
            accounts = sortedAccounts,
            selectedSortOrder = sortOrder
        )
    }

    private fun createNewAccount() {
        // TODO: Navigate to create account screen
    }

    private fun viewAccountTotals() {
        // TODO: Navigate to account totals screen
    }

    private fun performIntegrityCheck() {
        // TODO: Implement integrity check when use case is available
    }

    // Helper functions for UI formatting and business logic
    private fun formatAmount(amount: Long, currencyId: Long): String {
        // TODO: Implement proper amount formatting with currency
        return "$${amount / 100}.${String.format("%02d", amount % 100)}"
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "Never"
        return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }

    private fun getCurrencySymbol(currencyId: Long): String {
        // TODO: Implement currency symbol resolution
        return "$"
    }

    private fun getAccountTypeDisplayName(type: String): String {
        // TODO: Implement account type display name mapping
        return type.capitalize()
    }

    private fun getAccountTypeIcon(type: String): Int {
        // TODO: Implement account type icon mapping
        return android.R.drawable.ic_menu_save
    }

    private fun calculateTotalBalance(accounts: List<AccountListItem>): String {
        val total = accounts
            .filter { it.isIncludeIntoTotals }
            .sumOf { it.balance.toLongOrNull() ?: 0L }
        return formatAmount(total, 1) // Default currency for now
    }

    private fun sortAccounts(accounts: List<AccountListItem>, sortOrder: AccountSortOrder): List<AccountListItem> {
        return when (sortOrder) {
            AccountSortOrder.NAME -> accounts.sortedBy { it.title }
            AccountSortOrder.BALANCE -> accounts.sortedByDescending { it.balance.toLongOrNull() ?: 0L }
            AccountSortOrder.TYPE -> accounts.sortedBy { it.accountType }
            AccountSortOrder.LAST_TRANSACTION_DATE -> accounts.sortedByDescending { it.lastTransactionDate }
        }
    }

    companion object {
        private const val TAG = "AccountListViewModel"
    }
}
