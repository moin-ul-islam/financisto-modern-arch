package ru.orangesoftware.financisto.data.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.BlotterView

/**
 * Room DAO for querying the BlotterView.
 * 
 * The BlotterView (v_blotter) is a database view that pre-joins transactions with:
 * - Accounts (from and to)
 * - Categories
 * - Currencies
 * - Projects
 * - Locations
 * - Payees
 * - Running balances
 * 
 * The view uses a UNION to show both sides of transfers:
 * - First part: Transactions from the "from_account" perspective
 * - Second part: Transactions from the "to_account" perspective (with swapped amounts and is_transfer=-1)
 * 
 * This matches the legacy app's v_blotter_for_account_with_splits view.
 */
@Dao
interface BlotterDao {
    
    /**
     * Get blotter entries for a specific account.
     * 
     * This query filters the BlotterView for:
     * - Transactions where the account is either from_account or to_account
     * - Parent transactions only (parent_id = 0) OR the "to_account" side of transfers (is_transfer = -1)
     * 
     * This filter logic matches the legacy app's enhanceFilterForAccountBlotter:
     * ```
     * parent_id = 0 OR is_transfer = -1
     * ```
     * 
     * The reason for this filter:
     * - parent_id = 0: Shows only parent transactions (not individual splits)
     * - is_transfer = -1: Also shows the "to_account" side of transfers (from the UNION)
     * 
     * @param accountId The account ID to filter by
     * @return List of BlotterView entries ordered by most recent first
     */
    @Query("""
        SELECT * FROM v_blotter 
        WHERE from_account_id = :accountId 
        AND (parent_id = 0 OR is_transfer = -1)
        ORDER BY datetime DESC, _id DESC
    """)
    suspend fun getBlotterForAccount(accountId: Long): List<BlotterView>
    
    /**
     * Get blotter entries for a specific account as a Flow for reactive updates.
     * 
     * @param accountId The account ID to filter by
     * @return Flow of BlotterView entries ordered by most recent first
     */
    @Query("""
        SELECT * FROM v_blotter 
        WHERE from_account_id = :accountId 
        AND (parent_id = 0 OR is_transfer = -1)
        ORDER BY datetime DESC, _id DESC
    """)
    fun observeBlotterForAccount(accountId: Long): Flow<List<BlotterView>>
    
    /**
     * Get all blotter entries across all accounts.
     * 
     * Only shows parent transactions (not splits).
     * 
     * @return List of BlotterView entries ordered by most recent first
     */
    @Query("""
        SELECT * FROM v_blotter 
        WHERE parent_id = 0
        ORDER BY datetime DESC, _id DESC
    """)
    suspend fun getAllBlotter(): List<BlotterView>
    
    /**
     * Get all blotter entries across all accounts as a Flow.
     * 
     * @return Flow of BlotterView entries ordered by most recent first
     */
    @Query("""
        SELECT * FROM v_blotter 
        WHERE parent_id = 0
        ORDER BY datetime DESC, _id DESC
    """)
    fun observeAllBlotter(): Flow<List<BlotterView>>
    
    /**
     * Get blotter entries within a date range for a specific account.
     * 
     * @param accountId The account ID to filter by
     * @param fromDate Start of date range (inclusive)
     * @param toDate End of date range (inclusive)
     * @return List of BlotterView entries in the date range
     */
    @Query("""
        SELECT * FROM v_blotter 
        WHERE from_account_id = :accountId 
        AND (parent_id = 0 OR is_transfer = -1)
        AND datetime BETWEEN :fromDate AND :toDate
        ORDER BY datetime DESC, _id DESC
    """)
    suspend fun getBlotterForAccountInDateRange(
        accountId: Long,
        fromDate: Long,
        toDate: Long
    ): List<BlotterView>
}
