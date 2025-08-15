package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.repository.modern.TransactionRepository
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import ru.orangesoftware.financisto.data.dao.RunningBalanceDao
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.data.model.RunningBalanceEntity
import ru.orangesoftware.financisto.data.model.TransactionEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use cases for running balance calculations.
 * 
 * Running balance is a critical business logic in Financisto that tracks
 * the cumulative balance of an account over time as transactions are added.
 * This is complex logic that needs to be preserved exactly during migration.
 */
@Singleton
class RebuildRunningBalanceForAccountUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val runningBalanceDao: RunningBalanceDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Rebuilds running balance for a specific account.
     * This is equivalent to DatabaseAdapter.rebuildRunningBalanceForAccount().
     * 
     * Algorithm mirrors the legacy implementation:
     * 1. Delete existing running balance entries for the account
     * 2. Get all transactions for the account ordered by datetime ASC, then by ID ASC
     * 3. Calculate cumulative balance for each transaction
     * 4. Insert running balance entries
     * 
     * This handles split transactions and transfers correctly by processing
     * transactions in chronological order and maintaining the cumulative balance.
     */
    suspend fun execute(accountId: Long): Result<Boolean> = withContext(ioDispatcher) {
        try {
            // Step 1: Delete existing running balance entries for this account
            runningBalanceDao.deleteRunningBalanceForAccount(accountId)
            
            // Step 2: Get transactions for this account ordered by datetime ASC, then by ID ASC
            // This matches the legacy query: ORDER BY datetime ASC, _id ASC
            val transactions = getTransactionsForRunningBalance(accountId)
            
            if (transactions.isEmpty()) {
                return@withContext Result.success(true)
            }
            
            // Step 3: Calculate cumulative balance and create running balance entries
            val runningBalanceEntries = mutableListOf<RunningBalanceEntity>()
            var cumulativeBalance = 0L
            
            for (transaction in transactions) {
                // Calculate the transaction amount based on the account perspective
                // This mirrors the legacy logic where:
                // - from_account perspective: use from_amount (usually negative for outgoing)
                // - to_account perspective: use to_amount (usually positive for incoming)
                val transactionAmount = when {
                    transaction.fromAccountId == accountId -> {
                        // This is from the from_account perspective
                        transaction.fromAmount
                    }
                    transaction.toAccountId == accountId -> {
                        // This is from the to_account perspective (transfer incoming)
                        // In the legacy view, this becomes from_amount = to_amount (swapped)
                        transaction.toAmount
                    }
                    else -> {
                        // This shouldn't happen based on our query logic, but safety check
                        0L
                    }
                }
                
                // Skip zero-amount transactions
                if (transactionAmount == 0L) {
                    continue
                }
                
                // Add to cumulative balance
                cumulativeBalance += transactionAmount
                
                // Create running balance entry
                val runningBalanceEntry = RunningBalanceEntity(
                    accountId = accountId,
                    transactionId = transaction.id,
                    datetime = transaction.datetime,
                    balance = cumulativeBalance
                )
                
                runningBalanceEntries.add(runningBalanceEntry)
            }
            
            // Step 4: Insert all running balance entries in batch
            if (runningBalanceEntries.isNotEmpty()) {
                runningBalanceDao.insertRunningBalances(runningBalanceEntries)
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets transactions for running balance calculation.
     * Mirrors the legacy getBlotterForAccountWithSplits query.
     * 
     * The legacy view v_blotter_for_account_with_splits is complex:
     * 1. First part: transactions from from_account perspective (is_transfer = to_account_id)
     * 2. Second part: transfers from to_account perspective (is_transfer = -1)
     * 
     * Legacy logic processes:
     * - Regular transactions (parent_id = 0)
     * - Split transactions (parent_id > 0) but only those with is_transfer < 0 for transfers
     * - Skips self-transfers (to_account_id = from_account_id)
     */
    private suspend fun getTransactionsForRunningBalance(accountId: Long): List<TransactionEntity> {
        // Get all transactions that affect this account
        val allTransactions = transactionDao.getTransactionsForRunningBalance(accountId)
        
        // Process transactions to match legacy logic
        val result = mutableListOf<TransactionEntity>()
        
        for (transaction in allTransactions) {
            // Skip self-transfers (weird bug as noted in legacy code)
            if (transaction.toAccountId > 0 && transaction.toAccountId == transaction.fromAccountId) {
                continue
            }
            
            // Handle split transactions
            if (transaction.parentId > 0) {
                // For transfer splits, we need to determine if this is the "second part"
                // The legacy view marks the second part (to_account perspective) with is_transfer = -1
                // We simulate this by checking if this transaction represents the to_account side
                if (transaction.toAccountId == accountId) {
                    // This is a transfer split affecting the to_account, include it
                    result.add(transaction)
                }
                // Skip the from_account side of transfer splits (is_transfer >= 0 in legacy)
            } else {
                // Regular transactions (parent_id = 0) are always included
                result.add(transaction)
            }
        }
        
        return result
    }
}

/**
 * Use case for rebuilding running balances for all accounts.
 * This is equivalent to DatabaseAdapter.rebuildRunningBalances().
 */
@Singleton
class RebuildAllRunningBalancesUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val rebuildRunningBalanceForAccountUseCase: RebuildRunningBalanceForAccountUseCase,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(): Result<Boolean> = withContext(ioDispatcher) {
        try {
            // Get all accounts
            val accounts = accountRepository.getAllAccounts()
            
            // Rebuild running balance for each account
            for (account in accounts) {
                val result = rebuildRunningBalanceForAccountUseCase.execute(account.id)
                if (result.isFailure) {
                    // If any account fails, return the failure
                    return@withContext result
                }
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for getting the last running balance for an account.
 * This is equivalent to DatabaseAdapter.getLastRunningBalanceForAccount().
 */
@Singleton
class GetLastRunningBalanceForAccountUseCase @Inject constructor(
    private val runningBalanceDao: RunningBalanceDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(accountId: Long): Result<Long> = withContext(ioDispatcher) {
        try {
            val balance = runningBalanceDao.getLastRunningBalanceForAccount(accountId) ?: 0L
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for calculating account balance at a specific time.
 * This is equivalent to DatabaseAdapter.fetchAccountBalanceAtTheTime().
 */
@Singleton
class GetAccountBalanceAtTimeUseCase @Inject constructor(
    private val runningBalanceDao: RunningBalanceDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(accountId: Long, datetime: Long): Result<Long> = withContext(ioDispatcher) {
        try {
            val balance = runningBalanceDao.getAccountBalanceAtTime(accountId, datetime) ?: 0L
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
