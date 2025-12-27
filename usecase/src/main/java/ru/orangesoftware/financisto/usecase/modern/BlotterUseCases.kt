package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.RunningBalanceDao
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import ru.orangesoftware.financisto.repository.modern.CategoryRepository
import ru.orangesoftware.financisto.repository.modern.CurrencyRepository
import ru.orangesoftware.financisto.repository.modern.PayeeRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain model for a blotter item - represents a transaction with its running balance.
 * 
 * This is the primary model for displaying transactions in the blotter view.
 * It includes all necessary data for rendering a transaction row including:
 * - Transaction details (amount, date, category, etc.)
 * - Running balance after this transaction
 * - Display-friendly names resolved from IDs
 */
data class BlotterItem(
    val transactionId: Long,
    val datetime: Long,
    val fromAmount: Long,
    val toAmount: Long,
    val fromAccountId: Long,
    val fromAccountTitle: String,
    val fromAccountCurrencyId: Long,
    val toAccountId: Long,
    val toAccountTitle: String?,
    val toAccountCurrencyId: Long?,
    val categoryId: Long,
    val categoryTitle: String?,
    val payeeId: Long,
    val payeeTitle: String?,
    val note: String?,
    val runningBalance: Long, // This is the key field - running balance after this transaction
    val isTransfer: Boolean,
    val isSplit: Boolean,
    val status: String,
    val originalCurrencyId: Long,
    val originalFromAmount: Long
) {
    /**
     * Check if this is a transfer transaction
     */
    val isTransferTransaction: Boolean
        get() = toAccountId > 0 && !isSplit
    
    /**
     * Get display title for the transaction (category or transfer indicator)
     */
    fun getDisplayTitle(): String = when {
        isTransferTransaction -> "Transfer"
        isSplit -> "Split"
        else -> categoryTitle ?: "No Category"
    }
    
    /**
     * Get display subtitle (payee, note, or account names for transfers)
     */
    fun getDisplaySubtitle(): String = when {
        isTransferTransaction -> "$fromAccountTitle → ${toAccountTitle ?: ""}"
        !note.isNullOrBlank() -> note
        !payeeTitle.isNullOrBlank() -> payeeTitle
        else -> ""
    }
}

/**
 * Use case for getting blotter transactions for a specific account with running balances.
 * 
 * This is the primary use case for the blotter feature. It:
 * 1. Retrieves transactions for the specified account
 * 2. Ensures running balances are calculated
 * 3. Enriches transaction data with account/category/payee names
 * 4. Returns BlotterItem objects ready for UI display
 * 
 * The running balance shows the account balance after each transaction,
 * ordered from most recent to oldest.
 */
