package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.TransactionEntity

/**
 * Data class for transaction with its splits
 */
data class TransactionWithSplits(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "_id",
        entityColumn = "parent_id"
    )
    val splits: List<TransactionEntity>
) {
    /**
     * Check if this is a split transaction (has splits)
     */
    val isSplitTransaction: Boolean
        get() = splits.isNotEmpty()
    
    /**
     * Get total amount of all splits
     */
    val totalSplitAmount: Long
        get() = splits.sumOf { it.fromAmount }
    
    /**
     * Get unsplit amount (remaining amount not allocated to splits)
     */
    val unsplitAmount: Long
        get() = transaction.fromAmount - totalSplitAmount
    
    /**
     * Check if split transaction is balanced (all amount is allocated)
     */
    val isBalanced: Boolean
        get() = unsplitAmount == 0L
    
    /**
     * Get number of splits
     */
    val splitCount: Int
        get() = splits.size
}

/**
 * Room DAO for Transaction operations.
 * 
 * Provides comprehensive transaction data access with support for
 * complex queries, filtering, and reactive data streams.
 */
@Dao
interface TransactionDao {
    
    /**
     * Get all transactions as Flow for reactive updates
     */
    @Query("SELECT * FROM transactions ORDER BY datetime DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>
    
    /**
     * Get all transactions as a one-time operation
     */
    @Query("SELECT * FROM transactions WHERE is_template = 0 ORDER BY datetime DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>
    
    /**
     * Get transactions for a specific account
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE (from_account_id = :accountId OR to_account_id = :accountId)
        AND is_template = 0
        ORDER BY datetime DESC
    """)
    fun getTransactionsForAccountFlow(accountId: Long): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions for a specific account as one-time operation
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE (from_account_id = :accountId OR to_account_id = :accountId)
        AND is_template = 0
        ORDER BY datetime DESC
    """)
    suspend fun getTransactionsForAccount(accountId: Long): List<TransactionEntity>
    
    /**
     * Get transaction by ID
     */
    @Query("SELECT * FROM transactions WHERE _id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?
    
    /**
     * Get transactions within date range
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE datetime BETWEEN :startDate AND :endDate
        AND is_template = 0
        ORDER BY datetime DESC
    """)
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<TransactionEntity>
    
    /**
     * Get transactions for category
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE category_id = :categoryId
        AND is_template = 0
        ORDER BY datetime DESC
    """)
    suspend fun getTransactionsByCategory(categoryId: Long): List<TransactionEntity>
    
    /**
     * Insert a new transaction
     */
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    
    /**
     * Update an existing transaction
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    /**
     * Delete a transaction
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    /**
     * Delete transaction by ID
     */
    @Query("DELETE FROM transactions WHERE _id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long)
    
    /**
     * Get transaction templates
     */
    @Query("SELECT * FROM transactions WHERE is_template = 1 ORDER BY template_name")
    suspend fun getTransactionTemplates(): List<TransactionEntity>
    
    /**
     * Get total amount for account within date range
     */
    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN from_account_id = :accountId THEN -from_amount
                WHEN to_account_id = :accountId THEN to_amount
                ELSE 0
            END
        ), 0) AS total
        FROM transactions 
        WHERE (from_account_id = :accountId OR to_account_id = :accountId)
        AND datetime BETWEEN :startDate AND :endDate
        AND is_template = 0
    """)
    suspend fun getTotalAmountForAccount(accountId: Long, startDate: Long, endDate: Long): Long
    
    /**
     * Get transaction count for account
     */
    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE (from_account_id = :accountId OR to_account_id = :accountId)
        AND is_template = 0
    """)
    suspend fun getTransactionCountForAccount(accountId: Long): Int
    
    /**
     * Search transactions by note
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE note LIKE '%' || :query || '%'
        AND is_template = 0
        ORDER BY datetime DESC
    """)
    suspend fun searchTransactionsByNote(query: String): List<TransactionEntity>
    
    /**
     * Get transactions for running balance calculation.
     * This is specifically ordered for running balance processing:
     * - Filters by account (from_account_id OR to_account_id)
     * - Excludes templates
     * - Orders by datetime ASC, then by id ASC for chronological processing
     * - Handles both regular transactions and transfers
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE (from_account_id = :accountId OR to_account_id = :accountId)
        AND is_template = 0
        ORDER BY datetime ASC, _id ASC
    """)
    suspend fun getTransactionsForRunningBalance(accountId: Long): List<TransactionEntity>
    
    // ========== Split Transaction Methods ==========
    
    /**
     * Get split transactions for a parent transaction
     */
    @Query("SELECT * FROM transactions WHERE parent_id = :parentId ORDER BY _id ASC")
    suspend fun getSplitTransactions(parentId: Long): List<TransactionEntity>
    
    /**
     * Get split transactions as Flow for reactive updates
     */
    @Query("SELECT * FROM transactions WHERE parent_id = :parentId ORDER BY _id ASC")
    fun getSplitTransactionsFlow(parentId: Long): Flow<List<TransactionEntity>>
    
    /**
     * Get transaction with its splits (if any)
     */
    @Transaction
    @Query("SELECT * FROM transactions WHERE _id = :transactionId")
    suspend fun getTransactionWithSplits(transactionId: Long): TransactionWithSplits?
    
    /**
     * Check if transaction has splits
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE parent_id = :transactionId")
    suspend fun getSplitCount(transactionId: Long): Int
    
    /**
     * Delete all splits for a parent transaction
     */
    @Query("DELETE FROM transactions WHERE parent_id = :parentId")
    suspend fun deleteSplitTransactions(parentId: Long)
    
    /**
     * Get blotter transactions (excludes split children, includes split parents)
     * Split parents have category_id = -1
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE is_template = 0 AND parent_id = 0 
        ORDER BY datetime DESC
    """)
    suspend fun getBlotterTransactions(): List<TransactionEntity>
    
    /**
     * Get blotter transactions as Flow
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE is_template = 0 AND parent_id = 0 
        ORDER BY datetime DESC
    """)
    fun getBlotterTransactionsFlow(): Flow<List<TransactionEntity>>
    
    /**
     * Check if a transaction is a split parent (has splits)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE parent_id = :transactionId)")
    suspend fun isSplitParent(transactionId: Long): Boolean
    
    /**
     * Check if a transaction is a split child (has parent)
     */
    @Query("SELECT parent_id > 0 FROM transactions WHERE _id = :transactionId")
    suspend fun isSplitChild(transactionId: Long): Boolean
    
    /**
     * Get all split parent transactions (transactions with splits)
     */
    @Query("""
        SELECT DISTINCT p.* FROM transactions p 
        WHERE EXISTS (SELECT 1 FROM transactions s WHERE s.parent_id = p._id)
        AND p.is_template = 0
        ORDER BY p.datetime DESC
    """)
    suspend fun getAllSplitParents(): List<TransactionEntity>
}
