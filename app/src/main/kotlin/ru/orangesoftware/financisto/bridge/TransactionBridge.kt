package ru.orangesoftware.financisto.bridge

import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.db.DatabaseAdapter
import ru.orangesoftware.financisto.model.Transaction

/**
 * Bridge class that handles the transition from direct DatabaseAdapter calls
 * to modern architecture components for Transaction operations.
 * 
 * This class uses composition to wrap the legacy DatabaseAdapter and provides
 * routing logic based on feature flags. When modernization flags are enabled,
 * it routes calls to new implementations; otherwise, it delegates to legacy code.
 * 
 * Usage in Activities:
 * ```kotlin
 * private val transactionBridge = TransactionBridge(db)
 * 
 * // Replace: db.getTransaction(transactionId)
 * // With: transactionBridge.getTransaction(transactionId)
 * ```
 */
class TransactionBridge(private val legacyDb: DatabaseAdapter) {
    
    /**
     * Gets a transaction by ID with routing based on feature flags.
     * 
     * @param transactionId The ID of the transaction to retrieve
     * @return Transaction object
     */
    fun getTransaction(transactionId: Long): Transaction {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            // Future: Route to modern repository/use case implementation
            getTransactionModern(transactionId)
        } else {
            // Current: Delegate to legacy DatabaseAdapter
            legacyDb.getTransaction(transactionId)
        }
    }
    
    /**
     * Modern implementation placeholder for getTransaction.
     * This will be implemented in future phases when repositories are introduced.
     */
    private fun getTransactionModern(transactionId: Long): Transaction {
        // TODO: Phase 2 - Implement using TransactionRepository and use cases
        // For now, fallback to legacy to ensure functionality
        if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
            android.util.Log.d("TransactionBridge", "Using modern getTransaction implementation (placeholder)")
        }
        return legacyDb.getTransaction(transactionId)
    }
}
