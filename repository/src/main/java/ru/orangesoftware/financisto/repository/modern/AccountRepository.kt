package ru.orangesoftware.financisto.repository.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.AccountDao
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern repository interface for account data operations.
 * 
 * This interface defines the contract for account data access
 * using modern patterns (Coroutines, Flow) with Room entities.
 */
interface AccountRepository {
    suspend fun getAllAccounts(): List<AccountEntity>
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>
    suspend fun getAccountById(id: Long): AccountEntity?
    suspend fun insertAccount(account: AccountEntity): Long
    suspend fun updateAccount(account: AccountEntity): Boolean
    suspend fun deleteAccount(id: Long): Boolean
    suspend fun getAccountsIncludedInTotals(): List<AccountEntity>
    suspend fun searchAccounts(query: String): List<AccountEntity>
    suspend fun updateAccountBalance(accountId: Long, amount: Long, lastTransactionDate: Long)
    suspend fun incrementAccountBalance(accountId: Long, deltaAmount: Long, lastTransactionDate: Long)
}

/**
 * Implementation of AccountRepository using Room DAOs and Hilt DI.
 * 
 * This repository implementation:
 * - Uses Room DAOs for type-safe database operations
 * - Uses Coroutines for async operations
 * - Uses Flow for reactive data streams
 * - Provides error handling and transaction safety
 */
@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AccountRepository {

    override suspend fun getAllAccounts(): List<AccountEntity> = withContext(ioDispatcher) {
        accountDao.getAllAccounts()
    }

    override fun getAllAccountsFlow(): Flow<List<AccountEntity>> = 
        accountDao.getAllAccountsFlow().flowOn(ioDispatcher)

    override suspend fun getAccountById(id: Long): AccountEntity? = withContext(ioDispatcher) {
        accountDao.getAccountById(id)
    }

    override suspend fun insertAccount(account: AccountEntity): Long = withContext(ioDispatcher) {
        accountDao.insertAccount(account)
    }

    override suspend fun updateAccount(account: AccountEntity): Boolean = withContext(ioDispatcher) {
        try {
            accountDao.updateAccount(account)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteAccount(id: Long): Boolean = withContext(ioDispatcher) {
        try {
            accountDao.deleteAccountById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAccountsIncludedInTotals(): List<AccountEntity> = withContext(ioDispatcher) {
        accountDao.getAccountsIncludedInTotals()
    }

    override suspend fun searchAccounts(query: String): List<AccountEntity> = withContext(ioDispatcher) {
        accountDao.searchAccounts(query)
    }

    override suspend fun updateAccountBalance(
        accountId: Long, 
        amount: Long, 
        lastTransactionDate: Long
    ) = withContext(ioDispatcher) {
        accountDao.updateAccountBalance(accountId, amount, lastTransactionDate)
    }
    
    override suspend fun incrementAccountBalance(
        accountId: Long,
        deltaAmount: Long,
        lastTransactionDate: Long
    ) = withContext(ioDispatcher) {
        // Perform the atomic update
        accountDao.incrementAccountBalance(accountId, deltaAmount, lastTransactionDate)
        Unit
    }
}
