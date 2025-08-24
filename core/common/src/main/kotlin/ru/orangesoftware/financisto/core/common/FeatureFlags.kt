package ru.orangesoftware.financisto.core.common

/**
 * Central feature flag system for the modernization process.
 * All flags are disabled by default to ensure unchanged app behavior.
 */
object FeatureFlags {

    /**
     * Phase 1.2 - Bridge Pattern Flags
     * These flags control the migration from direct DatabaseAdapter calls to bridge pattern
     */
    
    /**
     * Enables the BlotterBridge for transaction list operations
     * When enabled, BlotterActivity will use BlotterBridge instead of direct DatabaseAdapter calls
     */
    const val USE_BLOTTER_BRIDGE = false
    
    /**
     * Enables the AccountBridge for account operations  
     * When enabled, Activities will use AccountBridge instead of direct DatabaseAdapter calls
     */
    const val USE_ACCOUNT_BRIDGE = false
    
    /**
     * Enables the TransactionBridge for transaction CRUD operations
     * When enabled, Activities will use TransactionBridge instead of direct DatabaseAdapter calls
     */
    const val USE_TRANSACTION_BRIDGE = false

    /**
     * Phase 2 - Repository Pattern Flags (future)
     */
    
    /**
     * Enables Repository pattern for data access
     */
    const val USE_REPOSITORY_PATTERN = false
    
    /**
     * Enables Room database instead of legacy SQLite
     */
    const val USE_ROOM_DATABASE = false

    /**
     * Phase 4.1 - ViewModel Integration Flags
     */
    
    /**
     * Enables BlotterViewModel for transaction list screen
     * When enabled, BlotterActivity will delegate to BlotterViewModel when possible
     */
    const val USE_BLOTTER_VIEWMODEL = false
    
    /**
     * Enables AccountListViewModel for account list screen
     * When enabled, AccountListActivity will delegate to AccountListViewModel when possible
     */
    const val USE_ACCOUNT_LIST_VIEWMODEL = false
    
    /**
     * Enables TransactionFormViewModel for transaction form screen
     * When enabled, TransactionActivity will delegate to TransactionFormViewModel when possible
     */
    const val USE_TRANSACTION_FORM_VIEWMODEL = false

    /**
     * Phase 4.2 Continued - Fragment Architecture Flags
     */
    
    /**
     * Enables Fragment-based architecture instead of Activity-based
     * When enabled, screens will use Fragments with a single MainActivity container
     */
    const val USE_FRAGMENT_ARCHITECTURE = false

    /**
     * Development and debugging flags
     */
    
    /**
     * Enables verbose logging for modernization components
     */
    const val ENABLE_MODERNIZATION_LOGS = false
    
    /**
     * Enables validation that bridge results match legacy results
     */
    const val ENABLE_BRIDGE_VALIDATION = true
}
