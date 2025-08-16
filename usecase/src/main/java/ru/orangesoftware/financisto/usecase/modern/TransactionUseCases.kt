package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.model.TransactionEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for transaction-related business logic.
 * 
 * This demonstrates modern Clean Architecture patterns:
 * - Single responsibility (only transaction operations)
 * - Dependency injection with Hilt
 * - Coroutines for async operations
 * - Flow for reactive data streams
 * - Proper error handling and business logic separation
 */
@Singleton
class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Get all transactions as a one-time operation
     */
    suspend fun execute(): List<TransactionEntity> = withContext(ioDispatcher) {
        try {
            transactionRepository.getAllTransactions()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get all transactions as a reactive stream
     */
    fun executeAsFlow(): Flow<List<TransactionEntity>> {
        return transactionRepository.getAllTransactionsFlow()
            .flowOn(ioDispatcher)
    }
}

/**
 * Use case for getting transactions for a specific account
 */
@Singleton
class GetTransactionsForAccountUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(accountId: Long): List<TransactionEntity> = withContext(ioDispatcher) {
        try {
            transactionRepository.getTransactionsForAccount(accountId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun executeAsFlow(accountId: Long): Flow<List<TransactionEntity>> {
        return transactionRepository.getTransactionsForAccountFlow(accountId)
            .flowOn(ioDispatcher)
    }
}

/**
 * Use case for getting a specific transaction by ID
 */
@Singleton
class GetTransactionByIdUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(transactionId: Long): TransactionEntity? = withContext(ioDispatcher) {
        try {
            transactionRepository.getTransactionById(transactionId)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Use case for creating a new transaction
 */
@Singleton
class CreateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(transaction: TransactionEntity): Result<Long> = withContext(ioDispatcher) {
        try {
            val transactionId = transactionRepository.insertTransaction(transaction)
            Result.success(transactionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for updating an existing transaction
 */
@Singleton
class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(transaction: TransactionEntity): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val success = transactionRepository.updateTransaction(transaction)
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for deleting a transaction
 */
@Singleton
class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(transactionId: Long): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val success = transactionRepository.deleteTransaction(transactionId)
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for getting transactions within a date range
 */
@Singleton
class GetTransactionsByDateRangeUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(startDate: Long, endDate: Long): List<TransactionEntity> = withContext(ioDispatcher) {
        try {
            transactionRepository.getTransactionsByDateRange(startDate, endDate)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Use case for getting transactions by category
 */
@Singleton
class GetTransactionsByCategoryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(categoryId: Long): List<TransactionEntity> = withContext(ioDispatcher) {
        try {
            transactionRepository.getTransactionsByCategory(categoryId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
