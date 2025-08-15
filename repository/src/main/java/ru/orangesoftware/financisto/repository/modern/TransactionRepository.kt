package ru.orangesoftware.financisto.repository.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.data.model.TransactionEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern repository interface for transaction data operations.
 * 
 * This interface defines the contract for transaction data access
 * using modern patterns (Coroutines, Flow) with Room entities.
 */
interface TransactionRepository {
    suspend fun getAllTransactions(): List<TransactionEntity>
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>
    suspend fun getTransactionById(id: Long): TransactionEntity?
    suspend fun getTransactionsForAccount(accountId: Long): List<TransactionEntity>
    fun getTransactionsForAccountFlow(accountId: Long): Flow<List<TransactionEntity>>
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<TransactionEntity>
    suspend fun getTransactionsByCategory(categoryId: Long): List<TransactionEntity>
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun updateTransaction(transaction: TransactionEntity): Boolean
    suspend fun deleteTransaction(id: Long): Boolean
    suspend fun getTransactionTemplates(): List<TransactionEntity>
    suspend fun getTotalAmountForAccount(accountId: Long, startDate: Long, endDate: Long): Long
}

/**
 * Implementation of TransactionRepository using Room DAOs and Hilt DI.
 * 
 * This repository implementation:
 * - Uses Room DAOs for type-safe database operations
 * - Uses Coroutines for async operations
 * - Uses Flow for reactive data streams
 * - Provides error handling and transaction safety
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val ioDispatcher: CoroutineDispatcher
) : TransactionRepository {

    override suspend fun getAllTransactions(): List<TransactionEntity> = withContext(ioDispatcher) {
        transactionDao.getAllTransactions()
    }

    override fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> = 
        transactionDao.getAllTransactionsFlow().flowOn(ioDispatcher)

    override suspend fun getTransactionById(id: Long): TransactionEntity? = withContext(ioDispatcher) {
        transactionDao.getTransactionById(id)
    }

    override suspend fun getTransactionsForAccount(accountId: Long): List<TransactionEntity> = 
        withContext(ioDispatcher) {
            transactionDao.getTransactionsForAccount(accountId)
        }

    override fun getTransactionsForAccountFlow(accountId: Long): Flow<List<TransactionEntity>> = 
        transactionDao.getTransactionsForAccountFlow(accountId).flowOn(ioDispatcher)

    override suspend fun getTransactionsByDateRange(
        startDate: Long, 
        endDate: Long
    ): List<TransactionEntity> = withContext(ioDispatcher) {
        transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    override suspend fun getTransactionsByCategory(categoryId: Long): List<TransactionEntity> = 
        withContext(ioDispatcher) {
            transactionDao.getTransactionsByCategory(categoryId)
        }

    override suspend fun insertTransaction(transaction: TransactionEntity): Long = 
        withContext(ioDispatcher) {
            transactionDao.insertTransaction(transaction)
        }

    override suspend fun updateTransaction(transaction: TransactionEntity): Boolean = 
        withContext(ioDispatcher) {
            try {
                transactionDao.updateTransaction(transaction)
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun deleteTransaction(id: Long): Boolean = withContext(ioDispatcher) {
        try {
            transactionDao.deleteTransactionById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getTransactionTemplates(): List<TransactionEntity> = 
        withContext(ioDispatcher) {
            transactionDao.getTransactionTemplates()
        }

    override suspend fun getTotalAmountForAccount(
        accountId: Long, 
        startDate: Long, 
        endDate: Long
    ): Long = withContext(ioDispatcher) {
        transactionDao.getTotalAmountForAccount(accountId, startDate, endDate)
    }
}