@Singleton
class GetBlotterForAccountUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val runningBalanceDao: RunningBalanceDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val currencyRepository: CurrencyRepository,
    private val payeeRepository: PayeeRepository,
    private val rebuildRunningBalanceForAccountUseCase: RebuildRunningBalanceForAccountUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Execute the use case to get blotter items for an account.
     * 
     * @param accountId The account ID to get transactions for
     * @param ensureBalanceCalculated If true, rebuild running balance if it appears stale
     * @return Result containing list of BlotterItem objects ordered by most recent first
     */
    suspend fun execute(
        accountId: Long,
        ensureBalanceCalculated: Boolean = true
    ): Result<List<BlotterItem>> = withContext(ioDispatcher) {
        try {
            // Ensure running balance is calculated for this account
            if (ensureBalanceCalculated) {
                rebuildRunningBalanceForAccountUseCase.execute(accountId)
                    .getOrElse { 
                        // Log error but continue - we'll show transactions without balance
                        println("Warning: Failed to rebuild running balance: ${it.message}")
                    }
            }
            
            // Get transactions for running balance (chronological order: oldest first)
            val transactions = transactionDao.getTransactionsForRunningBalance(accountId)
            
            if (transactions.isEmpty()) {
                return@withContext Result.success(emptyList())
            }
            
            // Get running balances for all transactions
            val runningBalances = runningBalanceDao.getRunningBalancesForAccount(accountId)
            val balanceMap = runningBalances.associate { it.transactionId to it.balance }
            
            // Get account info for enrichment
            val account = accountRepository.getAccountById(accountId)
                ?: return@withContext Result.failure(Exception("Account not found: $accountId"))
            
            // Build blotter items with enriched data
            val blotterItems = mutableListOf<BlotterItem>()
            
            for (transaction in transactions) {
                // Get running balance for this transaction
                val runningBalance = balanceMap[transaction.id] ?: 0L
                
                // Determine if this is from or to account perspective
                val isFromAccount = transaction.fromAccountId == accountId
                
                // Get category title if applicable
                val categoryTitle = if (transaction.categoryId > 0) {
                    categoryRepository.getCategoryById(transaction.categoryId)?.title
                } else if (transaction.categoryId == -1L) {
                    "Split"
                } else {
                    null
                }
                
                // Get payee title if applicable
                val payeeTitle = if (transaction.payeeId > 0) {
                    payeeRepository.getPayeeById(transaction.payeeId)?.title
                } else {
                    null
                }
                
                // Get to account info for transfers
                val toAccountTitle = if (transaction.toAccountId > 0) {
                    accountRepository.getAccountById(transaction.toAccountId)?.title
                } else {
                    null
                }
                
                val toAccountCurrencyId = if (transaction.toAccountId > 0) {
                    accountRepository.getAccountById(transaction.toAccountId)?.currencyId
                } else {
                    null
                }
                
                val blotterItem = BlotterItem(
                    transactionId = transaction.id,
                    datetime = transaction.datetime,
                    fromAmount = transaction.fromAmount,
                    toAmount = transaction.toAmount,
                    fromAccountId = transaction.fromAccountId,
                    fromAccountTitle = account.title,
                    fromAccountCurrencyId = account.currencyId,
                    toAccountId = transaction.toAccountId,
                    toAccountTitle = toAccountTitle,
                    toAccountCurrencyId = toAccountCurrencyId,
                    categoryId = transaction.categoryId,
                    categoryTitle = categoryTitle,
                    payeeId = transaction.payeeId,
                    payeeTitle = payeeTitle,
                    note = transaction.note,
                    runningBalance = runningBalance,
                    isTransfer = transaction.toAccountId > 0,
                    isSplit = transaction.categoryId == -1L,
                    status = transaction.status,
                    originalCurrencyId = transaction.originalCurrencyId,
                    originalFromAmount = transaction.originalFromAmount
                )
                
                blotterItems.add(blotterItem)
            }
            
            // Reverse to show most recent first (blotter shows newest on top)
            Result.success(blotterItems.reversed())
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for observing blotter transactions for an account as a Flow.
 * 
 * This provides reactive updates when transactions change.
 * Unlike the one-shot execute(), this returns a Flow that emits new lists
 * whenever underlying data changes.
 */
@Singleton
class ObserveBlotterForAccountUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val runningBalanceDao: RunningBalanceDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val payeeRepository: PayeeRepository,
    private val getBlotterForAccountUseCase: GetBlotterForAccountUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Observe blotter items for an account as a Flow.
     * 
     * @param accountId The account ID to observe
     * @return Flow of lists of BlotterItem objects
     */
    fun execute(accountId: Long): Flow<List<BlotterItem>> {
        // Observe transactions for the account
        return transactionDao.getTransactionsForAccountFlow(accountId)
            .map { 
                // When transactions change, re-fetch complete blotter data
                getBlotterForAccountUseCase.execute(
                    accountId = accountId,
                    ensureBalanceCalculated = true
                ).getOrElse { emptyList() }
            }
    }
}

/**
 * Use case for getting blotter transactions across all accounts.
 * 
 * This is used for the main blotter view that shows all transactions.
 * Note: Running balance is only meaningful per-account, so this doesn't include it.
 * For account-specific blotter with running balance, use GetBlotterForAccountUseCase.
 */
@Singleton
class GetBlotterAllAccountsUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val payeeRepository: PayeeRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(): Result<List<BlotterItem>> = withContext(ioDispatcher) {
        try {
            // Get all blotter transactions (parent transactions only, no splits)
            val transactions = transactionDao.getBlotterTransactions()
            
            if (transactions.isEmpty()) {
                return@withContext Result.success(emptyList())
            }
            
            // Build blotter items with enriched data
            val blotterItems = transactions.map { transaction ->
                // Get from account info
                val fromAccount = accountRepository.getAccountById(transaction.fromAccountId)
                
                // Get category title if applicable
                val categoryTitle = if (transaction.categoryId > 0) {
                    categoryRepository.getCategoryById(transaction.categoryId)?.title
                } else if (transaction.categoryId == -1L) {
                    "Split"
                } else {
                    null
                }
                
                // Get payee title if applicable
                val payeeTitle = if (transaction.payeeId > 0) {
                    payeeRepository.getPayeeById(transaction.payeeId)?.title
                } else {
                    null
                }
                
                // Get to account info for transfers
                val toAccountTitle = if (transaction.toAccountId > 0) {
                    accountRepository.getAccountById(transaction.toAccountId)?.title
                } else {
                    null
                }
                
                val toAccountCurrencyId = if (transaction.toAccountId > 0) {
                    accountRepository.getAccountById(transaction.toAccountId)?.currencyId
                } else {
                    null
                }
                
                BlotterItem(
                    transactionId = transaction.id,
                    datetime = transaction.datetime,
                    fromAmount = transaction.fromAmount,
                    toAmount = transaction.toAmount,
                    fromAccountId = transaction.fromAccountId,
                    fromAccountTitle = fromAccount?.title ?: "Unknown",
                    fromAccountCurrencyId = fromAccount?.currencyId ?: 0L,
                    toAccountId = transaction.toAccountId,
                    toAccountTitle = toAccountTitle,
                    toAccountCurrencyId = toAccountCurrencyId,
                    categoryId = transaction.categoryId,
                    categoryTitle = categoryTitle,
                    payeeId = transaction.payeeId,
                    payeeTitle = payeeTitle,
                    note = transaction.note,
                    runningBalance = 0L, // Not applicable for all-accounts view
                    isTransfer = transaction.toAccountId > 0,
                    isSplit = transaction.categoryId == -1L,
                    status = transaction.status,
                    originalCurrencyId = transaction.originalCurrencyId,
                    originalFromAmount = transaction.originalFromAmount
                )
            }
            
            Result.success(blotterItems)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
