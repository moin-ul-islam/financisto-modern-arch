package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.AccountEntity

/**
 * Room DAO for Account operations.
 * 
 * Provides type-safe database operations for accounts using modern
 * coroutines and Flow patterns for reactive data access.
 */
@Dao
interface AccountDao {
    
    /**
     * Get all accounts as a Flow for reactive updates
     */
    @Query("SELECT * FROM account WHERE is_active = 1 ORDER BY sort_order, title")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>
    
    /**
     * Get all accounts as a one-time operation
     */
    @Query("SELECT * FROM account WHERE is_active = 1 ORDER BY sort_order, title")
    suspend fun getAllAccounts(): List<AccountEntity>
    
    /**
     * Get account by ID
     */
    @Query("SELECT * FROM account WHERE _id = :accountId")
    suspend fun getAccountById(accountId: Long): AccountEntity?
    
    /**
     * Get accounts that are included in totals
     */
    @Query("SELECT * FROM account WHERE is_active = 1 AND is_include_into_totals = 1 ORDER BY sort_order, title")
    suspend fun getAccountsIncludedInTotals(): List<AccountEntity>
    
    /**
     * Insert a new account
     */
    @Insert
    suspend fun insertAccount(account: AccountEntity): Long
    
    /**
     * Update an existing account
     */
    @Update
    suspend fun updateAccount(account: AccountEntity)
    
    /**
     * Delete an account
     */
    @Delete
    suspend fun deleteAccount(account: AccountEntity)
    
    /**
     * Delete account by ID
     */
    @Query("DELETE FROM account WHERE _id = :accountId")
    suspend fun deleteAccountById(accountId: Long)
    
    /**
     * Update account balance
     */
    @Query("UPDATE account SET total_amount = :amount, last_transaction_date = :lastTransactionDate WHERE _id = :accountId")
    suspend fun updateAccountBalance(accountId: Long, amount: Long, lastTransactionDate: Long)
    
    /**
     * Increment account balance by delta amount atomically.
     * This is more efficient than fetching, calculating, and updating.
     * Returns the number of rows updated (should be 1 if successful).
     */
    @Query("UPDATE account SET total_amount = total_amount + :deltaAmount, last_transaction_date = :lastTransactionDate WHERE _id = :accountId")
    suspend fun incrementAccountBalance(accountId: Long, deltaAmount: Long, lastTransactionDate: Long): Int
    
    /**
     * Get account count
     */
    @Query("SELECT COUNT(*) FROM account WHERE is_active = 1")
    suspend fun getAccountCount(): Int
    
    /**
     * Search accounts by title
     */
    @Query("SELECT * FROM account WHERE is_active = 1 AND title LIKE '%' || :query || '%' ORDER BY title")
    suspend fun searchAccounts(query: String): List<AccountEntity>
}
