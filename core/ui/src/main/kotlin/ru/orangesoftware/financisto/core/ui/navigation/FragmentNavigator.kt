package ru.orangesoftware.financisto.core.ui.navigation

/**
 * Interface for handling navigation between screens in the Fragment-based architecture.
 * Fragments can use this to request navigation without knowing about the container Activity.
 */
interface FragmentNavigator {
    
    /**
     * Navigate to Account List screen
     */
    fun navigateToAccountList()
    
    /**
     * Navigate to Blotter screen
     */
    fun navigateToBlotter(accountId: Long = -1L)
    
    /**
     * Navigate to Transaction Form screen
     */
    fun navigateToTransactionForm(
        transactionId: Long = -1L,
        accountId: Long = -1L,
        duplicate: Boolean = false,
        fromTemplate: Boolean = false
    )
    
    /**
     * Navigate back in the navigation stack
     */
    fun navigateBack(): Boolean
    
    /**
     * Close the current screen (e.g., after successful save)
     */
    fun closeCurrentScreen()
}
