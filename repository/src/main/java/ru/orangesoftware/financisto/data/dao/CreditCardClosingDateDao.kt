package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import ru.orangesoftware.financisto.data.model.CreditCardClosingDateEntity

/**
 * Room DAO for Credit Card Closing Date operations.
 */
@Dao
interface CreditCardClosingDateDao {

    /**
     * Get all credit card closing dates
     */
    @Query("SELECT * FROM ccard_closing_date")
    suspend fun getAllCreditCardClosingDates(): List<CreditCardClosingDateEntity>

    /**
     * Get closing date for a specific account
     */
    @Query("SELECT * FROM ccard_closing_date WHERE account_id = :accountId")
    suspend fun getClosingDateForAccount(accountId: Long): List<CreditCardClosingDateEntity>

    /**
     * Get closing date for a specific account and period
     */
    @Query("SELECT * FROM ccard_closing_date WHERE account_id = :accountId AND period = :period")
    suspend fun getClosingDate(accountId: Long, period: Int): CreditCardClosingDateEntity?

    /**
     * Insert a credit card closing date
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCardClosingDate(closingDate: CreditCardClosingDateEntity)

    /**
     * Update a credit card closing date
     */
    @Update
    suspend fun updateCreditCardClosingDate(closingDate: CreditCardClosingDateEntity)

    /**
     * Delete a credit card closing date
     */
    @Delete
    suspend fun deleteCreditCardClosingDate(closingDate: CreditCardClosingDateEntity)

    /**
     * Delete all closing dates for an account
     */
    @Query("DELETE FROM ccard_closing_date WHERE account_id = :accountId")
    suspend fun deleteClosingDatesForAccount(accountId: Long)
}