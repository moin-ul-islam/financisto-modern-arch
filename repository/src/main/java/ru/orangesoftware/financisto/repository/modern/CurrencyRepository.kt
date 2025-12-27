package ru.orangesoftware.financisto.repository.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.CurrencyDao
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern repository interface for currency data operations.
 *
 * This interface defines the contract for currency data access
 * using modern patterns (Coroutines, Flow) with Room entities.
 */
interface CurrencyRepository {
    suspend fun getAllCurrencies(): List<CurrencyEntity>
    fun getAllCurrenciesFlow(): Flow<List<CurrencyEntity>>
    suspend fun getCurrencyById(id: Long): CurrencyEntity?
    suspend fun getDefaultCurrency(): CurrencyEntity?
    suspend fun insertCurrency(currency: CurrencyEntity): Long
    suspend fun updateCurrency(currency: CurrencyEntity): Boolean
    suspend fun deleteCurrency(currency: CurrencyEntity): Boolean
    suspend fun currencyExistsByName(name: String): Boolean
    suspend fun getCurrenciesCount(): Int
}

/**
 * Implementation of CurrencyRepository using Room DAOs and Hilt DI.
 *
 * This repository implementation:
 * - Uses Room DAOs for type-safe database operations
 * - Uses Coroutines for async operations
 * - Uses Flow for reactive data streams
 * - Provides error handling and transaction safety
 */
@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val currencyDao: CurrencyDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CurrencyRepository {

    override suspend fun getAllCurrencies(): List<CurrencyEntity> = withContext(ioDispatcher) {
        currencyDao.getAllCurrencies()
    }

    override fun getAllCurrenciesFlow(): Flow<List<CurrencyEntity>> =
        currencyDao.getAllCurrenciesFlow().flowOn(ioDispatcher)

    override suspend fun getCurrencyById(id: Long): CurrencyEntity? = withContext(ioDispatcher) {
        currencyDao.getCurrencyById(id)
    }

    override suspend fun getDefaultCurrency(): CurrencyEntity? = withContext(ioDispatcher) {
        currencyDao.getDefaultCurrency()
    }

    override suspend fun insertCurrency(currency: CurrencyEntity): Long = withContext(ioDispatcher) {
        currencyDao.insertCurrency(currency)
    }

    override suspend fun updateCurrency(currency: CurrencyEntity): Boolean = withContext(ioDispatcher) {
        try {
            currencyDao.updateCurrency(currency)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteCurrency(currency: CurrencyEntity): Boolean = withContext(ioDispatcher) {
        try {
            currencyDao.deleteCurrency(currency)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun currencyExistsByName(name: String): Boolean = withContext(ioDispatcher) {
        currencyDao.currencyExistsByName(name) > 0
    }

    override suspend fun getCurrenciesCount(): Int = withContext(ioDispatcher) {
        currencyDao.getCurrenciesCount()
    }
}