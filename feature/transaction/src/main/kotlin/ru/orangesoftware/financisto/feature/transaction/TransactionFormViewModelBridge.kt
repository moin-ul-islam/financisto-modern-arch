package ru.orangesoftware.financisto.feature.transaction

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.orangesoftware.financisto.core.common.FeatureFlags
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridge class that allows TransactionActivity to optionally delegate to TransactionFormViewModel.
 * 
 * This bridge uses composition pattern to gradually migrate from Activity-based logic
 * to ViewModel-based logic. When USE_TRANSACTION_FORM_VIEWMODEL flag is enabled, it delegates
 * UI state management to the ViewModel while preserving the existing Activity structure.
 * 
 * Note: Activity dependency is injected at runtime to avoid circular dependencies.
 */
@Singleton
class TransactionFormViewModelBridge @Inject constructor() {

    private var viewModel: TransactionFormViewModel? = null
    private var uiStateJob: Job? = null

    /**
     * Initialize the bridge with the Activity and ViewModel.
     * Should be called from Activity.onCreate() when feature flag is enabled.
     * 
     * @param activity The TransactionActivity instance (passed as Any to avoid import cycle)
     * @param viewModelStoreOwner The lifecycle owner for ViewModel scope
     * @param lifecycleScope Coroutine scope tied to Activity lifecycle
     */
    fun initialize(
        activity: Any, // TransactionActivity - using Any to avoid import dependencies
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleScope: CoroutineScope,
        transactionId: Long = -1L,
        accountId: Long = -1L,
        isTemplate: Boolean = false
    ) {
        if (!FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL) {
            return
        }

        // Get or create ViewModel with saved state
        viewModel = ViewModelProvider(viewModelStoreOwner)[TransactionFormViewModel::class.java]

        // Observe UI state changes and update Activity
        uiStateJob = viewModel?.uiState
            ?.onEach { uiState ->
                updateActivityUi(activity, uiState)
            }
            ?.launchIn(lifecycleScope)

        // Load initial data if editing existing transaction
        if (transactionId > 0) {
            viewModel?.handleAction(TransactionFormAction.LoadTransaction(transactionId))
        }
    }

    /**
     * Clean up resources when Activity is destroyed.
     */
    fun cleanup() {
        uiStateJob?.cancel()
        uiStateJob = null
        viewModel = null
    }

