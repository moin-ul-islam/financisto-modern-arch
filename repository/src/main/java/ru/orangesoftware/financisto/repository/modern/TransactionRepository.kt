package ru.orangesoftware.financisto.repository.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.data.dao.TransactionWithSplits
import ru.orangesoftware.financisto.data.model.TransactionEntity
import ru.orangesoftware.financisto.di.IoDispatcher
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
    suspend fun getTransactionCountForAccount(accountId: Long): Int
    
    // ========== Split Transaction Methods ==========
    suspend fun getSplitTransactions(parentId: Long): List<TransactionEntity>
    fun getSplitTransactionsFlow(parentId: Long): Flow<List<TransactionEntity>>
    suspend fun getTransactionWithSplits(transactionId: Long): TransactionWithSplits?
    suspend fun getSplitCount(transactionId: Long): Int
    suspend fun isSplitTransaction(transactionId: Long): Boolean
    suspend fun isSplitParent(transactionId: Long): Boolean
    suspend fun isSplitChild(transactionId: Long): Boolean
    suspend fun deleteSplitTransactions(parentId: Long): Boolean
    suspend fun getBlotterTransactions(): List<TransactionEntity>
    fun getBlotterTransactionsFlow(): Flow<List<TransactionEntity>>
    suspend fun getAllSplitParents(): List<TransactionEntity>
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
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

    override suspend fun getTransactionCountForAccount(accountId: Long): Int = withContext(ioDispatcher) {
        transactionDao.getTransactionCountForAccount(accountId)
    }
    
    // ========== Split Transaction Methods Implementation ==========
    
    override suspend fun getSplitTransactions(parentId: Long): List<TransactionEntity> = 
        withContext(ioDispatcher) {
            transactionDao.getSplitTransactions(parentId)
        }
    
    override fun getSplitTransactionsFlow(parentId: Long): Flow<List<TransactionEntity>> = 
        transactionDao.getSplitTransactionsFlow(parentId).flowOn(ioDispatcher)
    
    override suspend fun getTransactionWithSplits(transactionId: Long): TransactionWithSplits? = 
        withContext(ioDispatcher) {
            transactionDao.getTransactionWithSplits(transactionId)
        }
    
    override suspend fun getSplitCount(transactionId: Long): Int = withContext(ioDispatcher) {
        transactionDao.getSplitCount(transactionId)
    }
    
    override suspend fun isSplitTransaction(transactionId: Long): Boolean = withContext(ioDispatcher) {
        transactionDao.getSplitCount(transactionId) > 0
    }
    
    override suspend fun isSplitParent(transactionId: Long): Boolean = withContext(ioDispatcher) {
        transactionDao.isSplitParent(transactionId)
    }
    
    override suspend fun isSplitChild(transactionId: Long): Boolean = withContext(ioDispatcher) {
        transactionDao.isSplitChild(transactionId)
    }
    
    override suspend fun deleteSplitTransactions(parentId: Long): Boolean = withContext(ioDispatcher) {
        try {
            transactionDao.deleteSplitTransactions(parentId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getBlotterTransactions(): List<TransactionEntity> = withContext(ioDispatcher) {
        transactionDao.getBlotterTransactions()
    }
    
    override fun getBlotterTransactionsFlow(): Flow<List<TransactionEntity>> = 
        transactionDao.getBlotterTransactionsFlow().flowOn(ioDispatcher)
    
    override suspend fun getAllSplitParents(): List<TransactionEntity> = withContext(ioDispatcher) {
        transactionDao.getAllSplitParents()
    }
}
