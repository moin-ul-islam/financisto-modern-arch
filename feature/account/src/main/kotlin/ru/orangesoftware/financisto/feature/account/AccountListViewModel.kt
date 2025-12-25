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
import ru.orangesoftware.financisto.domain.model.Currency
import ru.orangesoftware.financisto.domain.model.CurrencyId
import ru.orangesoftware.financisto.domain.model.Money
import ru.orangesoftware.financisto.domain.model.SymbolFormat
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetAccountByIdUseCase
import ru.orangesoftware.financisto.usecase.modern.DeleteAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.UpdateAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.GetCurrenciesUseCase
import ru.orangesoftware.financisto.usecase.modern.GetCurrencyByIdUseCase
import ru.orangesoftware.financisto.usecase.modern.GetHomeCurrencyUseCase
import ru.orangesoftware.financisto.usecase.modern.CalculateTotalInHomeCurrencyUseCase
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
    private val getCurrencyByIdUseCase: GetCurrencyByIdUseCase,
    private val getHomeCurrencyUseCase: GetHomeCurrencyUseCase,
    private val calculateTotalInHomeCurrencyUseCase: CalculateTotalInHomeCurrencyUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountListUiState())
    val uiState: StateFlow<AccountListUiState> = _uiState.asStateFlow()

    // Cache for currencies to avoid repeated database calls
    private val currencyCache = mutableMapOf<Long, Currency>()

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
                
                // Load all currencies upfront to avoid repeated database calls
                val uniqueCurrencyIds = accounts.map { it.currencyId }.distinct()
                val currencyMap = mutableMapOf<Long, Currency>()
                
                uniqueCurrencyIds.forEach { currencyId ->
                    val currency = getCurrency(currencyId)
                    if (currency != null) {
                        currencyMap[currencyId] = currency
                    }
                }
                
                val accountItems = accounts.map { account ->
                    val currency = currencyMap[account.currencyId]
                    val formattedBalance = if (currency != null) {
                        val money = Money(account.totalAmount)
                        currency.formatAmount(money)
                    } else {
                        // Fallback formatting
                        "$${account.totalAmount / 100}.${String.format("%02d", account.totalAmount % 100)}"
                    }
                    
                    val currencySymbol = currency?.symbol ?: "$"
                    
                    AccountListItem(
                        id = account.id,
                        title = account.title,
                        balance = account.totalAmount.toString(),
                        formattedBalance = formattedBalance,
                        currencySymbol = currencySymbol,
                        currencyId = account.currencyId, // Include currency ID
                        accountType = getAccountTypeDisplayName(account.type),
                        iconResId = getAccountTypeIcon(account.type),
                        isActive = account.isActive,
                        isIncludeIntoTotals = account.isIncludeIntoTotals,
                        lastTransactionDate = account.lastTransactionDate,
                        formattedLastTransactionDate = formatDate(account.lastTransactionDate),
                        transactionCount = 0, // TODO: Get actual transaction count from use case
                        note = account.note ?: "",
                        balanceAmount = account.totalAmount // Add raw balance amount
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
                    // Get home currency
                    val homeCurrency = getHomeCurrencyUseCase.execute()
                    
                    if (homeCurrency != null) {
                        // Prepare account balances for calculation
                        val accountBalances = currentState.data.accounts.map { account ->
                            CalculateTotalInHomeCurrencyUseCase.AccountBalance(
                                amount = account.balance.toLongOrNull() ?: 0L,
                                currencyId = getCurrencyIdForAccount(account),
                                includeInTotals = account.isIncludeIntoTotals
                            )
                        }
                        
                        // Calculate total in home currency
                        val totalResult = calculateTotalInHomeCurrencyUseCase.execute(accountBalances)
                        
                        if (totalResult != null) {
                            val formattedTotal = formatAmountWithCurrency(totalResult.total, homeCurrency)
                            val warningMessage = if (totalResult.hasConversionWarnings) {
                                "Some accounts could not be converted. Exchange rates may be missing."
                            } else null
                            
                            val updatedContentData = currentState.data.copy(totalBalance = formattedTotal)
                            _uiState.value = _uiState.value.copy(
                                totalCalculationState = TotalCalculationState.Completed(
                                    total = formattedTotal,
                                    warningMessage = warningMessage
                                ),
                                screenState = AccountListScreenState.Content(updatedContentData)
                            )
                        } else {
                            // No home currency set
                            _uiState.value = _uiState.value.copy(
                                totalCalculationState = TotalCalculationState.Completed(
                                    total = "Set Home Currency",
                                    warningMessage = "Please set a home currency in settings"
                                )
                            )
                        }
                    } else {
                        // No home currency set
                        val updatedContentData = currentState.data.copy(totalBalance = "Set Home Currency")
                        _uiState.value = _uiState.value.copy(
                            totalCalculationState = TotalCalculationState.Completed(
                                total = "Set Home Currency",
                                warningMessage = "Please set a home currency in settings"
                            ),
                            screenState = AccountListScreenState.Content(updatedContentData)
                        )
                    }
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
    private suspend fun formatAmount(amount: Long, currencyId: Long): String {
        val currency = getCurrency(currencyId)
        return if (currency != null) {
            val money = Money(amount)
            currency.formatAmount(money)
        } else {
            // Fallback to basic formatting if currency not found
            "$${amount / 100}.${String.format("%02d", amount % 100)}"
        }
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "Never"
        return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }

    private suspend fun getCurrencySymbol(currencyId: Long): String {
        val currency = getCurrency(currencyId)
        return currency?.symbol ?: "$"
    }

    private suspend fun getCurrency(currencyId: Long): Currency? {
        // Check cache first
        currencyCache[currencyId]?.let { return it }

        // Load from database if not in cache
        val currencyEntity = getCurrencyByIdUseCase.execute(currencyId)
        if (currencyEntity != null) {
            val currency = Currency(
                id = CurrencyId(currencyEntity.id),
                name = currencyEntity.name,
                title = currencyEntity.title,
                symbol = currencyEntity.symbol,
                isDefault = currencyEntity.isDefault,
                decimals = currencyEntity.decimals,
                decimalSeparator = currencyEntity.decimalSeparator,
                groupSeparator = currencyEntity.groupSeparator,
                symbolFormat = SymbolFormat.valueOf(currencyEntity.symbolFormat),
                isActive = true // Default to active since entity doesn't have this field
            )
            // Cache the currency
            currencyCache[currencyId] = currency
            return currency
        }

        return null
    }

    private fun getAccountTypeDisplayName(type: String): String {
        // TODO: Implement account type display name mapping
        return type.capitalize()
    }

    private fun getAccountTypeIcon(type: String): Int {
        // TODO: Implement account type icon mapping
        return android.R.drawable.ic_menu_save
    }

    private suspend fun getCurrencyIdForAccount(account: AccountListItem): Long {
        // Currency ID is now stored directly in AccountListItem
        return account.currencyId
    }
    
    private fun formatAmountWithCurrency(amount: Long, currency: ru.orangesoftware.financisto.data.model.CurrencyEntity): String {
        // Format the amount using currency formatting rules
        val absAmount = kotlin.math.abs(amount) / 100.0
        val sign = if (amount < 0) "-" else ""
        val formattedAmount = String.format("%.${currency.decimals}f", absAmount)
        
        return when {
            currency.symbolFormat == "RS" -> "$sign${currency.symbol}$formattedAmount"
            currency.symbolFormat == "LS" -> "$sign$formattedAmount${currency.symbol}"
            currency.symbolFormat == "RSP" -> "$sign${currency.symbol} $formattedAmount"
            currency.symbolFormat == "LSP" -> "$sign$formattedAmount ${currency.symbol}"
            else -> "$sign${currency.symbol}$formattedAmount"
        }
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
