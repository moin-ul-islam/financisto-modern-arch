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
import ru.orangesoftware.financisto.feature.transaction.ui.TransactionFormAction
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
            is TransactionFormAction.SelectAccount -> selectAccount(action.account)
            is TransactionFormAction.SelectToAccount -> selectToAccount(action.account)
            is TransactionFormAction.UpdateAmount -> updateAmount(action.amount)
            is TransactionFormAction.SelectCategory -> selectCategory(action.category)
            is TransactionFormAction.SelectPayee -> selectPayee(action.payee)
            is TransactionFormAction.SelectProject -> selectProject(action.project)
            is TransactionFormAction.SelectLocation -> selectLocation(action.location)
            is TransactionFormAction.UpdateNote -> updateNote(action.note)
            is TransactionFormAction.ToggleTransfer -> toggleTransfer(action.isTransfer)
            is TransactionFormAction.UpdateExchangeRate -> updateExchangeRate(action.rate)
            is TransactionFormAction.ToggleIncomeExpense -> toggleIncomeExpense()
            is TransactionFormAction.ShowDateTimePicker -> { /* TODO: Handle date/time picker */ }
            is TransactionFormAction.ShowStatusPicker -> { /* TODO: Handle status picker */ }
            is TransactionFormAction.AddSplit -> { /* TODO: Handle add split */ }
            is TransactionFormAction.EditSplit -> { /* TODO: Handle edit split */ }
            is TransactionFormAction.DeleteSplit -> { /* TODO: Handle delete split */ }
        }
        validateForm()
    }

    fun saveTransaction() {
        if (!_uiState.value.isFormValid) return
        
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                saveState = SaveState.Saving
            )
            
            try {
                // TODO: Implement actual save logic
                // For now, simulate success
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveState = SaveState.Success(1L, "Transaction saved successfully")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveState = SaveState.Failed("Failed to save transaction: ${e.message}")
                )
            }
        }
    }

    private fun loadInitialData(transactionId: Long, accountId: Long, isTemplate: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(screenState = TransactionFormScreenState.Loading)
            
            try {
                // TODO: Load actual data from use cases
                val mockAccounts = listOf(
                    AccountOption(
                        id = 1L,
                        title = "Cash",
                        currencySymbol = "$",
                        balance = "1,250.00",
                        iconResId = 0
                    ),
                    AccountOption(
                        id = 2L,
                        title = "Bank Account",
                        currencySymbol = "$",
                        balance = "5,430.25",
                        iconResId = 0
                    )
                )
                
                val mockCategories = listOf(
                    CategoryOption(
                        id = 1L,
                        title = "Food & Dining",
                        iconResId = 0,
                        type = "EXPENSE"
                    ),
                    CategoryOption(
                        id = 2L,
                        title = "Salary",
                        iconResId = 0,
                        type = "INCOME"
                    )
                )

                val contentData = TransactionFormContentData()

                _uiState.value = _uiState.value.copy(
                    screenState = TransactionFormScreenState.Content(contentData),
                    availableAccounts = mockAccounts,
                    availableCategories = mockCategories,
                    isTemplate = isTemplate,
                    isEditMode = transactionId > 0,
                    transactionId = transactionId
                )

                // Pre-select account if provided
                if (accountId > 0) {
                    val selectedAccount = mockAccounts.find { it.id == accountId }
                    selectedAccount?.let { selectAccount(it) }
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

    private fun selectAccount(account: AccountOption) {
        _uiState.value = _uiState.value.copy(selectedAccount = account)
    }

    private fun selectToAccount(account: AccountOption) {
        _uiState.value = _uiState.value.copy(selectedToAccount = account)
    }

    private fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount,
            formattedAmount = formatAmount(amount)
        )
    }

    private fun selectCategory(category: CategoryOption) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    private fun selectPayee(payee: PayeeOption) {
        _uiState.value = _uiState.value.copy(selectedPayee = payee)
    }

    private fun selectProject(project: ProjectOption) {
        _uiState.value = _uiState.value.copy(selectedProject = project)
    }

    private fun selectLocation(location: LocationOption) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    private fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    private fun toggleTransfer(isTransfer: Boolean) {
        _uiState.value = _uiState.value.copy(
            isTransfer = isTransfer,
            selectedToAccount = if (!isTransfer) null else _uiState.value.selectedToAccount
        )
    }

    private fun updateExchangeRate(rate: String) {
        _uiState.value = _uiState.value.copy(exchangeRate = rate)
    }

    private fun toggleIncomeExpense() {
        _uiState.value = _uiState.value.copy(isIncome = !_uiState.value.isIncome)
    }

    private fun validateForm() {
        val state = _uiState.value
        val errors = mutableListOf<ValidationError>()

        // Validate required fields
        if (state.selectedAccount == null) {
            errors.add(ValidationError.AccountRequired)
        }

        if (state.amount.isBlank()) {
            errors.add(ValidationError.AmountRequired)
        } else {
            try {
                val amountValue = state.amount.toDouble()
                if (amountValue <= 0) {
                    errors.add(ValidationError.NegativeAmount)
                }
            } catch (e: NumberFormatException) {
                errors.add(ValidationError.InvalidAmount)
            }
        }

        if (state.isTransfer) {
            if (state.selectedToAccount == null) {
                errors.add(ValidationError.ToAccountRequired)
            } else if (state.selectedAccount?.id == state.selectedToAccount?.id) {
                errors.add(ValidationError.SameAccountTransfer)
            }
        } else {
            if (state.selectedCategory == null) {
                errors.add(ValidationError.CategoryRequired)
            }
        }

        _uiState.value = _uiState.value.copy(
            validationErrors = errors,
            isFormValid = errors.isEmpty()
        )
    }

    private fun formatAmount(amount: String): String {
        return try {
            val value = amount.toDouble()
            String.format("%.2f", value)
        } catch (e: NumberFormatException) {
            amount
        }
    }
}
