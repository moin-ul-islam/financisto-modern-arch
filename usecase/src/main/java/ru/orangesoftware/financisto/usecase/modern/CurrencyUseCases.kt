package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.CurrencyRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for getting all currencies.
 */
@Singleton
class GetCurrenciesUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Get all currencies as a one-time operation
     */
    suspend fun execute(): List<CurrencyEntity> = withContext(ioDispatcher) {
        try {
            currencyRepository.getAllCurrencies()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get all currencies as a reactive stream
     */
    fun executeAsFlow(): Flow<List<CurrencyEntity>> {
        return currencyRepository.getAllCurrenciesFlow()
            .flowOn(ioDispatcher)
    }
}

/**
 * Use case for getting a specific currency by ID
 */
@Singleton
class GetCurrencyByIdUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(currencyId: Long): CurrencyEntity? = withContext(ioDispatcher) {
        try {
            currencyRepository.getCurrencyById(currencyId)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Use case for creating a new currency
 */
@Singleton
class CreateCurrencyUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(currency: CurrencyEntity): Result<Long> = withContext(ioDispatcher) {
        try {
            // Check if currency with same name already exists
            if (currencyRepository.currencyExistsByName(currency.name)) {
                Result.failure(Exception("Currency with name '${currency.name}' already exists"))
            } else {
                val currencyId = currencyRepository.insertCurrency(currency)
                Result.success(currencyId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for updating an existing currency
 */
@Singleton
class UpdateCurrencyUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(currency: CurrencyEntity): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val success = currencyRepository.updateCurrency(currency)
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for deleting a currency
 */
@Singleton
class DeleteCurrencyUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(currencyId: Long): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val currency = currencyRepository.getCurrencyById(currencyId)
            if (currency != null) {
                val success = currencyRepository.deleteCurrency(currency)
                Result.success(success)
            } else {
                Result.failure(Exception("Currency not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}