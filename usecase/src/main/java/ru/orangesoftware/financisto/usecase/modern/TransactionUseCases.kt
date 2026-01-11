package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.model.TransactionEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.AccountRepository
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
 * Updates running balance incrementally for a new or updated transaction.
 * This is more efficient than rebuilding all running balances.
 *
 * Mirrors the legacy approach of inserting a new running balance entry
 * and updating all subsequent entries.
 */
@Singleton
class UpdateRunningBalanceIncrementallyUseCase @Inject constructor(
    private val runningBalanceDao: ru.orangesoftware.financisto.data.dao.RunningBalanceDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Update running balance for a transaction incrementally.
     *
     * 1. Insert/update the running balance entry for this transaction
     * 2. Update all running balances after this transaction's datetime
     *
     * @param accountId The account ID
     * @param transactionId The transaction ID
     * @param transactionAmount The transaction amount (signed)
     * @param transactionDate The transaction datetime
     */
    suspend operator fun invoke(
        accountId: Long,
        transactionId: Long,
        transactionAmount: Long,
        transactionDate: Long
    ) = withContext(ioDispatcher) {
        // Get the balance just before this transaction
        val previousBalance = runningBalanceDao.getAccountBalanceAtTime(accountId, transactionDate - 1) ?: 0L
        
        // Calculate the new balance for this transaction
        val newBalance = previousBalance + transactionAmount
        
        // Insert the running balance entry for this transaction
        runningBalanceDao.insertRunningBalance(
            ru.orangesoftware.financisto.data.model.RunningBalanceEntity(
                accountId = accountId,
                transactionId = transactionId,
                datetime = transactionDate,
                balance = newBalance
            )
        )
        
        // Update all subsequent running balance entries
        runningBalanceDao.updateRunningBalancesAfterTime(accountId, transactionAmount, transactionDate)
    }
}

/**
 * Inserts a split transaction (parent + children) with proper balance updates.
 *
 * Mirrors the legacy DatabaseAdapter.insertSplits() logic:
 * - Parent transaction updates fromAccount balance
 * - Transfer children update toAccount balance
 * - Non-transfer children don't update any balance (already counted in parent)
 */
@Singleton
class InsertSplitTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val updateRunningBalanceIncrementallyUseCase: UpdateRunningBalanceIncrementallyUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Insert a split transaction with all its children atomically.
     *
     * @param parent The parent transaction (should have categoryId = -1)
     * @param children List of child transactions (will have parentId set to parent's ID after insertion)
     * @return Result containing the parent transaction ID if successful, or error
     */
    suspend operator fun invoke(
        parent: TransactionEntity,
        children: List<TransactionEntity>
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            // 1. Insert parent transaction
            val parentId = transactionRepository.insertTransaction(parent)
            
            // 2. Update parent's fromAccount balance and running balance
            accountRepository.incrementAccountBalance(parent.fromAccountId, parent.fromAmount, parent.datetime)
            
            updateRunningBalanceIncrementallyUseCase(
                accountId = parent.fromAccountId,
                transactionId = parentId,
                transactionAmount = parent.fromAmount,
                transactionDate = parent.datetime
            )
            
            // 3. Insert all child transactions
            for (child in children) {
                // Set the parentId to the newly inserted parent
                val childWithParent = child.copy(parentId = parentId)
                val childId = transactionRepository.insertTransaction(childWithParent)
                
                // 4. Update balances for transfer children only
                if (childWithParent.toAccountId > 0) {
                    // This is a transfer - update toAccount balance
                    accountRepository.updateAccountBalance(
                        accountId = childWithParent.toAccountId,
                        amount = childWithParent.toAmount,
                        lastTransactionDate = childWithParent.datetime
                    )
                    
                    updateRunningBalanceIncrementallyUseCase(
                        accountId = childWithParent.toAccountId,
                        transactionId = childId,
                        transactionAmount = childWithParent.toAmount,
                        transactionDate = childWithParent.datetime
                    )
                }
                // Note: Non-transfer children don't update fromAccount because
                // the parent transaction already did that
            }
            
            Result.success(parentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
