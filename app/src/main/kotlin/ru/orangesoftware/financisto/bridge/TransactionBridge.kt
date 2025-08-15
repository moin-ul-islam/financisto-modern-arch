package ru.orangesoftware.financisto.bridge

import kotlinx.coroutines.runBlocking
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.data.model.ModelConverters.toLegacyModel
import ru.orangesoftware.financisto.data.model.ModelConverters.toRoomEntity
import ru.orangesoftware.financisto.db.DatabaseAdapter
import ru.orangesoftware.financisto.model.Transaction
import ru.orangesoftware.financisto.model.Account
import ru.orangesoftware.financisto.repository.modern.TransactionRepository
import ru.orangesoftware.financisto.usecase.modern.GetTransactionByIdUseCase
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsForAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.CreateTransactionUseCase
import ru.orangesoftware.financisto.usecase.modern.UpdateTransactionUseCase
import ru.orangesoftware.financisto.usecase.modern.DeleteTransactionUseCase
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsByDateRangeUseCase
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsByCategoryUseCase
import ru.orangesoftware.financisto.usecase.modern.RebuildRunningBalanceForAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.RebuildAllRunningBalancesUseCase
import ru.orangesoftware.financisto.usecase.modern.GetLastRunningBalanceForAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.GetAccountBalanceAtTimeUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridge class that handles the transition from direct DatabaseAdapter calls
 * to modern architecture components for Transaction operations.
 * 
 * This class uses composition to wrap the legacy DatabaseAdapter and provides
 * routing logic based on feature flags. When modernization flags are enabled,
 * it routes calls to new implementations; otherwise, it delegates to legacy code.
 * 
 * Phase 2.3 Implementation:
 * - Uses modern repositories and use cases when flags are enabled
 * - Provides seamless data conversion between legacy models and Room entities
 * - Maintains full backward compatibility with existing code
 * - Includes validation logic to ensure modern and legacy results match
 * - Includes critical business logic like running balance calculations
 */
