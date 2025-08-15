package ru.orangesoftware.financisto.bridge

import android.database.Cursor
import kotlinx.coroutines.runBlocking
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.db.DatabaseAdapter
import ru.orangesoftware.financisto.filter.WhereFilter
import ru.orangesoftware.financisto.repository.modern.TransactionRepository
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsForAccountUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridge class that handles the transition from direct DatabaseAdapter calls
 * to modern architecture components for Blotter/Transaction list operations.
 * 
 * This class uses composition to wrap the legacy DatabaseAdapter and provides
 * routing logic based on feature flags. When modernization flags are enabled,
 * it routes calls to new implementations; otherwise, it delegates to legacy code.
 * 
 * Phase 2.3 Implementation:
 * - Uses modern repositories and use cases when flags are enabled
 * - Provides seamless data access for transaction listing operations
 * - Maintains full backward compatibility with existing Cursor-based code
 * 
 * Note: For now, modern implementations still return Cursors to maintain
 * compatibility with existing Activities. Future phases will migrate to
 * more modern data structures like Flow<List<TransactionEntity>>.
 */
@Singleton
class BlotterBridge @Inject constructor(
    private val legacyDb: DatabaseAdapter,
    private val transactionRepository: TransactionRepository,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionsForAccountUseCase: GetTransactionsForAccountUseCase
) {
    
    /**
     * Gets blotter data with routing based on feature flags.
     * 
     * @param filter The filter criteria for the blotter query
     * @return Cursor containing blotter data
     */
    fun getBlotter(filter: WhereFilter): Cursor {
        return if (FeatureFlags.USE_BLOTTER_BRIDGE) {
            getBlotterModern(filter)
        } else {
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
            getBlotterForAccountModern(filter)
        } else {
            legacyDb.getBlotterForAccount(filter)
        }
    }

    /**
     * Gets blotter data for account with splits.
     * 
     * @param filter The filter criteria for the account blotter query
     * @return Cursor containing account blotter data with splits
     */
    fun getBlotterForAccountWithSplits(filter: WhereFilter): Cursor {
        return if (FeatureFlags.USE_BLOTTER_BRIDGE) {
            getBlotterForAccountWithSplitsModern(filter)
        } else {
            legacyDb.getBlotterForAccountWithSplits(filter)
        }
    }

    // ========================================
    // Modern Implementations
    // ========================================

    /**
     * Modern implementation for getBlotter.
     * Note: Currently returns legacy Cursor for compatibility.
     * Future phases will migrate to Flow<List<TransactionEntity>>.
     */
    private fun getBlotterModern(filter: WhereFilter): Cursor {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("BlotterBridge", "Using modern getBlotter implementation")
            }
            
            // For now, we still delegate to legacy to maintain Cursor compatibility
            // Future: Convert Room entities to Cursor or migrate Activities to use modern data structures
            val transactions = runBlocking { 
                getTransactionsUseCase.execute() 
            }
            
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("BlotterBridge", "Modern getBlotter loaded ${transactions.size} transactions")
            }
            
            // For compatibility, still return legacy Cursor
            // TODO: In future phases, create Cursor from Room entities or migrate UI to use modern data
            legacyDb.getBlotter(filter)
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("BlotterBridge", "Error in modern getBlotter, falling back to legacy", e)
            }
            legacyDb.getBlotter(filter)
        }
    }
    
    /**
     * Modern implementation for getBlotterForAccount.
     * Note: Currently returns legacy Cursor for compatibility.
     */
    private fun getBlotterForAccountModern(filter: WhereFilter): Cursor {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("BlotterBridge", "Using modern getBlotterForAccount implementation")
            }
            
            // Extract account ID from filter if possible
            // For now, we still delegate to legacy to maintain Cursor compatibility
            // TODO: Parse filter to extract account ID and use getTransactionsForAccountUseCase
            
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("BlotterBridge", "Modern getBlotterForAccount processing filter")
            }
            
            // For compatibility, still return legacy Cursor
            legacyDb.getBlotterForAccount(filter)
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("BlotterBridge", "Error in modern getBlotterForAccount, falling back to legacy", e)
            }
            legacyDb.getBlotterForAccount(filter)
        }
    }

    /**
     * Modern implementation for getBlotterForAccountWithSplits.
     * Note: Currently returns legacy Cursor for compatibility.
     */
    private fun getBlotterForAccountWithSplitsModern(filter: WhereFilter): Cursor {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("BlotterBridge", "Using modern getBlotterForAccountWithSplits implementation")
            }
            
            // For compatibility, still return legacy Cursor
            // TODO: In future phases, implement split transaction handling in Room
            legacyDb.getBlotterForAccountWithSplits(filter)
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("BlotterBridge", "Error in modern getBlotterForAccountWithSplits, falling back to legacy", e)
            }
            legacyDb.getBlotterForAccountWithSplits(filter)
        }
    }
}
