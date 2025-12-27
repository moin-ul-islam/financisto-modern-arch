package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.PayeeEntity

/**
 * Room DAO for Payee operations.
 */
@Dao
interface PayeeDao {

    /**
     * Get all payees as a Flow for reactive updates
     */
    @Query("SELECT * FROM payee WHERE is_active = 1 ORDER BY sort_order, title")
    fun getAllPayeesFlow(): Flow<List<PayeeEntity>>

    /**
     * Get all payees as a one-time operation
     */
    @Query("SELECT * FROM payee WHERE is_active = 1 ORDER BY sort_order, title")
    suspend fun getAllPayees(): List<PayeeEntity>

    /**
     * Get payee by ID
     */
    @Query("SELECT * FROM payee WHERE _id = :payeeId")
    suspend fun getPayeeById(payeeId: Long): PayeeEntity?

    /**
     * Insert a new payee
     */
    @Insert
    suspend fun insertPayee(payee: PayeeEntity): Long

    /**
     * Update an existing payee
     */
    @Update
    suspend fun updatePayee(payee: PayeeEntity)

    /**
     * Delete a payee
     */
    @Delete
    suspend fun deletePayee(payee: PayeeEntity)

    /**
     * Delete payee by ID
     */
    @Query("DELETE FROM payee WHERE _id = :payeeId")
    suspend fun deletePayeeById(payeeId: Long)

    /**
     * Get payee count
     */
    @Query("SELECT COUNT(*) FROM payee WHERE is_active = 1")
    suspend fun getPayeeCount(): Int

    /**
     * Search payees by title
     */
    @Query("SELECT * FROM payee WHERE is_active = 1 AND title LIKE '%' || :query || '%' ORDER BY title")
    suspend fun searchPayees(query: String): List<PayeeEntity>
}