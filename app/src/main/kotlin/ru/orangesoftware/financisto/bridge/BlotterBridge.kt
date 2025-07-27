package ru.orangesoftware.financisto.bridge

import android.database.Cursor
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.db.DatabaseAdapter
import ru.orangesoftware.financisto.filter.WhereFilter

/**
 * Bridge class that handles the transition from direct DatabaseAdapter calls
 * to modern architecture components for Blotter/Transaction list operations.
 * 
 * This class uses composition to wrap the legacy DatabaseAdapter and provides
 * routing logic based on feature flags. When modernization flags are enabled,
 * it routes calls to new implementations; otherwise, it delegates to legacy code.
 * 
 * Usage in Activities:
 * ```kotlin
 * private val blotterBridge = BlotterBridge(db)
 * 
 * // Replace: db.getBlotter(filter) 
 * // With: blotterBridge.getBlotter(filter)
 * ```
 */
class BlotterBridge(private val legacyDb: DatabaseAdapter) {
    
    /**
     * Gets blotter data with routing based on feature flags.
     * 
     * @param filter The filter criteria for the blotter query
     * @return Cursor containing blotter data
     */
    fun getBlotter(filter: WhereFilter): Cursor {
        return if (FeatureFlags.USE_BLOTTER_BRIDGE) {
            // Future: Route to modern repository/use case implementation
            getBlotterModern(filter)
        } else {
            // Current: Delegate to legacy DatabaseAdapter
            legacyDb.getBlotter(filter)
        }
    }
    
    /**
     * Gets blotter data for a specific account with routing based on feature flags.
     * 
     * @param filter The filter criteria for the account blotter query
     * @return Cursor containing account blotter data
     */
    fun getBlotterForAccount(filter: WhereFilter): Cursor {
        return if (FeatureFlags.USE_BLOTTER_BRIDGE) {
            // Future: Route to modern repository/use case implementation  
            getBlotterForAccountModern(filter)
        } else {
            // Current: Delegate to legacy DatabaseAdapter
            legacyDb.getBlotterForAccount(filter)
        }
    }
    
    /**
     * Modern implementation placeholder for getBlotter.
     * This will be implemented in future phases when repositories are introduced.
     */
    private fun getBlotterModern(filter: WhereFilter): Cursor {
        // TODO: Phase 2 - Implement using TransactionRepository and use cases
        // For now, fallback to legacy to ensure functionality
        if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
            android.util.Log.d("BlotterBridge", "Using modern getBlotter implementation (placeholder)")
        }
        return legacyDb.getBlotter(filter)
    }
    
    /**
     * Modern implementation placeholder for getBlotterForAccount.
     * This will be implemented in future phases when repositories are introduced.
     */
    private fun getBlotterForAccountModern(filter: WhereFilter): Cursor {
        // TODO: Phase 2 - Implement using TransactionRepository and use cases
        // For now, fallback to legacy to ensure functionality
        if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
            android.util.Log.d("BlotterBridge", "Using modern getBlotterForAccount implementation (placeholder)")
        }
        return legacyDb.getBlotterForAccount(filter)
    }
}
