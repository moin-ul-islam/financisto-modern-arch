package ru.orangesoftware.financisto.demo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import ru.orangesoftware.financisto.repository.modern.TransactionRepository
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsForAccountUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demonstration class showing how to use the modern Room-based repositories
 * and use cases with proper dependency injection and reactive patterns.
 * 
 * This class can be injected anywhere in the app to demonstrate the new
 * architecture patterns.
 */
@Singleton
class ModernDataLayerDemo @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getTransactionsForAccountUseCase: GetTransactionsForAccountUseCase
) {

    /**
     * Demonstrates how to create a new account using the modern repository
     */
    suspend fun createSampleAccount(): Long {
        val newAccount = AccountEntity(
            title = "Demo Account",
            type = "CASH",
            currencyId = 1,
            totalAmount = 100000, // $1000.00 (in cents)
            isActive = true
        )
        
        return accountRepository.insertAccount(newAccount)
    }

    /**
     * Demonstrates reactive data access with Flow
     */
    fun observeAccounts() {
        CoroutineScope(Dispatchers.Main).launch {
            getAccountsUseCase.executeAsFlow().collect { accounts ->
                // This will be called every time accounts data changes
                println("Found ${accounts.size} accounts")
                accounts.forEach { account ->
                    println("Account: ${account.title} - ${account.totalAmount}")
                }
            }
        }
    }

    /**
     * Demonstrates how to get transactions for a specific account
     */
    suspend fun getAccountTransactions(accountId: Long) {
        val transactions = getTransactionsForAccountUseCase.execute(accountId)
        println("Found ${transactions.size} transactions for account $accountId")
    }

    /**
     * Demonstrates proper error handling with Result types
     */
    suspend fun updateAccountWithErrorHandling(accountId: Long, newTitle: String): Boolean {
        return try {
            val account = accountRepository.getAccountById(accountId)
            if (account != null) {
                val updatedAccount = account.copy(title = newTitle)
                accountRepository.updateAccount(updatedAccount)
            } else {
                false
            }
        } catch (e: Exception) {
            // Log error and return failure
            false
        }
    }

    /**
     * Demonstrates search functionality
     */
    suspend fun searchAccounts(query: String): List<AccountEntity> {
        return accountRepository.searchAccounts(query)
    }
}
