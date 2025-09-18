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
import ru.orangesoftware.financisto.usecase.modern.UpdateAccountUseCase
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
    private val updateAccountUseCase: UpdateAccountUseCase,
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
            is AccountListAction.RetryLoading -> retryLoading()
            is AccountListAction.EditAccount -> editAccount(action.accountId)
            is AccountListAction.DeleteAccount -> deleteAccount(action.accountId)
            is AccountListAction.ToggleAccountStatus -> toggleAccountStatus(action.accountId)
            is AccountListAction.ViewAccountTransactions -> viewAccountTransactions(action.accountId)
            is AccountListAction.SortBy -> sortBy(action.sortOrder)
            is AccountListAction.ShowAccountInfo -> showAccountInfo(action.accountId)
            is AccountListAction.UpdateAccountBalance -> updateAccountBalance(action.accountId)
            is AccountListAction.PurgeAccount -> purgeAccount(action.accountId)
            is AccountListAction.CreateNewAccount -> createNewAccount()
            is AccountListAction.ViewAccountTotals -> viewAccountTotals()
            is AccountListAction.IntegrityCheck -> performIntegrityCheck()
            is AccountListAction.DismissIntegrityError -> dismissIntegrityError()
            is AccountListAction.CalculateTotals -> calculateTotals()
            is AccountListAction.DismissAccountInfoDialog -> dismissAccountInfoDialog()
            is AccountListAction.DismissDeleteConfirmDialog -> dismissDeleteConfirmDialog()
            is AccountListAction.ConfirmDeleteAccount -> confirmDeleteAccount(action.accountId)
        }
    }

    /**
     * Load accounts from the data source.
     * Updates the UI state with loading, success, or error states using sealed classes.
     */
    private fun loadAccounts() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(screenState = AccountListScreenState.Loading)
            
            try {
                val accounts = getAccountsUseCase.execute()
                
                if (accounts.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        screenState = AccountListScreenState.Empty
                    )
                    return@launch
                }
                
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
                
                val contentData = AccountListContentData(
                    accounts = sortedAccounts,
                    lastUpdateTime = System.currentTimeMillis()
                )
                
                _uiState.value = _uiState.value.copy(
                    screenState = AccountListScreenState.Content(contentData)
                )
                
                // Start total calculation
                calculateTotals()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = AccountListScreenState.Error(
                        message = "Failed to load accounts: ${e.message}",
                        exception = e,
                        canRetry = true
                    )
                )
            }
        }
    }

    private fun refreshAccounts() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadAccounts()
    }
    
    private fun retryLoading() {
        loadAccounts()
    }

    private fun editAccount(accountId: Long) {
        // TODO: Navigate to edit account screen
    }

    private fun deleteAccount(accountId: Long) {
        // Show confirmation dialog instead of immediately deleting
        viewModelScope.launch(ioDispatcher) {
            try {
                // Find the account in current list to show in confirmation dialog
                val currentState = _uiState.value.screenState
                if (currentState is AccountListScreenState.Content) {
                    val accountToDelete = currentState.data.accounts.find { it.id == accountId }
                    if (accountToDelete != null) {
                        _uiState.value = _uiState.value.copy(
                            showDeleteConfirmDialog = true,
                            accountToDelete = accountToDelete
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = AccountListScreenState.Error(
                        message = "Failed to prepare delete confirmation: ${e.message}",
                        exception = e,
                        canRetry = false
                    )
                )
            }
        }
    }

    private fun toggleAccountStatus(accountId: Long) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val account = getAccountByIdUseCase.execute(accountId)
                if (account != null) {
                    // Toggle the account active status
                    val updatedAccount = account.copy(isActive = !account.isActive)
                    updateAccountUseCase.execute(updatedAccount)
                    loadAccounts() // Refresh the list
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = AccountListScreenState.Error(
                        message = "Failed to update account: ${e.message}",
                        exception = e,
                        canRetry = false
                    )
                )
            }
        }
    }

    private fun viewAccountTransactions(accountId: Long) {
        // TODO: Navigate to account transactions (Blotter with account filter)
    }

    private fun sortBy(sortOrder: AccountSortOrder) {
        val currentState = _uiState.value.screenState
        if (currentState is AccountListScreenState.Content) {
            val sortedAccounts = sortAccounts(currentState.data.accounts, sortOrder)
            val updatedContentData = currentState.data.copy(accounts = sortedAccounts)
            
            _uiState.value = _uiState.value.copy(
                screenState = AccountListScreenState.Content(updatedContentData),
                selectedSortOrder = sortOrder
            )
        }
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
    
    private fun showAccountInfo(accountId: Long) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val account = getAccountByIdUseCase.execute(accountId)
                if (account != null) {
                    val accountInfoData = AccountInfoData(
                        accountId = account.id,
                        title = account.title,
                        accountType = getAccountTypeDisplayName(account.type),
                        currency = getCurrencySymbol(account.currencyId),
                        balance = account.totalAmount.toString(),
                        formattedBalance = formatAmount(account.totalAmount, account.currencyId),
                        issuer = account.issuer,
                        cardNumber = account.number,
                        note = account.note,
                        isActive = account.isActive,
                        lastTransactionDate = formatDate(account.lastTransactionDate)
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        showAccountInfoDialog = true,
                        accountInfoData = accountInfoData
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = AccountListScreenState.Error(
                        message = "Failed to load account info: ${e.message}",
                        exception = e,
                        canRetry = false
                    )
                )
            }
        }
    }
    
    private fun updateAccountBalance(accountId: Long) {
        // TODO: Implement account balance update when use case is available
    }
    
    private fun purgeAccount(accountId: Long) {
        // TODO: Implement account purge when use case is available
    }
    
    private fun dismissIntegrityError() {
        _uiState.value = _uiState.value.copy(showIntegrityError = false)
    }
    
    private fun dismissAccountInfoDialog() {
        _uiState.value = _uiState.value.copy(
            showAccountInfoDialog = false,
            accountInfoData = null
        )
    }
    
    private fun dismissDeleteConfirmDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmDialog = false,
            accountToDelete = null
        )
    }
    
    private fun confirmDeleteAccount(accountId: Long) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val result = deleteAccountUseCase.execute(accountId)
                if (result.isSuccess) {
                    // Dismiss dialog and refresh the list
                    _uiState.value = _uiState.value.copy(
                        showDeleteConfirmDialog = false,
                        accountToDelete = null
                    )
                    loadAccounts() // Refresh the list
                } else {
                    _uiState.value = _uiState.value.copy(
                        screenState = AccountListScreenState.Error(
                            message = "Failed to delete account: ${result.exceptionOrNull()?.message}",
                            exception = result.exceptionOrNull(),
                            canRetry = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = AccountListScreenState.Error(
                        message = "Failed to delete account: ${e.message}",
                        exception = e,
                        canRetry = false
                    )
                )
            }
        }
    }
    
    private fun calculateTotals() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(
                totalCalculationState = TotalCalculationState.Calculating
            )
            
            try {
                // Get current accounts from content state
                val currentState = _uiState.value.screenState
                if (currentState is AccountListScreenState.Content) {
                    val totalBalance = calculateTotalBalance(currentState.data.accounts)
                    val updatedContentData = currentState.data.copy(totalBalance = totalBalance)
                    _uiState.value = _uiState.value.copy(
                        totalCalculationState = TotalCalculationState.Completed(
                            total = totalBalance,
                            warningMessage = null // TODO: Add currency warnings if needed
                        ),
                        screenState = AccountListScreenState.Content(updatedContentData)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        totalCalculationState = TotalCalculationState.Completed(
                            total = "$0.00",
                            warningMessage = null
                        )
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
