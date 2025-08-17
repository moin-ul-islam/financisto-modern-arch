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
        }
    }

    private fun loadInitialData(transactionId: Long, accountId: Long, isTemplate: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load available accounts
                val accounts = getAccountsUseCase.execute()
                val accountOptions = accounts.map { account ->
                    AccountOption(
                        id = account.id,
                        title = account.title,
                        currencySymbol = getCurrencySymbol(account.currencyId),
                        balance = formatAmount(account.totalAmount, account.currencyId),
                        iconResId = getAccountTypeIcon(account.type)
                    )
                }

                _uiState.value = _uiState.value.copy(
                    availableAccounts = accountOptions,
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
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load form data: ${e.message}"
                )
            }
        }
    }

    private fun loadTransaction(transactionId: Long) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val transaction = getTransactionByIdUseCase.execute(transactionId)
                if (transaction != null) {
                    // Populate form with transaction data
                    val selectedAccount = _uiState.value.availableAccounts.find { it.id == transaction.fromAccountId }
                    val selectedToAccount = if (transaction.toAccountId > 0) {
                        _uiState.value.availableAccounts.find { it.id == transaction.toAccountId }
                    } else null

                    _uiState.value = _uiState.value.copy(
                        transactionId = transactionId,
                        isEditMode = true,
                        selectedAccount = selectedAccount,
                        selectedToAccount = selectedToAccount,
                        amount = (transaction.fromAmount / 100.0).toString(),
                        formattedAmount = formatAmount(transaction.fromAmount, transaction.originalCurrencyId),
                        note = transaction.note ?: "",
                        dateTime = transaction.datetime,
                        formattedDateTime = formatDateTime(transaction.datetime),
                        isTransfer = transaction.toAccountId > 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load transaction: ${e.message}"
                )
            }
        }
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
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            try {
                if (_uiState.value.isEditMode) {
                    // Update existing transaction
                    // TODO: Implement update logic
                } else {
                    // Create new transaction
                    // TODO: Implement create logic
                }
                
                _uiState.value = _uiState.value.copy(isSaving = false)
                // TODO: Navigate back or show success message
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save transaction: ${e.message}"
                )
            }
        }
    }

    private fun saveAsTemplate() {
        // TODO: Implement save as template logic
    }

    private fun clearForm() {
        _uiState.value = TransactionFormUiState(
            availableAccounts = _uiState.value.availableAccounts,
            availableCategories = _uiState.value.availableCategories,
            availablePayees = _uiState.value.availablePayees,
            availableProjects = _uiState.value.availableProjects,
            availableLocations = _uiState.value.availableLocations
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
