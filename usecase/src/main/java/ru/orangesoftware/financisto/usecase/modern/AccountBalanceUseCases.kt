package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.RunningBalanceDao
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.data.model.RunningBalanceEntity
import ru.orangesoftware.financisto.data.model.TransactionEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use cases for account balance calculations and management.
 *
 * These usecases handle the complex business logic for maintaining
 * account balances and running balance calculations.
 */

/**
 * Use case for recalculating balance for a specific account.
 * This rebuilds the running balance table for the account.
 */
@Singleton
class RecalculateAccountBalanceUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val runningBalanceDao: RunningBalanceDao,
    private val accountRepository: AccountRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Recalculates the balance for a specific account by rebuilding running balances.
     * This mirrors the legacy DatabaseAdapter.rebuildRunningBalanceForAccount() logic.
     *
     * @param accountId The account ID to recalculate balance for
     * @return Result indicating success or failure
     */
    suspend fun execute(accountId: Long): Result<Long> = withContext(ioDispatcher) {
        try {
            // Delete existing running balance entries for this account
            runningBalanceDao.deleteRunningBalanceForAccount(accountId)

            // Get all transactions for this account ordered by datetime ASC, then by ID ASC
            val transactions = transactionDao.getTransactionsForRunningBalance(accountId)

            if (transactions.isEmpty()) {
                // Update the account's total_amount field to 0 when no transactions
                val account = accountRepository.getAccountById(accountId)
                if (account != null) {
                    val updatedAccount = account.copy(totalAmount = 0L)
                    accountRepository.updateAccount(updatedAccount)
                }
                return@withContext Result.success(0L)
            }

            // Calculate cumulative balance and create running balance entries
            val runningBalanceEntries = mutableListOf<RunningBalanceEntity>()
            var cumulativeBalance = 0L

            for (transaction in transactions) {
                // Calculate the transaction amount based on the account perspective
                val transactionAmount = when {
                    transaction.fromAccountId == accountId -> transaction.fromAmount
                    transaction.toAccountId == accountId -> transaction.toAmount
                    else -> 0L // This shouldn't happen based on our query logic
                }

                // Skip zero-amount transactions
                if (transactionAmount == 0L) {
                    continue
                }

                // Skip split transactions
                if (transaction.parentId > 0L) {
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

            // Insert all running balance entries in batch
            if (runningBalanceEntries.isNotEmpty()) {
                runningBalanceDao.insertRunningBalances(runningBalanceEntries)
            }

            // Update the account's total_amount field with the final balance
            val account = accountRepository.getAccountById(accountId)
            if (account != null) {
                val updatedAccount = account.copy(totalAmount = cumulativeBalance)
                accountRepository.updateAccount(updatedAccount)
            }

            Result.success(cumulativeBalance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for recalculating balances for all accounts.
 */
@Singleton
class RecalculateAllAccountBalancesUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val recalculateAccountBalanceUseCase: RecalculateAccountBalanceUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Recalculates balances for all accounts in the system.
     * This is used during data migration or integrity checks.
     *
     * @return Result with count of accounts processed on success
     */
    suspend fun execute(): Result<Int> = withContext(ioDispatcher) {
        try {
            val accounts = accountRepository.getAllAccounts()
            var processedCount = 0

            for (account in accounts) {
                recalculateAccountBalanceUseCase.execute(account.id)
                processedCount++
            }

            Result.success(processedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for getting the current balance of an account.
 */
@Singleton
class GetAccountBalanceUseCase @Inject constructor(
    private val runningBalanceDao: RunningBalanceDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Gets the current balance for an account.
     * This returns the most recent running balance entry.
     *
     * @param accountId The account ID to get balance for
     * @return Result with balance amount on success
     */
    suspend fun execute(accountId: Long): Result<Long> = withContext(ioDispatcher) {
        try {
            val balance = runningBalanceDao.getLastRunningBalanceForAccount(accountId) ?: 0L
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}