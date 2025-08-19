package ru.orangesoftware.financisto.feature.blotter

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
 * Bridge class that allows BlotterActivity to optionally delegate to BlotterViewModel.
 * 
 * This bridge uses composition pattern to gradually migrate from Activity-based logic
 * to ViewModel-based logic. When USE_BLOTTER_VIEWMODEL flag is enabled, it delegates
 * UI state management to the ViewModel while preserving the existing Activity structure.
 * 
 * The bridge pattern allows for:
 * - Gradual migration without breaking existing functionality
 * - Easy rollback by toggling feature flags
 * - Preservation of existing UI behavior during transition
 * - Testing of ViewModel logic alongside legacy code
 * 
 * Note: Activity dependency is injected at runtime to avoid circular dependencies.
 */
@Singleton
class BlotterViewModelBridge @Inject constructor() {

    private var viewModel: BlotterViewModel? = null
    private var uiStateJob: Job? = null

    /**
     * Initialize the bridge with the Activity and ViewModel.
     * Should be called from Activity.onCreate() when feature flag is enabled.
     * 
     * @param activity The BlotterActivity instance (passed as Any to avoid import cycle)
     * @param viewModelStoreOwner The lifecycle owner for ViewModel scope
     * @param lifecycleScope Coroutine scope tied to Activity lifecycle
     */
    fun initialize(
        activity: Any, // BlotterActivity - using Any to avoid import dependencies
        viewModelStoreOwner: ViewModelStoreOwner,
        lifecycleScope: CoroutineScope
    ) {
        if (!FeatureFlags.USE_BLOTTER_VIEWMODEL) {
            return
        }

        // Get or create ViewModel
        viewModel = ViewModelProvider(viewModelStoreOwner)[BlotterViewModel::class.java]

        // Observe UI state changes and update Activity
        uiStateJob = viewModel?.uiState
            ?.onEach { uiState ->
                updateActivityUi(activity, uiState)
            }
            ?.launchIn(lifecycleScope)

        // Load initial data
        viewModel?.handleAction(BlotterAction.LoadTransactions)
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
     * Delegate loading transactions to ViewModel if enabled.
     * Otherwise, returns false to let Activity handle it the legacy way.
     */
    fun loadTransactions(): Boolean {
        return if (FeatureFlags.USE_BLOTTER_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(BlotterAction.LoadTransactions)
            true
        } else {
            false
        }
    }

    /**
     * Delegate refresh to ViewModel if enabled.
     */
    fun refreshTransactions(): Boolean {
        return if (FeatureFlags.USE_BLOTTER_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(BlotterAction.RefreshTransactions)
            true
        } else {
            false
        }
    }

    /**
     * Delegate search to ViewModel if enabled.
     */
    fun searchTransactions(query: String): Boolean {
        return if (FeatureFlags.USE_BLOTTER_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(BlotterAction.SearchTransactions(query))
            true
        } else {
            false
        }
    }

    /**
     * Delegate filter by account to ViewModel if enabled.
     */
    fun filterByAccount(accountId: Long): Boolean {
        return if (FeatureFlags.USE_BLOTTER_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(BlotterAction.FilterByAccount(accountId))
            true
        } else {
            false
        }
    }

    /**
     * Delegate delete transaction to ViewModel if enabled.
     */
    fun deleteTransaction(transactionId: Long): Boolean {
        return if (FeatureFlags.USE_BLOTTER_VIEWMODEL && viewModel != null) {
            viewModel?.handleAction(BlotterAction.DeleteTransaction(transactionId))
            true
        } else {
            false
        }
    }

    /**
     * Update Activity UI based on ViewModel state.
     * This method bridges the gap between ViewModel state and existing Activity UI.
     * Handles the new sealed class UI state structure.
     * 
     * @param activity The BlotterActivity instance (passed as Any to avoid import cycle)
     * @param uiState Current UI state from ViewModel
     */
    private fun updateActivityUi(activity: Any, uiState: BlotterUiState) {
        try {
            // Use reflection to call Activity methods to avoid import cycles
            val activityClass = activity.javaClass
            
            // Handle screen state using sealed classes
            when (uiState.screenState) {
                is BlotterScreenState.Loading -> {
                    // Show loading state - hide list, show progress
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, true)
                    } catch (e: Exception) {
                        // Fallback: try to show loading via other means or log
                    }
                }
                
                is BlotterScreenState.Empty -> {
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
                
                is BlotterScreenState.Content -> {
                    // Show content - hide progress, update list
                    try {
                        val setProgressVisibility = activityClass.getDeclaredMethod("setProgressBarIndeterminateVisibility", Boolean::class.java)
                        setProgressVisibility.isAccessible = true
                        setProgressVisibility.invoke(activity, false)
                        
                        // Update transaction list if adapter is available
                        // TODO: Update Activity's ListView/RecyclerView with new data
                        // This would involve creating an adapter or updating existing adapter
                        val transactions = uiState.screenState.data.transactions
                        
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is BlotterScreenState.Error -> {
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
                    try {
                        val totalTextField = activityClass.getDeclaredField("totalText")
                        totalTextField.isAccessible = true
                        val totalText = totalTextField.get(activity) as android.widget.TextView
                        totalText.text = "Calculating..."
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
                
                is TotalCalculationState.Completed -> {
                    // Show calculated total
                    try {
                        val totalTextField = activityClass.getDeclaredField("totalText")
                        totalTextField.isAccessible = true
                        val totalText = totalTextField.get(activity) as android.widget.TextView
                        totalText.text = uiState.totalCalculationState.total
                        
                        // Show warning if present
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
                        val totalTextField = activityClass.getDeclaredField("totalText")
                        totalTextField.isAccessible = true
                        val totalText = totalTextField.get(activity) as android.widget.TextView
                        totalText.text = "Error"
                        
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

            // Update filter state
            if (uiState.isFilterActive) {
                // TODO: Update filter indicator in Activity
            }
            
            // Handle refreshing state
            if (uiState.isRefreshing) {
                // TODO: Show refresh indicator if Activity supports it
            }
            
        } catch (e: Exception) {
            // Log error but don't crash
            android.util.Log.e("BlotterViewModelBridge", "Error updating Activity UI", e)
        }
    }

    /**
     * Check if ViewModel delegation is enabled and properly initialized.
     */
    fun isViewModelEnabled(): Boolean {
        return FeatureFlags.USE_BLOTTER_VIEWMODEL && viewModel != null
    }

    /**
     * Get current UI state for debugging or testing purposes.
     */
    fun getCurrentUiState(): BlotterUiState? {
        return if (FeatureFlags.USE_BLOTTER_VIEWMODEL) {
            viewModel?.uiState?.value
        } else {
            null
        }
    }
}
