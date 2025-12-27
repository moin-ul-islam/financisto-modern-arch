package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.BudgetEntity

/**
 * Room DAO for Budget operations.
 */
@Dao
interface BudgetDao {

    /**
     * Get all budgets as a Flow for reactive updates
     */
    @Query("SELECT * FROM budget ORDER BY sort_order, title")
    fun getAllBudgetsFlow(): Flow<List<BudgetEntity>>

    /**
     * Get all budgets as a one-time operation
     */
    @Query("SELECT * FROM budget ORDER BY sort_order, title")
    suspend fun getAllBudgets(): List<BudgetEntity>

    /**
     * Get budget by ID
     */
    @Query("SELECT * FROM budget WHERE _id = :budgetId")
    suspend fun getBudgetById(budgetId: Long): BudgetEntity?

    /**
     * Get budgets for a specific account
     */
    @Query("SELECT * FROM budget WHERE budget_account_id = :accountId ORDER BY sort_order, title")
    suspend fun getBudgetsForAccount(accountId: Long): List<BudgetEntity>

    /**
     * Get current budgets (is_current = 1)
     */
    @Query("SELECT * FROM budget WHERE is_current = 1 ORDER BY sort_order, title")
    suspend fun getCurrentBudgets(): List<BudgetEntity>

    /**
     * Insert a new budget
     */
    @Insert
    suspend fun insertBudget(budget: BudgetEntity): Long

    /**
     * Update an existing budget
     */
    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    /**
     * Delete a budget
     */
    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    /**
     * Delete budget by ID
     */
    @Query("DELETE FROM budget WHERE _id = :budgetId")
    suspend fun deleteBudgetById(budgetId: Long)

    /**
     * Get budget count
     */
    @Query("SELECT COUNT(*) FROM budget")
    suspend fun getBudgetCount(): Int

    /**
     * Search budgets by title
     */
    @Query("SELECT * FROM budget WHERE title LIKE '%' || :query || '%' ORDER BY title")
    suspend fun searchBudgets(query: String): List<BudgetEntity>
}