package ru.orangesoftware.financisto.feature.account

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
 * Bridge class that allows AccountListActivity to optionally delegate to AccountListViewModel.
 * 
 * This bridge uses composition pattern to gradually migrate from Activity-based logic
 * to ViewModel-based logic. When USE_ACCOUNT_LIST_VIEWMODEL flag is enabled, it delegates
 * UI state management to the ViewModel while preserving the existing Activity structure.
 * 
 * Note: Activity dependency is injected at runtime to avoid circular dependencies.
 */
@Singleton
class AccountListViewModelBridge @Inject constructor() {

    private var viewModel: AccountListViewModel? = null
    private var uiStateJob: Job? = null

    /**
     * Initialize the bridge with the Activity and ViewModel.
     * Should be called from Activity.onCreate() when feature flag is enabled.
     * 
     * @param activity The AccountListActivity instance (passed as Any to avoid import cycle)
     * @param viewModelStoreOwner The lifecycle owner for ViewModel scope
     * @param lifecycleScope Coroutine scope tied to Activity lifecycle
     */
    fun initialize(
        activity: Any, // AccountListActivity - using Any to avoid import dependencies
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleScope: CoroutineScope
    ) {
        if (!FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL) {
            return
        }

        // Get or create ViewModel
        viewModel = ViewModelProvider(viewModelStoreOwner)[AccountListViewModel::class.java]

        // Observe UI state changes and update Activity
        uiStateJob = viewModel?.uiState
            ?.onEach { uiState ->
                updateActivityUi(activity, uiState)
            }
            ?.launchIn(lifecycleScope)

        // Load initial data
        viewModel?.handleAction(AccountListAction.LoadAccounts)
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
     * Delegate loading accounts to ViewModel if enabled.
     */
    fun loadAccounts(): Boolean {
        return if (FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(AccountListAction.LoadAccounts)
            true
        } else {
            false
        }
    }

    /**
     * Delegate refresh to ViewModel if enabled.
     */
    fun refreshAccounts(): Boolean {
        return if (FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(AccountListAction.RefreshAccounts)
            true
        } else {
            false
        }
    }

    /**
     * Delegate account editing to ViewModel if enabled.
     */
    fun editAccount(accountId: Long): Boolean {
        return if (FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(AccountListAction.EditAccount(accountId))
            true
        } else {
            false
        }
    }

    /**
     * Delegate account deletion to ViewModel if enabled.
     */
    fun deleteAccount(accountId: Long): Boolean {
        return if (FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(AccountListAction.DeleteAccount(accountId))
            true
        } else {
            false
        }
    }

    /**
     * Delegate sorting to ViewModel if enabled.
     */
    fun sortBy(sortOrder: AccountSortOrder): Boolean {
        return if (FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(AccountListAction.SortBy(sortOrder))
            true
        } else {
            false
        }
    }

    /**
     * Update Activity UI based on ViewModel state.
     * Handles the new sealed class UI state structure.
     * 
     * @param activity The AccountListActivity instance (passed as Any to avoid import cycle)
     * @param uiState Current UI state from ViewModel
     */
    private fun updateActivityUi(activity: Any, uiState: AccountListUiState) {
        try {
            // Use reflection to call Activity methods to avoid import cycles
            val activityClass = activity.javaClass
            
            // Handle screen state using sealed classes
            when (uiState.screenState) {
                is AccountListScreenState.Loading -> {
                    // Show loading state - hide list, show progress
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, true)
                    } catch (e: Exception) {
                        // Fallback: try to show loading via other means or log
                    }
                }
                
                is AccountListScreenState.Empty -> {
                    // Show empty state - hide progress, show empty message
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, false)
                        
                        // TODO: Show empty state message
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is AccountListScreenState.Content -> {
                    // Show content - hide progress, update list
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, false)
                        
                        // Update account list if adapter is available
                        // TODO: Update Activity's ListView/RecyclerView with new data
                        val accounts = uiState.screenState.data.accounts
                        
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is AccountListScreenState.Error -> {
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

            // Handle total calculation state
            when (uiState.totalCalculationState) {
                is TotalCalculationState.Calculating -> {
                    // Show calculation in progress
                    // TODO: Update total text view to show calculating
                }
                
                is TotalCalculationState.Completed -> {
                    // Show calculated total
                    try {
                        // TODO: Find and update the total display element
                        uiState.totalCalculationState.warningMessage?.let { warning ->
                            val context = activityClass.getMethod("getApplicationContext").invoke(activity) as android.content.Context
                            android.widget.Toast.makeText(context, warning, android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is TotalCalculationState.Failed -> {
                    // Show calculation error
                    try {
                        val context = activityClass.getMethod("getApplicationContext").invoke(activity) as android.content.Context
                        android.widget.Toast.makeText(context, uiState.totalCalculationState.error, android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is TotalCalculationState.Idle -> {
                    // Do nothing - keep current state
                }
            }
            
            // Handle refreshing state
            if (uiState.isRefreshing) {
                // TODO: Show refresh indicator if Activity supports it
            }
            
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("AccountListViewModelBridge", "Error updating Activity UI", e)
        }
    }

    /**
     * Check if ViewModel delegation is enabled and properly initialized.
     */
    fun isViewModelEnabled(): Boolean {
        return FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL && viewModel != null
    }

    /**
     * Get current UI state for debugging or testing purposes.
     */
    fun getCurrentUiState(): AccountListUiState? {
        return if (FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL) {
            viewModel?.uiState?.value
        } else {
            null
        }
    }
}
