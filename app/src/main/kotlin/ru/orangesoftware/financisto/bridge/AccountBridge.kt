package ru.orangesoftware.financisto.bridge

import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.db.DatabaseAdapter
import ru.orangesoftware.financisto.model.Account

/**
 * Bridge class that handles the transition from direct DatabaseAdapter calls
 * to modern architecture components for Account operations.
 * 
 * This class uses composition to wrap the legacy DatabaseAdapter and provides
 * routing logic based on feature flags. When modernization flags are enabled,
 * it routes calls to new implementations; otherwise, it delegates to legacy code.
 * 
 * Usage in Activities:
 * ```kotlin
 * private val accountBridge = AccountBridge(db)
 * 
 * // Replace: db.getAccount(accountId)
 * // With: accountBridge.getAccount(accountId)
 * ```
 */
class AccountBridge(private val legacyDb: DatabaseAdapter) {
    
    /**
     * Gets an account by ID with routing based on feature flags.
     * 
     * @param accountId The ID of the account to retrieve
     * @return Account object or null if not found
     */
    fun getAccount(accountId: Long): Account? {
        return if (FeatureFlags.USE_ACCOUNT_BRIDGE) {
            // Future: Route to modern repository/use case implementation
            getAccountModern(accountId)
        } else {
            // Current: Delegate to legacy DatabaseAdapter
            legacyDb.getAccount(accountId)
        }
    }
    
    /**
     * Modern implementation placeholder for getAccount.
     * This will be implemented in future phases when repositories are introduced.
     */
    private fun getAccountModern(accountId: Long): Account? {
        // TODO: Phase 2 - Implement using AccountRepository and use cases
        // For now, fallback to legacy to ensure functionality
        if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
            android.util.Log.d("AccountBridge", "Using modern getAccount implementation (placeholder)")
        }
        return legacyDb.getAccount(accountId)
    }
}
