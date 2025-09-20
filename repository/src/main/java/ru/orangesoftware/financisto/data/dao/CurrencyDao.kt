package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.CurrencyEntity

/**
 * Room DAO for Currency operations.
 *
 * Provides type-safe database operations for currencies using modern
 * coroutines and Flow patterns for reactive data access.
 */
@Dao
interface CurrencyDao {

    /**
     * Get all currencies as a Flow for reactive updates
     */
    @Query("SELECT * FROM currency ORDER BY name")
    fun getAllCurrenciesFlow(): Flow<List<CurrencyEntity>>

    /**
     * Get all currencies as a one-time operation
     */
    @Query("SELECT * FROM currency ORDER BY name")
    suspend fun getAllCurrencies(): List<CurrencyEntity>

    /**
     * Get currency by ID
     */
    @Query("SELECT * FROM currency WHERE _id = :currencyId")
    suspend fun getCurrencyById(currencyId: Long): CurrencyEntity?

    /**
     * Get default currency
     */
    @Query("SELECT * FROM currency WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultCurrency(): CurrencyEntity?

    /**
     * Insert a new currency
     */
    @Insert
    suspend fun insertCurrency(currency: CurrencyEntity): Long

    /**
     * Update an existing currency
     */
    @Update
    suspend fun updateCurrency(currency: CurrencyEntity)

    /**
     * Delete a currency
     */
    @Delete
    suspend fun deleteCurrency(currency: CurrencyEntity)

    /**
     * Check if currency exists by name
     */
    @Query("SELECT COUNT(*) FROM currency WHERE name = :name")
    suspend fun currencyExistsByName(name: String): Int

    /**
     * Get currencies count
     */
    @Query("SELECT COUNT(*) FROM currency")
    suspend fun getCurrenciesCount(): Int
}