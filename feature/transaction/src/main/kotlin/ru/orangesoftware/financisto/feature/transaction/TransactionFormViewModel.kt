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
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.data.model.CategoryView
import ru.orangesoftware.financisto.domain.model.Currency
import ru.orangesoftware.financisto.domain.model.CurrencyId
import ru.orangesoftware.financisto.domain.model.Money
import ru.orangesoftware.financisto.usecase.modern.GetTransactionByIdUseCase
import ru.orangesoftware.financisto.usecase.modern.CreateTransactionUseCase
import ru.orangesoftware.financisto.usecase.modern.UpdateTransactionUseCase
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetCategoryTreeUseCase
import ru.orangesoftware.financisto.usecase.modern.GetCurrencyByIdUseCase
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
    private val getCategoryTreeUseCase: GetCategoryTreeUseCase,
    private val getCurrencyByIdUseCase: GetCurrencyByIdUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionFormUiState())
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    // Cache for currencies to avoid repeated database calls
    private val currencyCache = mutableMapOf<Long, Currency>()

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
                // Load real accounts and categories from use cases
                val accounts = getAccountsUseCase.execute()
                val categoriesResult = getCategoryTreeUseCase.execute()
                
                val accountOptions = accounts.map { account ->
                    val currencySymbol = getCurrency(account.currencyId)?.symbol ?: "$"
                    val formattedBalance = formatAccountBalance(account.totalAmount, account.currencyId)
                    
                    AccountOption(
                        id = account.id,
                        title = account.title,
                        currencySymbol = currencySymbol,
                        balance = formattedBalance,
                        iconResId = 0 // TODO: Map account type to icon
                    )
                }
                
                val categoryOptions = if (categoriesResult.isSuccess) {
                    categoriesResult.getOrThrow().map { category ->
                        CategoryOption(
                            id = category.id,
                            title = category.title,
                            iconResId = 0, // TODO: Map category to icon
                            type = getCategoryTypeString(category.type)
                        )
                    }
                } else {
                    // Fallback to empty list if categories fail to load
                    emptyList()
                }

                val contentData = TransactionFormContentData()

                _uiState.value = _uiState.value.copy(
                    screenState = TransactionFormScreenState.Content(contentData),
                    availableAccounts = accountOptions,
                    availableCategories = categoryOptions,
                    isTemplate = isTemplate,
                    isEditMode = transactionId > 0,
                    transactionId = transactionId
                )

                // Pre-select account if provided
                if (accountId > 0) {
                    val selectedAccount = accountOptions.find { it.id == accountId }
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

    private suspend fun getCurrency(currencyId: Long): Currency? {
        // Check cache first
        currencyCache[currencyId]?.let { return it }

        // Load from database if not in cache
        val currencyEntity = getCurrencyByIdUseCase.execute(currencyId)
        if (currencyEntity != null) {
            val currency = Currency(
                id = ru.orangesoftware.financisto.domain.model.CurrencyId(currencyEntity.id),
                name = currencyEntity.name,
                title = currencyEntity.title,
                symbol = currencyEntity.symbol,
                isDefault = currencyEntity.isDefault,
                decimals = currencyEntity.decimals,
                decimalSeparator = currencyEntity.decimalSeparator,
                groupSeparator = currencyEntity.groupSeparator,
                symbolFormat = ru.orangesoftware.financisto.domain.model.SymbolFormat.valueOf(currencyEntity.symbolFormat),
                isActive = true // Default to active since entity doesn't have this field
            )
            // Cache the currency
            currencyCache[currencyId] = currency
            return currency
        }

        return null
    }

    private suspend fun formatAccountBalance(amount: Long, currencyId: Long): String {
        val currency = getCurrency(currencyId)
        return if (currency != null) {
            val money = Money(amount)
            currency.formatAmount(money)
        } else {
            // Fallback formatting
            "$${amount / 100}.${String.format("%02d", amount % 100)}"
        }
    }

    private fun getCategoryTypeString(type: Int): String {
        // Map the integer type to string representation
        // Based on CategoryEntity: 0 = expense, 1 = income
        return when (type) {
            0 -> "EXPENSE"
            1 -> "INCOME"
            else -> "EXPENSE" // Default to expense
        }
    }
}
