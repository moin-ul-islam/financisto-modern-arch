package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import ru.orangesoftware.financisto.data.model.CurrencyExchangeRateEntity

/**
 * Room DAO for Currency Exchange Rate operations.
 */
@Dao
interface CurrencyExchangeRateDao {

    /**
     * Get all exchange rates
     */
    @Query("SELECT * FROM currency_exchange_rate ORDER BY rate_date DESC")
    suspend fun getAllExchangeRates(): List<CurrencyExchangeRateEntity>

    /**
     * Get exchange rates for a currency pair
     */
    @Query("SELECT * FROM currency_exchange_rate WHERE from_currency_id = :fromCurrencyId AND to_currency_id = :toCurrencyId ORDER BY rate_date DESC")
    suspend fun getExchangeRatesForPair(fromCurrencyId: Long, toCurrencyId: Long): List<CurrencyExchangeRateEntity>

    /**
     * Get latest exchange rate for a currency pair
     */
    @Query("SELECT * FROM currency_exchange_rate WHERE from_currency_id = :fromCurrencyId AND to_currency_id = :toCurrencyId ORDER BY rate_date DESC LIMIT 1")
    suspend fun getLatestExchangeRate(fromCurrencyId: Long, toCurrencyId: Long): CurrencyExchangeRateEntity?

    /**
     * Get exchange rate for a specific date
     */
    @Query("SELECT * FROM currency_exchange_rate WHERE from_currency_id = :fromCurrencyId AND to_currency_id = :toCurrencyId AND rate_date = :date")
    suspend fun getExchangeRateAtDate(fromCurrencyId: Long, toCurrencyId: Long, date: Long): CurrencyExchangeRateEntity?

    /**
     * Get exchange rate closest to a specific date (most recent before or on the date)
     */
    @Query("SELECT * FROM currency_exchange_rate WHERE from_currency_id = :fromCurrencyId AND to_currency_id = :toCurrencyId AND rate_date <= :date ORDER BY rate_date DESC LIMIT 1")
    suspend fun getExchangeRateClosestToDate(fromCurrencyId: Long, toCurrencyId: Long, date: Long): CurrencyExchangeRateEntity?

    /**
     * Insert an exchange rate
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRate(exchangeRate: CurrencyExchangeRateEntity)

    /**
     * Insert multiple exchange rates
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(exchangeRates: List<CurrencyExchangeRateEntity>)

    /**
     * Update an exchange rate
     */
    @Update
    suspend fun updateExchangeRate(exchangeRate: CurrencyExchangeRateEntity)

    /**
     * Delete an exchange rate
     */
    @Delete
    suspend fun deleteExchangeRate(exchangeRate: CurrencyExchangeRateEntity)

    /**
     * Delete all exchange rates for a currency pair
     */
    @Query("DELETE FROM currency_exchange_rate WHERE from_currency_id = :fromCurrencyId AND to_currency_id = :toCurrencyId")
    suspend fun deleteExchangeRatesForPair(fromCurrencyId: Long, toCurrencyId: Long)

    /**
     * Delete exchange rates older than a specific date
     */
    @Query("DELETE FROM currency_exchange_rate WHERE rate_date < :date")
    suspend fun deleteOldExchangeRates(date: Long)
}