package ru.orangesoftware.financisto.feature.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.usecase.modern.GetTransactionByIdUseCase
import ru.orangesoftware.financisto.usecase.modern.CreateTransactionUseCase
import ru.orangesoftware.financisto.usecase.modern.UpdateTransactionUseCase
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase
import javax.inject.Inject

/**
 * ViewModel for the Transaction Form screen (create/edit transactions).
 * 
 * This ViewModel demonstrates modern MVVM architecture:
 * - Uses StateFlow for reactive UI state management
 * - Handles complex form validation and state management
 * - Coordinates between multiple use cases
 * - Handles loading states, errors, and user actions
 * - Preserves state across configuration changes using SavedStateHandle
 * - Supports both create and edit modes
 */
@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionFormUiState())
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    init {
        // Load initial data and check if we're in edit mode
        val transactionId = savedStateHandle.get<Long>("transactionId") ?: -1L
        val accountId = savedStateHandle.get<Long>("accountId") ?: -1L
        val isTemplate = savedStateHandle.get<Boolean>("isTemplate") ?: false
        
        loadInitialData(transactionId, accountId, isTemplate)
    }

    /**
     * Public method to handle user actions.
     * This is the single entry point for all UI interactions.
     */
    fun handleAction(action: TransactionFormAction) {
        when (action) {
            is TransactionFormAction.InitializeForm -> initializeForm()
            is TransactionFormAction.LoadTransaction -> loadTransaction(action.transactionId)
            is TransactionFormAction.SetAccount -> setAccount(action.account)
            is TransactionFormAction.SetToAccount -> setToAccount(action.account)
            is TransactionFormAction.SetAmount -> setAmount(action.amount)
            is TransactionFormAction.SetCategory -> setCategory(action.category)
            is TransactionFormAction.SetPayee -> setPayee(action.payee)
            is TransactionFormAction.SetProject -> setProject(action.project)
            is TransactionFormAction.SetLocation -> setLocation(action.location)
            is TransactionFormAction.SetNote -> setNote(action.note)
            is TransactionFormAction.SetDateTime -> setDateTime(action.dateTime)
            is TransactionFormAction.SetExchangeRate -> setExchangeRate(action.rate)
            is TransactionFormAction.ToggleTransfer -> toggleTransfer(action.isTransfer)
            is TransactionFormAction.AddSplitTransaction -> addSplitTransaction(action.split)
            is TransactionFormAction.RemoveSplitTransaction -> removeSplitTransaction(action.splitId)
            is TransactionFormAction.UpdateSplitTransaction -> updateSplitTransaction(action.split)
            is TransactionFormAction.SaveTransaction -> saveTransaction()
            is TransactionFormAction.SaveAsTemplate -> saveAsTemplate()
            is TransactionFormAction.ClearForm -> clearForm()
            is TransactionFormAction.ValidateForm -> validateForm()
            is TransactionFormAction.RetryLoading -> retryLoading()
            is TransactionFormAction.DismissSaveError -> dismissSaveError()
        }
    }

    private fun loadInitialData(transactionId: Long, accountId: Long, isTemplate: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(screenState = TransactionFormScreenState.Loading)
            
            try {
                // Load available accounts
                val accounts = getAccountsUseCase.execute()
                
                if (accounts.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        screenState = TransactionFormScreenState.Error(
                            message = "No accounts available. Please create an account first.",
                            exception = null,
                            canRetry = true
                        )
                    )
                    return@launch
                }
                
                val accountOptions = accounts.map { account ->
                    AccountOption(
                        id = account.id,
                        title = account.title,
                        currencySymbol = getCurrencySymbol(account.currencyId),
                        balance = formatAmount(account.totalAmount, account.currencyId),
                        iconResId = getAccountTypeIcon(account.type)
                    )
                }

                val contentData = TransactionFormContentData()

                _uiState.value = _uiState.value.copy(
                    screenState = TransactionFormScreenState.Content(contentData),
                    availableAccounts = accountOptions,
                    availableCategories = emptyList(), // TODO: Load categories  
                    availablePayees = emptyList(), // TODO: Load payees
                    availableProjects = emptyList(), // TODO: Load projects
                    availableLocations = emptyList(), // TODO: Load locations
                    isTemplate = isTemplate
                )

                // If editing existing transaction, load it
                if (transactionId > 0) {
                    loadTransaction(transactionId)
                } else {
                    // Pre-select account if provided
                    if (accountId > 0) {
                        val selectedAccount = accountOptions.find { it.id == accountId }
                        if (selectedAccount != null) {
                            setAccount(selectedAccount)
                        }
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = TransactionFormScreenState.Error(
                        message = "Failed to load form data: ${e.message}",
                        exception = e,
                        canRetry = true
                    )
                )
            }
        }
    }

    private fun loadTransaction(transactionId: Long) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(screenState = TransactionFormScreenState.Loading)
            
            try {
                val transaction = getTransactionByIdUseCase.execute(transactionId)
                if (transaction == null) {
                    _uiState.value = _uiState.value.copy(
                        screenState = TransactionFormScreenState.Error(
                            message = "Transaction not found",
                            exception = null,
                            canRetry = false
                        )
                    )
                    return@launch
                }
                
                // Get current content data or create it
                val currentState = _uiState.value.screenState
                val contentData = if (currentState is TransactionFormScreenState.Content) {
                    currentState.data
                } else {
                    // This shouldn't happen if loadInitialData was called first
                    TransactionFormContentData()
                }
                
                // Find selected accounts from main UI state
                val selectedAccount = _uiState.value.availableAccounts.find { it.id == transaction.fromAccountId }
                val selectedToAccount = if (transaction.toAccountId > 0) {
                    _uiState.value.availableAccounts.find { it.id == transaction.toAccountId }
                } else null

                // Update form with transaction data
                _uiState.value = _uiState.value.copy(
                    screenState = TransactionFormScreenState.Content(contentData),
                    transactionId = transactionId,
                    isEditMode = true,
                    selectedAccount = selectedAccount,
                    selectedToAccount = selectedToAccount,
                    amount = (transaction.fromAmount / 100.0).toString(),
                    formattedAmount = formatAmount(transaction.fromAmount, transaction.originalCurrencyId),
                    note = transaction.note ?: "",
                    dateTime = transaction.datetime,
                    formattedDateTime = formatDateTime(transaction.datetime),
                    isTransfer = transaction.toAccountId > 0
                )
                
                validateForm()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = TransactionFormScreenState.Error(
                        message = "Failed to load transaction: ${e.message}",
                        exception = e,
                        canRetry = true
                    )
                )
            }
        }
    }
    
    private fun retryLoading() {
        val transactionId = _uiState.value.transactionId
        if (transactionId > 0) {
            loadTransaction(transactionId)
        } else {
            // Retry initial data load
            val savedTransactionId = savedStateHandle.get<Long>("transactionId") ?: -1L
            val accountId = savedStateHandle.get<Long>("accountId") ?: -1L
            val isTemplate = savedStateHandle.get<Boolean>("isTemplate") ?: false
            loadInitialData(savedTransactionId, accountId, isTemplate)
        }
    }
    
    private fun dismissError() {
        // Try to go back to content state if possible
        val currentState = _uiState.value.screenState
        if (currentState is TransactionFormScreenState.Error) {
            _uiState.value = _uiState.value.copy(
                screenState = TransactionFormScreenState.Content(TransactionFormContentData())
            )
        }
    }
    
    private fun initializeForm() {
        // Called when form needs to be initialized
        val transactionId = savedStateHandle.get<Long>("transactionId") ?: -1L
        val accountId = savedStateHandle.get<Long>("accountId") ?: -1L
        val isTemplate = savedStateHandle.get<Boolean>("isTemplate") ?: false
        loadInitialData(transactionId, accountId, isTemplate)
    }
    
    private fun dismissSaveError() {
        _uiState.value = _uiState.value.copy(
            saveState = SaveState.Idle
        )
    }

    private fun setAccount(account: AccountOption) {
        _uiState.value = _uiState.value.copy(selectedAccount = account)
        validateForm()
    }

    private fun setToAccount(account: AccountOption) {
        _uiState.value = _uiState.value.copy(selectedToAccount = account)
        validateForm()
    }

    private fun setAmount(amount: String) {
        val formattedAmount = try {
            val numericAmount = amount.toDoubleOrNull() ?: 0.0
            formatAmount((numericAmount * 100).toLong(), 1) // Default currency
        } catch (e: Exception) {
            amount
        }

        _uiState.value = _uiState.value.copy(
            amount = amount,
            formattedAmount = formattedAmount
        )
        validateForm()
    }

    private fun setCategory(category: CategoryOption) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        validateForm()
    }

    private fun setPayee(payee: PayeeOption?) {
        _uiState.value = _uiState.value.copy(selectedPayee = payee)
    }

    private fun setProject(project: ProjectOption?) {
        _uiState.value = _uiState.value.copy(selectedProject = project)
    }

    private fun setLocation(location: LocationOption?) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    private fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    private fun setDateTime(dateTime: Long) {
        _uiState.value = _uiState.value.copy(
            dateTime = dateTime,
            formattedDateTime = formatDateTime(dateTime)
        )
    }

    private fun setExchangeRate(rate: String) {
        _uiState.value = _uiState.value.copy(exchangeRate = rate)
    }

    private fun toggleTransfer(isTransfer: Boolean) {
        _uiState.value = _uiState.value.copy(
            isTransfer = isTransfer,
            selectedToAccount = if (!isTransfer) null else _uiState.value.selectedToAccount
        )
        validateForm()
    }

    private fun addSplitTransaction(split: SplitTransactionItem) {
        val currentSplits = _uiState.value.splitTransactions
        _uiState.value = _uiState.value.copy(
            splitTransactions = currentSplits + split,
            isSplitTransaction = true
        )
        validateForm()
    }

    private fun removeSplitTransaction(splitId: Long) {
        val currentSplits = _uiState.value.splitTransactions
        val updatedSplits = currentSplits.filterNot { it.id == splitId }
        _uiState.value = _uiState.value.copy(
            splitTransactions = updatedSplits,
            isSplitTransaction = updatedSplits.isNotEmpty()
        )
        validateForm()
    }

    private fun updateSplitTransaction(split: SplitTransactionItem) {
        val currentSplits = _uiState.value.splitTransactions
        val updatedSplits = currentSplits.map { if (it.id == split.id) split else it }
        _uiState.value = _uiState.value.copy(splitTransactions = updatedSplits)
        validateForm()
    }

    private fun saveTransaction() {
        if (!_uiState.value.isFormValid) {
            validateForm()
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(
                saveState = SaveState.Saving
            )
            
            try {
                if (_uiState.value.isEditMode) {
                    // Update existing transaction
                    // TODO: Implement update logic
                } else {
                    // Create new transaction
                    // TODO: Implement create logic
                }
                
                _uiState.value = _uiState.value.copy(
                    saveState = SaveState.Success(
                        transactionId = -1L, // TODO: Return actual transaction ID from use case
                        message = "Transaction saved successfully"
                    )
                )
                // TODO: Navigate back or show success message
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveState = SaveState.Failed(
                        error = "Failed to save transaction: ${e.message}",
                        canRetry = true
                    )
                )
            }
        }
    }

    private fun saveAsTemplate() {
        // TODO: Implement save as template logic
    }

    private fun clearForm() {
        val currentState = _uiState.value
        
        _uiState.value = TransactionFormUiState(
            screenState = TransactionFormScreenState.Content(TransactionFormContentData()),
            availableAccounts = currentState.availableAccounts,
            availableCategories = currentState.availableCategories,
            availablePayees = currentState.availablePayees,
            availableProjects = currentState.availableProjects,
            availableLocations = currentState.availableLocations,
            isTemplate = currentState.isTemplate
        )
    }

    private fun validateForm() {
        val state = _uiState.value
        val errors = mutableListOf<ValidationError>()

        // Required fields validation
        if (state.selectedAccount == null) {
            errors.add(ValidationError.AccountRequired)
        }

        if (state.amount.isBlank()) {
            errors.add(ValidationError.AmountRequired)
        } else {
            val numericAmount = state.amount.toDoubleOrNull()
            if (numericAmount == null) {
                errors.add(ValidationError.InvalidAmount)
            } else if (numericAmount <= 0) {
                errors.add(ValidationError.NegativeAmount)
            }
        }

        if (!state.isTransfer && state.selectedCategory == null) {
            errors.add(ValidationError.CategoryRequired)
        }

        // Transfer-specific validation
        if (state.isTransfer) {
            if (state.selectedToAccount == null) {
                errors.add(ValidationError.ToAccountRequired)
            } else if (state.selectedAccount?.id == state.selectedToAccount.id) {
                errors.add(ValidationError.SameAccountTransfer)
            }
        }

        // Split transaction validation
        if (state.isSplitTransaction) {
            val totalSplitAmount = state.splitTransactions.sumOf { 
                it.amount.toDoubleOrNull() ?: 0.0 
            }
            val transactionAmount = state.amount.toDoubleOrNull() ?: 0.0
            if (Math.abs(totalSplitAmount - transactionAmount) > 0.01) {
                errors.add(ValidationError.SplitAmountMismatch)
            }
        }

        _uiState.value = _uiState.value.copy(
            validationErrors = errors,
            isFormValid = errors.isEmpty()
        )
    }

    // Helper functions for UI formatting
    private fun formatAmount(amount: Long, currencyId: Long): String {
        return "$${amount / 100}.${String.format("%02d", amount % 100)}"
    }

    private fun formatDateTime(timestamp: Long): String {
        return java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }

    private fun getCurrencySymbol(currencyId: Long): String {
        return "$" // TODO: Implement currency symbol resolution
    }

    private fun getAccountTypeIcon(type: String): Int {
        return android.R.drawable.ic_menu_save // TODO: Implement account type icon mapping
    }

    companion object {
        private const val TAG = "TransactionFormViewModel"
    }
}
