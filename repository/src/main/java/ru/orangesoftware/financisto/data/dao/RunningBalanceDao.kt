package ru.orangesoftware.financisto.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.orangesoftware.financisto.data.model.RunningBalanceEntity

/**
 * Data Access Object for Running Balance operations.
 * 
 * This DAO handles all database operations for running balance calculations,
 * which are critical for maintaining cumulative account balances over time.
 * 
 * Running balance is complex business logic that must be preserved exactly
 * during the migration from legacy SQLite to Room.
 */
@Dao
interface RunningBalanceDao {

    /**
     * Insert or replace a running balance entry.
     * Used during running balance rebuilds.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunningBalance(runningBalance: RunningBalanceEntity)

    /**
     * Insert multiple running balance entries.
     * Used for batch operations during rebuilds.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunningBalances(runningBalances: List<RunningBalanceEntity>)

    /**
     * Delete all running balance entries for a specific account.
     * Used before rebuilding running balance for an account.
     */
    @Query("DELETE FROM running_balance WHERE account_id = :accountId")
    suspend fun deleteRunningBalanceForAccount(accountId: Long)

    /**
     * Delete a specific running balance entry.
     * Used when deleting transactions.
     */
    @Query("DELETE FROM running_balance WHERE account_id = :accountId AND transaction_id = :transactionId")
    suspend fun deleteRunningBalance(accountId: Long, transactionId: Long)

    /**
     * Delete all running balance entries.
     * Used for full rebuilds.
     */
    @Query("DELETE FROM running_balance")
    suspend fun deleteAllRunningBalances()

    /**
     * Get the last (most recent) running balance for an account.
     * This is equivalent to DatabaseAdapter.getLastRunningBalanceForAccount().
     * 
     * Orders by datetime desc, transaction_id desc to handle transactions with same datetime.
     */
    @Query("""
        SELECT balance FROM running_balance 
        WHERE account_id = :accountId 
        ORDER BY datetime DESC, transaction_id DESC 
        LIMIT 1
    """)
    suspend fun getLastRunningBalanceForAccount(accountId: Long): Long?

    /**
     * Get account balance at a specific time.
     * This is equivalent to DatabaseAdapter.fetchAccountBalanceAtTheTime().
     * 
     * Returns the balance from the latest transaction up to the given datetime.
     */
    @Query("""
        SELECT balance FROM running_balance 
        WHERE account_id = :accountId AND datetime <= :datetime 
        ORDER BY datetime DESC, transaction_id DESC 
        LIMIT 1
    """)
    suspend fun getAccountBalanceAtTime(accountId: Long, datetime: Long): Long?

    /**
     * Update all running balances for an account after a specific datetime.
     * Used when inserting/updating transactions to update subsequent balances.
     */
    @Query("""
        UPDATE running_balance 
        SET balance = balance + :deltaAmount 
        WHERE account_id = :accountId AND datetime > :datetime
    """)
    suspend fun updateRunningBalancesAfterTime(accountId: Long, deltaAmount: Long, datetime: Long)

    /**
     * Get all running balance entries for an account ordered by datetime.
     * Used for debugging and validation.
     */
    @Query("""
        SELECT * FROM running_balance 
        WHERE account_id = :accountId 
        ORDER BY datetime ASC, transaction_id ASC
    """)
    suspend fun getRunningBalancesForAccount(accountId: Long): List<RunningBalanceEntity>

    /**
     * Get count of running balance entries for an account.
     * Used for validation and debugging.
     */
    @Query("SELECT COUNT(*) FROM running_balance WHERE account_id = :accountId")
    suspend fun getRunningBalanceCountForAccount(accountId: Long): Int
}
