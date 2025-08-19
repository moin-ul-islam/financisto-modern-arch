package ru.orangesoftware.financisto.feature.transaction

/**
 * Enhanced UI state management for the Transaction Form screen.
 * Uses sealed classes to represent different screen states clearly.
 */
sealed class TransactionFormScreenState {
    object Loading : TransactionFormScreenState()
    object Empty : TransactionFormScreenState()
    data class Content(val data: TransactionFormContentData) : TransactionFormScreenState()
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val canRetry: Boolean = true
    ) : TransactionFormScreenState()
}

/**
 * UI state for the Transaction Form screen.
 * Contains data and states relevant to transaction creation/editing.
 */
data class TransactionFormUiState(
    val screenState: TransactionFormScreenState = TransactionFormScreenState.Loading,
    val isLoadingData: Boolean = false,
    val isSaving: Boolean = false,
    val saveState: SaveState = SaveState.Idle,
    
    // Form mode
    val transactionId: Long = -1,
    val isEditMode: Boolean = false,
    val isTemplate: Boolean = false,
    
    // Transaction fields
    val selectedAccount: AccountOption? = null,
    val availableAccounts: List<AccountOption> = emptyList(),
    val amount: String = "",
    val formattedAmount: String = "",
    val selectedCategory: CategoryOption? = null,
    val availableCategories: List<CategoryOption> = emptyList(),
    val selectedPayee: PayeeOption? = null,
    val availablePayees: List<PayeeOption> = emptyList(),
    val selectedProject: ProjectOption? = null,
    val availableProjects: List<ProjectOption> = emptyList(),
    val selectedLocation: LocationOption? = null,
    val availableLocations: List<LocationOption> = emptyList(),
    val note: String = "",
    val dateTime: Long = System.currentTimeMillis(),
    val formattedDateTime: String = "",
    
    // Transfer specific
    val isTransfer: Boolean = false,
    val selectedToAccount: AccountOption? = null,
    val exchangeRate: String = "1.0",
    val toAmount: String = "",
    
    // Split transaction
    val isSplitTransaction: Boolean = false,
    val splitTransactions: List<SplitTransactionItem> = emptyList(),
    val remainingAmount: String = "",
    
    // Validation
    val validationErrors: List<ValidationError> = emptyList(),
    val isFormValid: Boolean = false
)

/**
 * Content data for successful state
 */
data class TransactionFormContentData(
    val formData: Map<String, Any> = emptyMap(),
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * States for save operation
 */
sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val transactionId: Long, val message: String) : SaveState()
    data class Failed(val error: String, val canRetry: Boolean = true) : SaveState()
}

/**
 * Options for dropdowns/selectors
 */
data class AccountOption(
    val id: Long,
    val title: String,
    val currencySymbol: String,
    val balance: String,
    val iconResId: Int
)

data class CategoryOption(
    val id: Long,
    val title: String,
    val iconResId: Int,
    val type: String // "INCOME", "EXPENSE", etc.
)

data class PayeeOption(
    val id: Long,
    val title: String
)

data class ProjectOption(
    val id: Long,
    val title: String,
    val isActive: Boolean
)

data class LocationOption(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Split transaction item for complex transactions
 */
data class SplitTransactionItem(
    val id: Long = -1,
    val categoryId: Long,
    val categoryTitle: String,
    val amount: String,
    val formattedAmount: String,
    val note: String,
    val projectId: Long? = null,
    val projectTitle: String? = null
)

/**
 * Form validation errors
 */
sealed class ValidationError(val message: String) {
    object AmountRequired : ValidationError("Amount is required")
    object AccountRequired : ValidationError("Account is required")
    object CategoryRequired : ValidationError("Category is required")
    object InvalidAmount : ValidationError("Invalid amount format")
    object NegativeAmount : ValidationError("Amount must be positive")
    object ToAccountRequired : ValidationError("Destination account is required for transfers")
    object SameAccountTransfer : ValidationError("Cannot transfer to the same account")
    object SplitAmountMismatch : ValidationError("Split amounts don't match transaction amount")
}

/**
 * User actions that can be performed on the Transaction Form screen.
 */
sealed class TransactionFormAction {
    object InitializeForm : TransactionFormAction()
    object RetryLoading : TransactionFormAction()
    data class LoadTransaction(val transactionId: Long) : TransactionFormAction()
    data class SetAccount(val account: AccountOption) : TransactionFormAction()
    data class SetToAccount(val account: AccountOption) : TransactionFormAction()
    data class SetAmount(val amount: String) : TransactionFormAction()
    data class SetCategory(val category: CategoryOption) : TransactionFormAction()
    data class SetPayee(val payee: PayeeOption?) : TransactionFormAction()
    data class SetProject(val project: ProjectOption?) : TransactionFormAction()
    data class SetLocation(val location: LocationOption?) : TransactionFormAction()
    data class SetNote(val note: String) : TransactionFormAction()
    data class SetDateTime(val dateTime: Long) : TransactionFormAction()
    data class SetExchangeRate(val rate: String) : TransactionFormAction()
    data class ToggleTransfer(val isTransfer: Boolean) : TransactionFormAction()
    data class AddSplitTransaction(val split: SplitTransactionItem) : TransactionFormAction()
    data class RemoveSplitTransaction(val splitId: Long) : TransactionFormAction()
    data class UpdateSplitTransaction(val split: SplitTransactionItem) : TransactionFormAction()
    object SaveTransaction : TransactionFormAction()
    object SaveAsTemplate : TransactionFormAction()
    object ClearForm : TransactionFormAction()
    object ValidateForm : TransactionFormAction()
    object DismissSaveError : TransactionFormAction()
}