    /**
     * Delegate setting account to ViewModel if enabled.
     */
    fun setAccount(accountId: Long, accountTitle: String): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null) {
            val accountOption = AccountOption(
                id = accountId,
                title = accountTitle,
                currencySymbol = "$", // TODO: Get actual currency
                balance = "0.00", // TODO: Get actual balance
                iconResId = android.R.drawable.ic_menu_save
            )
            viewModel?.handleAction(TransactionFormAction.SetAccount(accountOption))
            true
        } else {
            false
        }
    }

    /**
     * Delegate setting amount to ViewModel if enabled.
     */
    fun setAmount(amount: String): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(TransactionFormAction.SetAmount(amount))
            true
        } else {
            false
        }
    }

    /**
     * Delegate setting category to ViewModel if enabled.
     */
    fun setCategory(categoryId: Long, categoryTitle: String): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null) {
            val categoryOption = CategoryOption(
                id = categoryId,
                title = categoryTitle,
                iconResId = android.R.drawable.ic_menu_info_details,
                type = "EXPENSE" // TODO: Get actual type
            )
            viewModel?.handleAction(TransactionFormAction.SetCategory(categoryOption))
            true
        } else {
            false
        }
    }

    /**
     * Delegate setting note to ViewModel if enabled.
     */
    fun setNote(note: String): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(TransactionFormAction.SetNote(note))
            true
        } else {
            false
        }
    }

    /**
     * Delegate setting date/time to ViewModel if enabled.
     */
    fun setDateTime(dateTime: Long): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(TransactionFormAction.SetDateTime(dateTime))
            true
        } else {
            false
        }
    }

    /**
     * Delegate saving transaction to ViewModel if enabled.
     */
    fun saveTransaction(): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(TransactionFormAction.SaveTransaction)
            true
        } else {
            false
        }
    }

    /**
     * Delegate form validation to ViewModel if enabled.
     */
    fun validateForm(): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(TransactionFormAction.ValidateForm)
            // Return current validation state
            viewModel?.uiState?.value?.isFormValid ?: false
        } else {
            false
        }
    }

    /**
     * Delegate toggle transfer mode to ViewModel if enabled.
     */
    fun toggleTransfer(isTransfer: Boolean): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(TransactionFormAction.ToggleTransfer(isTransfer))
            true
        } else {
            false
        }
    }

    /**
     * Update Activity UI based on ViewModel state.
     * Handles the new sealed class UI state structure.
     * 
     * @param activity The TransactionActivity instance (passed as Any to avoid import cycle)
     * @param uiState Current UI state from ViewModel
     */
    private fun updateActivityUi(activity: Any, uiState: TransactionFormUiState) {
        try {
            // Use reflection to call Activity methods to avoid import cycles
            val activityClass = activity.javaClass
            
            // Handle screen state using sealed classes
            when (uiState.screenState) {
                is TransactionFormScreenState.Loading -> {
                    // Show loading state - disable form, show progress
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, true)
                        
                        // TODO: Disable form fields during loading
                    } catch (e: Exception) {
                        // Fallback: try to show loading via other means or log
                    }
                }
                
                is TransactionFormScreenState.Empty -> {
                    // Show empty state - not typically used for form
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, false)
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is TransactionFormScreenState.Content -> {
                    // Show content - hide progress, enable form
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, false)
                        
                        // TODO: Enable form fields and populate with data from uiState.screenState.data
                        
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is TransactionFormScreenState.Error -> {
                    // Show error state - hide progress, show error
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, false)
                        
                        // Show error message - try to use Toast or existing error display
                        val context = activityClass.getMethod("getApplicationContext").invoke(activity) as android.content.Context
                        android.widget.Toast.makeText(context, uiState.screenState.message, android.widget.Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
            }

            // Handle save state
            when (uiState.saveState) {
                is SaveState.Saving -> {
                    // Show saving indicator and disable form
                    try {
                        // TODO: Show saving progress and disable save button
                        val context = activityClass.getMethod("getApplicationContext").invoke(activity) as android.content.Context
                        android.widget.Toast.makeText(context, "Saving...", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is SaveState.Success -> {
                    // Show success message and possibly finish activity
                    try {
                        val context = activityClass.getMethod("getApplicationContext").invoke(activity) as android.content.Context
                        android.widget.Toast.makeText(context, uiState.saveState.message, android.widget.Toast.LENGTH_SHORT).show()
                        
                        // TODO: Finish activity or navigate back
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is SaveState.Failed -> {
                    // Show save error
                    try {
                        val context = activityClass.getMethod("getApplicationContext").invoke(activity) as android.content.Context
                        android.widget.Toast.makeText(context, uiState.saveState.error, android.widget.Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is SaveState.Idle -> {
                    // Do nothing - normal state
                }
            }

            // Update form fields based on ViewModel state
            uiState.selectedAccount?.let { account ->
                // TODO: Update account field in Activity
            }

            if (uiState.amount.isNotEmpty()) {
                // TODO: Update amount field in Activity
            }

            uiState.selectedCategory?.let { category ->
                // TODO: Update category field in Activity
            }

            if (uiState.note.isNotEmpty()) {
                // TODO: Update note field in Activity
            }

            // Update validation errors
            if (uiState.validationErrors.isNotEmpty()) {
                // TODO: Show validation errors in Activity
            }

            // Update form valid state
            if (uiState.isFormValid) {
                // TODO: Enable save button in Activity
            } else {
                // TODO: Disable save button in Activity
            }
            
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("TransactionFormViewModelBridge", "Error updating Activity UI", e)
        }
    }

    /**
     * Check if ViewModel delegation is enabled and properly initialized.
     */
    fun isViewModelEnabled(): Boolean {
        return FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL && viewModel != null
    }

    /**
     * Get current UI state for debugging or testing purposes.
     */
    fun getCurrentUiState(): TransactionFormUiState? {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL) {
            viewModel?.uiState?.value
        } else {
            null
        }
    }

    /**
     * Get current validation errors for immediate feedback.
     */
    fun getValidationErrors(): List<ValidationError> {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL) {
            viewModel?.uiState?.value?.validationErrors ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Check if form is currently valid.
     */
    fun isFormValid(): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL) {
            viewModel?.uiState?.value?.isFormValid ?: false
        } else {
            false
        }
    }
}
