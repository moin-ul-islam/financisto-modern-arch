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

/**
 * Enhanced use case for creating a transaction with balance updates
 */
@Singleton
class CreateTransactionWithBalanceUpdateUseCase @Inject constructor(
    private val insertOrUpdateTransactionUseCase: InsertOrUpdateTransactionUseCase,
    private val recalculateAccountBalanceUseCase: RecalculateAccountBalanceUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Creates a transaction and updates account balances.
     * This ensures account balances are kept in sync with transactions.
     *
     * @param transaction The transaction to create
     * @param attributes Optional transaction attributes
     * @return Result with transaction ID on success
     */
    suspend fun execute(
        transaction: TransactionEntity,
        attributes: List<ru.orangesoftware.financisto.data.model.TransactionAttributeEntity> = emptyList()
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            // Insert the transaction
            val result = insertOrUpdateTransactionUseCase.execute(transaction, attributes)

            if (result.isSuccess) {
                val transactionId = result.getOrThrow()

                // Update balances for affected accounts
                if (transaction.fromAccountId > 0) {
                    recalculateAccountBalanceUseCase.execute(transaction.fromAccountId)
                }
                if (transaction.toAccountId > 0 && transaction.toAccountId != transaction.fromAccountId) {
                    recalculateAccountBalanceUseCase.execute(transaction.toAccountId)
                }

                Result.success(transactionId)
            } else {
                result
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for getting transaction templates
 */
@Singleton
class GetTransactionTemplatesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Gets all transaction templates for reuse.
     *
     * @return Result with list of transaction templates
     */
    suspend fun execute(): Result<List<TransactionEntity>> = withContext(ioDispatcher) {
        try {
            val templates = transactionRepository.getTransactionTemplates()
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for searching transactions across multiple fields
 */
@Singleton
class SearchTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Searches transactions by note content.
     * In a full implementation, this would search across notes, payee names, etc.
     *
     * @param query The search query
     * @param accountId Optional account ID to limit search scope
     * @return Result with list of matching transactions
     */
    suspend fun execute(query: String, accountId: Long? = null): Result<List<TransactionEntity>> = withContext(ioDispatcher) {
        try {
            // For now, we'll use the existing search by note
            // In a real implementation, this would be more comprehensive
            val transactions = if (accountId != null) {
                transactionRepository.getTransactionsForAccount(accountId)
                    .filter { it.note?.contains(query, ignoreCase = true) == true }
            } else {
                // This would need to be implemented in the repository
                // For now, return empty list
                emptyList()
            }

            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}