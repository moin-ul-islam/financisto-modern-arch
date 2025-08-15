package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for account-related business logic.
 * 
 * This demonstrates modern Clean Architecture patterns:
 * - Single responsibility (only account operations)
 * - Dependency injection with Hilt
 * - Coroutines for async operations
 * - Flow for reactive data streams
 * - Proper error handling and business logic separation
 * 
 * Note: Currently using Any type placeholders until shared model classes are available.
 */
@Singleton
class GetAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Get all accounts as a one-time operation
     */
    suspend fun execute(): List<Any> = withContext(ioDispatcher) {
        try {
            accountRepository.getAllAccounts()
        } catch (e: Exception) {
            // Handle errors appropriately
            emptyList()
        }
    }

    /**
     * Get all accounts as a reactive stream
     */
    fun executeAsFlow(): Flow<List<Any>> {
        return accountRepository.getAllAccountsFlow()
            .flowOn(ioDispatcher)
    }
}

/**
 * Use case for getting a specific account by ID
 */
@Singleton
class GetAccountByIdUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(accountId: Long): Any? = withContext(ioDispatcher) {
        try {
            accountRepository.getAccountById(accountId)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Use case for creating a new account
 */
@Singleton
class CreateAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun execute(account: Any): Result<Long> = withContext(ioDispatcher) {
        try {
            val accountId = accountRepository.insertAccount(account)
            Result.success(accountId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
