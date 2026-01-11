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
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.data.model.CategoryView
import ru.orangesoftware.financisto.data.model.TransactionEntity
import ru.orangesoftware.financisto.domain.model.Currency
import ru.orangesoftware.financisto.domain.model.CurrencyId
import ru.orangesoftware.financisto.domain.model.Money
import ru.orangesoftware.financisto.usecase.modern.GetTransactionByIdUseCase
import ru.orangesoftware.financisto.usecase.modern.CreateTransactionWithBalanceUpdateUseCase
import ru.orangesoftware.financisto.usecase.modern.UpdateTransactionUseCase
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetCategoryTreeUseCase
import ru.orangesoftware.financisto.usecase.modern.GetPayeesUseCase
import ru.orangesoftware.financisto.usecase.modern.GetProjectsUseCase
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
    private val createTransactionUseCase: CreateTransactionWithBalanceUpdateUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val insertOrUpdateTransactionUseCase: ru.orangesoftware.financisto.usecase.modern.InsertOrUpdateTransactionUseCase,
    private val insertSplitTransactionUseCase: ru.orangesoftware.financisto.usecase.modern.InsertSplitTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoryTreeUseCase: GetCategoryTreeUseCase,
    private val getPayeesUseCase: ru.orangesoftware.financisto.usecase.modern.GetPayeesUseCase,
    private val getProjectsUseCase: ru.orangesoftware.financisto.usecase.modern.GetProjectsUseCase,
    private val getCurrencyByIdUseCase: GetCurrencyByIdUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionFormUiState())
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    // Cache for currencies to avoid repeated database calls
    private val currencyCache = mutableMapOf<Long, Currency>()

    // Sequence for generating temporary IDs for split transactions
    private var idSequence = 0L

    // Navigation callbacks
    var onNavigateToCreateCategory: (() -> Unit)? = null
    var onNavigateToCreatePayee: (() -> Unit)? = null
    var onNavigateToCreateProject: (() -> Unit)? = null
    var onSplitSaved: ((SplitTransactionItem) -> Unit)? = null
    var onNavigateToEditSplit: ((SplitTransactionItem) -> Unit)? = null

    /**
     * Refreshes the available options (categories, payees, projects) after creating new entities.
     * Optionally selects the newly created entity if provided.
     */
    fun refreshDataAfterCreation(createdEntityType: CreatedEntityType, createdEntityId: Long? = null) {
        viewModelScope.launch(ioDispatcher) {
            try {
                when (createdEntityType) {
                    CreatedEntityType.CATEGORY -> {
                        val categoriesResult = getCategoryTreeUseCase.execute()
                        if (categoriesResult.isSuccess) {
                            val regularCategories = categoriesResult.getOrThrow().map { category ->
                                CategoryOption(
                                    id = category.id,
                                    title = category.title,
                                    iconResId = 0, // TODO: Map category to icon
                                    type = category.type
                                )
                            }
                            val categoryOptions = regularCategories + CategoryOption(
                                id = -1L,
                                title = "Split Transaction",
                                iconResId = 0,
                                type = 0
                            )
                            _uiState.value = _uiState.value.copy(availableCategories = categoryOptions)
                            
                            // Select the newly created category if ID provided
                            createdEntityId?.let { id ->
                                val newCategory = categoryOptions.find { it.id == id }
                                newCategory?.let { selectCategory(it) }
                            }
                        }
                    }
                    CreatedEntityType.PAYEE -> {
                        val payeesResult = getPayeesUseCase.execute()
                        if (payeesResult.isSuccess) {
                            val payeeOptions = payeesResult.getOrThrow().map { payee ->
                                PayeeOption(
                                    id = payee.id,
                                    name = payee.title
                                )
                            }
                            _uiState.value = _uiState.value.copy(availablePayees = payeeOptions)
                            
                            // Select the newly created payee if ID provided
                            createdEntityId?.let { id ->
                                val newPayee = payeeOptions.find { it.id == id }
                                newPayee?.let { selectPayee(it) }
                            }
                        }
                    }
                    CreatedEntityType.PROJECT -> {
                        val projectsResult = getProjectsUseCase.execute()
                        if (projectsResult.isSuccess) {
                            val projectOptions = projectsResult.getOrThrow().map { project ->
                                ProjectOption(
                                    id = project.id,
                                    name = project.title,
                                    isActive = project.isActive
                                )
                            }
                            _uiState.value = _uiState.value.copy(availableProjects = projectOptions)
                            
                            // Select the newly created project if ID provided
                            createdEntityId?.let { id ->
                                val newProject = projectOptions.find { it.id == id }
                                newProject?.let { selectProject(it) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Log error but don't show to user as this is a background refresh
                // The data will still be available from the previous load
            }
        }
    }

    /**
     * Enum to identify which type of entity was created
     */
    enum class CreatedEntityType {
        CATEGORY, PAYEE, PROJECT
    }

    init {
        // Load initial data and check if we're in edit mode
        val transactionId = savedStateHandle.get<Long>("transactionId") ?: -1L
        val accountId = savedStateHandle.get<Long>("accountId") ?: -1L
        val isTemplate = savedStateHandle.get<Boolean>("isTemplate") ?: false
        val isTransfer = savedStateHandle.get<Boolean>("isTransfer") ?: false
        
        loadInitialData(transactionId, accountId, isTemplate, isTransfer)
    }

    /**
     * Public method to handle user actions.
     * This is the single entry point for all UI interactions.
     */
    fun handleAction(action: TransactionFormAction) {
        when (action) {
            is TransactionFormAction.SelectAccount -> {
                selectAccount(action.account)
                validateForm()
            }
            is TransactionFormAction.SelectToAccount -> {
                selectToAccount(action.account)
                validateForm()
            }
            is TransactionFormAction.UpdateAmount -> {
                updateAmount(action.amount)
                validateForm()
            }
            is TransactionFormAction.SelectCategory -> {
                selectCategory(action.category)
                validateForm()
            }
            is TransactionFormAction.SelectPayee -> {
                selectPayee(action.payee)
                validateForm()
            }
            is TransactionFormAction.SelectProject -> {
                selectProject(action.project)
                validateForm()
            }
            is TransactionFormAction.AddNewCategory -> {
                android.util.Log.d("TransactionFormViewModel", "AddNewCategory action triggered")
                onNavigateToCreateCategory?.invoke() ?: android.util.Log.w("TransactionFormViewModel", "onNavigateToCreateCategory is null")
            }
            is TransactionFormAction.AddNewPayee -> {
                android.util.Log.d("TransactionFormViewModel", "AddNewPayee action triggered")
                onNavigateToCreatePayee?.invoke() ?: android.util.Log.w("TransactionFormViewModel", "onNavigateToCreatePayee is null")
            }
            is TransactionFormAction.AddNewProject -> {
                android.util.Log.d("TransactionFormViewModel", "AddNewProject action triggered")
                onNavigateToCreateProject?.invoke() ?: android.util.Log.w("TransactionFormViewModel", "onNavigateToCreateProject is null")
            }
            is TransactionFormAction.UpdateNote -> {
                updateNote(action.note)
                validateForm()
            }
            is TransactionFormAction.ToggleTransfer -> {
                toggleTransfer(action.isTransfer)
                validateForm()
            }
            is TransactionFormAction.UpdateExchangeRate -> {
                updateExchangeRate(action.rate)
                validateForm()
            }
            is TransactionFormAction.ToggleIncomeExpense -> {
                toggleIncomeExpense()
                validateForm()
            }
            is TransactionFormAction.ShowDateTimePicker -> { /* TODO: Handle date/time picker */ }
            is TransactionFormAction.ShowStatusPicker -> { /* TODO: Handle status picker */ }
            is TransactionFormAction.AddSplit -> {
                addSplit()
            }
            is TransactionFormAction.EditSplit -> {
                editSplit(action.split)
            }
            is TransactionFormAction.DeleteSplit -> {
                deleteSplit(action.split)
            }
        }
    }

    fun saveTransaction() {
        if (!_uiState.value.isFormValid) return
        
        // Prevent duplicate saves
        val currentState = _uiState.value.saveState
        if (currentState is SaveState.Saving || currentState is SaveState.Success) {
            return
        }
        
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                saveState = SaveState.Saving
            )
            
            try {
                val uiState = _uiState.value
                
                // Build transaction entity from form data
                val (parentTransaction, splitTransactions) = buildTransactionEntity(uiState)
                
                // Save transaction(s) with balance update
                val result = if (splitTransactions.isNotEmpty()) {
                    // Handle split transaction
                    saveSplitTransaction(parentTransaction, splitTransactions)
                } else {
                    // Handle regular transaction
                    createTransactionUseCase.execute(parentTransaction)
                }
                
                result.onSuccess { transactionId ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveState = SaveState.Success(transactionId, "Transaction saved successfully")
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveState = SaveState.Failed("Failed to save transaction: ${exception.message}")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveState = SaveState.Failed("Failed to save transaction: ${e.message}")
                )
            }
        }
    }

    private suspend fun saveSplitTransaction(
        parentTransaction: TransactionEntity,
        splitTransactions: List<TransactionEntity>
    ): Result<Long> = withContext(ioDispatcher) {
        // Use the new InsertSplitTransactionUseCase which handles:
        // 1. Atomic insertion of parent + children
        // 2. Proper balance updates (parent updates fromAccount, transfer children update toAccount)
        // 3. Incremental running balance updates
        // 4. All or nothing - rollback if any step fails
        insertSplitTransactionUseCase(parentTransaction, splitTransactions)
    }

    private fun buildTransactionEntity(uiState: TransactionFormUiState): Pair<TransactionEntity, List<TransactionEntity>> {
        // For transfers, amount should always be negative (expense from source account)
        // For regular transactions, use isIncome to determine sign
        val amountInCents = if (uiState.isTransfer) {
            (uiState.amount.toDoubleOrNull() ?: 0.0) * -100  // Always negative for transfers
        } else {
            (uiState.amount.toDoubleOrNull() ?: 0.0) * if (uiState.isIncome) 100 else -100
        }
        
        val parentTransaction = TransactionEntity(
            fromAccountId = uiState.selectedAccount?.id ?: 0,
            toAccountId = if (uiState.isTransfer) uiState.selectedToAccount?.id ?: 0 else 0,
            categoryId = if (uiState.isSplitTransaction) -1L else uiState.selectedCategory?.id ?: 0,
            projectId = uiState.selectedProject?.id ?: 0,
            payeeId = uiState.selectedPayee?.id ?: 0,
            fromAmount = amountInCents.toLong(),
            toAmount = if (uiState.isTransfer) {
                // For transfers: toAmount must be POSITIVE (credit to destination account)
                // while fromAmount is NEGATIVE (debit from source account)
                if (uiState.isDifferentCurrency) {
                    // Convert amount using exchange rate
                    val exchangeRate = uiState.exchangeRate.toDoubleOrNull() ?: 1.0
                    kotlin.math.abs((amountInCents * exchangeRate).toLong())
                } else {
                    kotlin.math.abs(amountInCents.toLong())
                }
            } else 0,
            datetime = uiState.dateTime,
            note = uiState.note.takeIf { it.isNotBlank() },
            status = uiState.status,
            isTemplate = uiState.isTemplate,
            originalCurrencyId = uiState.selectedAccount?.currencyId ?: 0
        )
        
        val splitTransactions = if (uiState.isSplitTransaction) {
            uiState.splitTransactions.map { split ->
                TransactionEntity(
                    fromAccountId = uiState.selectedAccount?.id ?: 0,
                    toAccountId = if (uiState.isTransfer) uiState.selectedToAccount?.id ?: 0 else 0,
                    categoryId = split.categoryId,
                    projectId = split.projectId ?: 0,
                    fromAmount = split.amount * split.type,
                    toAmount = if (uiState.isTransfer) {
                        // For split transfers: toAmount must be POSITIVE (credit to destination)
                        // Use absolute value of the split amount
                        if (uiState.isDifferentCurrency) {
                            val exchangeRate = uiState.exchangeRate.toDoubleOrNull() ?: 1.0
                            kotlin.math.abs((split.amount * exchangeRate).toLong())
                        } else {
                            kotlin.math.abs(split.amount)
                        }
                    } else 0,
                    datetime = uiState.dateTime,
                    note = split.note,
                    status = uiState.status,
                    parentId = 0L, // Will be set after parent is saved
                    originalCurrencyId = parentTransaction.originalCurrencyId
                )
            }
        } else {
            emptyList()
        }
        
        return Pair(parentTransaction, splitTransactions)
    }

    private fun loadInitialData(transactionId: Long, accountId: Long, isTemplate: Boolean, isTransfer: Boolean = false) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(screenState = TransactionFormScreenState.Loading)
            
            try {
                // Load real accounts, categories, payees, and projects from use cases
                val accounts = getAccountsUseCase.execute()
                val categoriesResult = getCategoryTreeUseCase.execute()
                val payeesResult = getPayeesUseCase.execute()
                val projectsResult = getProjectsUseCase.execute()
                
                val accountOptions = accounts.map { account ->
                    val currencySymbol = getCurrency(account.currencyId)?.symbol ?: "$"
                    val formattedBalance = formatAccountBalance(account.totalAmount, account.currencyId)
                    
                    AccountOption(
                        id = account.id,
                        title = account.title,
                        currencySymbol = currencySymbol,
                        balance = formattedBalance,
                        iconResId = 0, // TODO: Map account type to icon
                        currencyId = account.currencyId
                    )
                }
                
                val categoryOptions = if (categoriesResult.isSuccess) {
                    val regularCategories = categoriesResult.getOrThrow().map { category ->
                        CategoryOption(
                            id = category.id,
                            title = category.title,
                            iconResId = 0, // TODO: Map category to icon
                            type = category.type
                        )
                    }
                    // Add split category
                    regularCategories + CategoryOption(
                        id = -1L,
                        title = "Split Transaction",
                        iconResId = 0,
                        type = 0 // Doesn't matter for split
                    )
                } else {
                    // Fallback: include split category even if loading fails
                    listOf(CategoryOption(
                        id = -1L,
                        title = "Split Transaction",
                        iconResId = 0,
                        type = 0
                    ))
                }

                val payeeOptions = if (payeesResult.isSuccess) {
                    payeesResult.getOrThrow().map { payee ->
                        PayeeOption(
                            id = payee.id,
                            name = payee.title
                        )
                    }
                } else {
                    // Fallback to empty list if payees fail to load
                    emptyList()
                }

                val projectOptions = if (projectsResult.isSuccess) {
                    projectsResult.getOrThrow().map { project ->
                        ProjectOption(
                            id = project.id,
                            name = project.title,
                            isActive = project.isActive
                        )
                    }
                } else {
                    // Fallback to empty list if projects fail to load
                    emptyList()
                }

                val contentData = TransactionFormContentData()

                _uiState.value = _uiState.value.copy(
                    screenState = TransactionFormScreenState.Content(contentData),
                    availableAccounts = accountOptions,
                    availableCategories = categoryOptions,
                    availablePayees = payeeOptions,
                    availableProjects = projectOptions,
                    isTemplate = isTemplate,
                    isEditMode = transactionId > 0,
                    transactionId = transactionId,
                    isTransfer = isTransfer
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
        val isSplitCategory = category.id == -1L // Split category has ID -1
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            isSplitTransaction = isSplitCategory
        )
    }

    private fun selectPayee(payee: PayeeOption) {
        _uiState.value = _uiState.value.copy(selectedPayee = payee)
    }

    private fun selectProject(project: ProjectOption) {
        _uiState.value = _uiState.value.copy(selectedProject = project)
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
            if (!state.isSplitTransaction && state.selectedCategory == null) {
                errors.add(ValidationError.CategoryRequired)
            }
        }

        // Validate split transactions
        if (state.isSplitTransaction) {
            val totalAmount = state.amount.toDoubleOrNull() ?: 0.0
            val splitTotal = state.splitTransactions.sumOf { it.amount / 100.0 }
            if (Math.abs(totalAmount - splitTotal) > 0.01) { // Allow small floating point differences
                errors.add(ValidationError.SplitAmountMismatch)
            }
        }

        _uiState.value = _uiState.value.copy(
            validationErrors = errors,
            isFormValid = errors.isEmpty()
        )
    }

    private fun addSplit() {
        // Navigate to split editing screen instead of adding default split
        onNavigateToEditSplit?.invoke(SplitTransactionItem(id = -1L))
    }

    fun saveSplit(split: SplitTransactionItem) {
        val currentSplits = _uiState.value.splitTransactions
        val existingSplitIndex = currentSplits.indexOfFirst { it.id == split.id }

        val updatedSplits = if (existingSplitIndex >= 0) {
            // Update existing split
            currentSplits.toMutableList().apply {
                set(existingSplitIndex, split)
            }
        } else {
            // Add new split
            currentSplits + split
        }

        _uiState.value = _uiState.value.copy(
            splitTransactions = updatedSplits
        )
        updateRemainingAmount()
        validateForm()
    }

    private fun editSplit(split: SplitTransactionItem) {
        // Navigate to split editing screen
        onNavigateToEditSplit?.invoke(split)
    }

    private fun deleteSplit(split: SplitTransactionItem) {
        val currentSplits = _uiState.value.splitTransactions
        val updatedSplits = currentSplits.filter { it.id != split.id }
        _uiState.value = _uiState.value.copy(
            splitTransactions = updatedSplits
        )
        updateRemainingAmount()
        validateForm()
    }

    private fun updateRemainingAmount() {
        val state = _uiState.value
        val totalAmount = state.amount.toDoubleOrNull() ?: 0.0
        val splitTotal = state.splitTransactions.sumOf { it.amount / 100.0 } // Convert from cents
        val remaining = totalAmount - splitTotal
        _uiState.value = _uiState.value.copy(
            remainingAmount = String.format("%.2f", remaining)
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