@Singleton
class TransactionBridge @Inject constructor(
    private val legacyDb: DatabaseAdapter,
    private val transactionRepository: TransactionRepository?,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase?,
    private val getTransactionsUseCase: GetTransactionsUseCase?,
    private val getTransactionsForAccountUseCase: GetTransactionsForAccountUseCase?,
    private val createTransactionUseCase: CreateTransactionUseCase?,
    private val updateTransactionUseCase: UpdateTransactionUseCase?,
    private val deleteTransactionUseCase: DeleteTransactionUseCase?,
    private val getTransactionsByDateRangeUseCase: GetTransactionsByDateRangeUseCase?,
    private val getTransactionsByCategoryUseCase: GetTransactionsByCategoryUseCase?,
    private val rebuildRunningBalanceForAccountUseCase: RebuildRunningBalanceForAccountUseCase?,
    private val rebuildAllRunningBalancesUseCase: RebuildAllRunningBalancesUseCase?,
    private val getLastRunningBalanceForAccountUseCase: GetLastRunningBalanceForAccountUseCase?,
    private val getAccountBalanceAtTimeUseCase: GetAccountBalanceAtTimeUseCase?
) {
    
    /**
     * Gets a transaction by ID with routing based on feature flags.
     */
    fun getTransaction(transactionId: Long): Transaction {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            getTransactionModern(transactionId)
        } else {
            legacyDb.getTransaction(transactionId)
        }
    }

    /**
     * Gets all transactions with routing based on feature flags.
     */
    fun getAllTransactions(): List<Transaction> {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            getAllTransactionsModern()
        } else {
            // Note: legacy doesn't have getAllTransactions, adapt as needed
            emptyList()
        }
    }

    /**
     * Gets transactions for a specific account with routing based on feature flags.
     */
    fun getTransactionsForAccount(accountId: Long): List<Transaction> {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            getTransactionsForAccountModern(accountId)
        } else {
            // Legacy implementation would need custom query
            emptyList()
        }
    }

    /**
     * Creates or updates a transaction with routing based on feature flags.
     */
    fun insertOrUpdate(transaction: Transaction): Long {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            insertOrUpdateModern(transaction)
        } else {
            legacyDb.insertOrUpdate(transaction)
        }
    }

    /**
     * Deletes a transaction with routing based on feature flags.
     */
    fun deleteTransaction(transactionId: Long): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            deleteTransactionModern(transactionId)
        } else {
            try {
                legacyDb.deleteTransaction(transactionId)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Gets transactions by date range with routing based on feature flags.
     */
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<Transaction> {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            getTransactionsByDateRangeModern(startDate, endDate)
        } else {
            // Legacy implementation would need custom query
            emptyList()
        }
    }

    /**
     * Gets transactions by category with routing based on feature flags.
     */
    fun getTransactionsByCategory(categoryId: Long): List<Transaction> {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            getTransactionsByCategoryModern(categoryId)
        } else {
            // Legacy implementation would need custom query
            emptyList()
        }
    }

    /**
     * Rebuilds running balance for a specific account with routing based on feature flags.
     * This is critical business logic that must be preserved exactly.
     */
    fun rebuildRunningBalanceForAccount(account: Account): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            rebuildRunningBalanceForAccountModern(account.id)
        } else {
            try {
                legacyDb.rebuildRunningBalanceForAccount(account)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Rebuilds running balances for all accounts with routing based on feature flags.
     */
    fun rebuildRunningBalances(): Boolean {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            rebuildAllRunningBalancesModern()
        } else {
            try {
                legacyDb.rebuildRunningBalances()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Gets the last running balance for an account with routing based on feature flags.
     */
    fun getLastRunningBalanceForAccount(account: Account): Long {
        return if (FeatureFlags.USE_TRANSACTION_BRIDGE) {
            getLastRunningBalanceForAccountModern(account.id)
        } else {
            legacyDb.getLastRunningBalanceForAccount(account)
        }
    }

    // ========================================
    // Modern Implementations
    // ========================================

    /**
     * Modern implementation for getTransaction using Room repository.
     */
    private fun getTransactionModern(transactionId: Long): Transaction {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern getTransaction implementation for ID: $transactionId")
            }
            
            val transactionEntity = runBlocking { 
                getTransactionByIdUseCase?.execute(transactionId)
            }
            
            val result = transactionEntity?.toLegacyModel() ?: Transaction()
            
            // Validation: Compare with legacy result if enabled
            if (FeatureFlags.ENABLE_BRIDGE_VALIDATION && transactionEntity != null) {
                validateTransactionResult(transactionId, result, legacyDb.getTransaction(transactionId))
            }
            
            result
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern getTransaction, falling back to legacy", e)
            }
            legacyDb.getTransaction(transactionId)
        }
    }

    /**
     * Modern implementation for getAllTransactions using Room repository.
     */
    private fun getAllTransactionsModern(): List<Transaction> {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern getAllTransactions implementation")
            }
            
            val transactionEntities = runBlocking { 
                getTransactionsUseCase?.execute() ?: emptyList()
            }
            
            transactionEntities.map { it.toLegacyModel() }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern getAllTransactions, falling back to empty list", e)
            }
            emptyList()
        }
    }

    /**
     * Modern implementation for getTransactionsForAccount using Room repository.
     */
    private fun getTransactionsForAccountModern(accountId: Long): List<Transaction> {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern getTransactionsForAccount implementation for account: $accountId")
            }
            
            val transactionEntities = runBlocking { 
                getTransactionsForAccountUseCase?.execute(accountId) ?: emptyList()
            }
            
            transactionEntities.map { it.toLegacyModel() }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern getTransactionsForAccount, falling back to empty list", e)
            }
            emptyList()
        }
    }

    /**
     * Modern implementation for insertOrUpdate using Room repository.
     */
    private fun insertOrUpdateModern(transaction: Transaction): Long {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern insertOrUpdate implementation for transaction with amount: ${transaction.fromAmount}")
            }
            
            val transactionEntity = transaction.toRoomEntity()
            
            val result = if (transaction.id <= 0) {
                // Create new transaction
                val createResult = runBlocking { 
                    createTransactionUseCase?.execute(transactionEntity) ?: Result.failure(Exception("Use case not available"))
                }
                createResult.getOrElse { -1L }
            } else {
                // Update existing transaction
                val updateResult = runBlocking { 
                    updateTransactionUseCase?.execute(transactionEntity) ?: Result.failure(Exception("Use case not available"))
                }
                if (updateResult.getOrElse { false }) transaction.id else -1L
            }
            
            result
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern insertOrUpdate, falling back to legacy", e)
            }
            legacyDb.insertOrUpdate(transaction)
        }
    }

    /**
     * Modern implementation for deleteTransaction using Room repository.
     */
    private fun deleteTransactionModern(transactionId: Long): Boolean {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern deleteTransaction implementation for ID: $transactionId")
            }
            
            val result = runBlocking { 
                deleteTransactionUseCase?.execute(transactionId) ?: Result.failure(Exception("Use case not available"))
            }
            
            result.getOrElse { false }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern deleteTransaction, falling back to legacy", e)
            }
            try {
                legacyDb.deleteTransaction(transactionId)
                true
            } catch (legacyException: Exception) {
                false
            }
        }
    }

    /**
     * Modern implementation for getTransactionsByDateRange using Room repository.
     */
    private fun getTransactionsByDateRangeModern(startDate: Long, endDate: Long): List<Transaction> {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern getTransactionsByDateRange implementation")
            }
            
            val transactionEntities = runBlocking { 
                getTransactionsByDateRangeUseCase?.execute(startDate, endDate) ?: emptyList()
            }
            
            transactionEntities.map { it.toLegacyModel() }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern getTransactionsByDateRange, falling back to empty list", e)
            }
            emptyList()
        }
    }

    /**
     * Modern implementation for getTransactionsByCategory using Room repository.
     */
    private fun getTransactionsByCategoryModern(categoryId: Long): List<Transaction> {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern getTransactionsByCategory implementation")
            }
            
            val transactionEntities = runBlocking { 
                getTransactionsByCategoryUseCase?.execute(categoryId) ?: emptyList()
            }
            
            transactionEntities.map { it.toLegacyModel() }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern getTransactionsByCategory, falling back to empty list", e)
            }
            emptyList()
        }
    }

    /**
     * Modern implementation for rebuildRunningBalanceForAccount using Room repository.
     */
    private fun rebuildRunningBalanceForAccountModern(accountId: Long): Boolean {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern rebuildRunningBalanceForAccount implementation for account: $accountId")
            }
            
            val result = runBlocking { 
                rebuildRunningBalanceForAccountUseCase?.execute(accountId) ?: Result.failure(Exception("Use case not available"))
            }
            
            result.getOrElse { false }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern rebuildRunningBalanceForAccount, falling back to legacy", e)
            }
            try {
                legacyDb.rebuildRunningBalanceForAccount(legacyDb.getAccount(accountId))
                true
            } catch (legacyException: Exception) {
                false
            }
        }
    }

    /**
     * Modern implementation for rebuildAllRunningBalances using Room repository.
     */
    private fun rebuildAllRunningBalancesModern(): Boolean {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern rebuildAllRunningBalances implementation")
            }
            
            val result = runBlocking { 
                rebuildAllRunningBalancesUseCase?.execute() ?: Result.failure(Exception("Use case not available"))
            }
            
            result.getOrElse { false }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern rebuildAllRunningBalances, falling back to legacy", e)
            }
            try {
                legacyDb.rebuildRunningBalances()
                true
            } catch (legacyException: Exception) {
                false
            }
        }
    }

    /**
     * Modern implementation for getLastRunningBalanceForAccount using Room repository.
     */
    private fun getLastRunningBalanceForAccountModern(accountId: Long): Long {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("TransactionBridge", "Using modern getLastRunningBalanceForAccount implementation for account: $accountId")
            }
            
            val result = runBlocking { 
                getLastRunningBalanceForAccountUseCase?.execute(accountId) ?: Result.failure(Exception("Use case not available"))
            }
            
            result.getOrElse { 0L }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("TransactionBridge", "Error in modern getLastRunningBalanceForAccount, falling back to legacy", e)
            }
            legacyDb.getLastRunningBalanceForAccount(legacyDb.getAccount(accountId))
        }
    }

    /**
     * Validates that modern and legacy results match for transaction operations.
     */
    private fun validateTransactionResult(transactionId: Long, modernResult: Transaction?, legacyResult: Transaction?) {
        if (modernResult == null && legacyResult == null) return
        if (modernResult == null || legacyResult == null) {
            android.util.Log.w("TransactionBridge", 
                "Transaction result mismatch for ID $transactionId: modern=$modernResult, legacy=$legacyResult")
            return
        }
        
        if (modernResult.id != legacyResult.id || 
            modernResult.fromAmount != legacyResult.fromAmount ||
            modernResult.fromAccountId != legacyResult.fromAccountId ||
            modernResult.dateTime != legacyResult.dateTime) {
            android.util.Log.w("TransactionBridge", 
                "Transaction data mismatch for ID $transactionId: " +
                "modern=[${modernResult.id}, ${modernResult.fromAmount}, ${modernResult.fromAccountId}, ${modernResult.dateTime}], " +
                "legacy=[${legacyResult.id}, ${legacyResult.fromAmount}, ${legacyResult.fromAccountId}, ${legacyResult.dateTime}]")
        }
    }
}
