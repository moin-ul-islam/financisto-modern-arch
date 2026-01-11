package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.model.TransactionEntity
import ru.orangesoftware.financisto.data.model.TransactionAttributeEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.TransactionRepository
import ru.orangesoftware.financisto.repository.modern.AttributeRepository
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use cases for complex transaction management operations.
 *
 * These usecases orchestrate multiple repository calls to handle
 * complex business logic that was previously in DatabaseAdapter.
 */
@Singleton
class InsertOrUpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val attributeRepository: AttributeRepository,
    private val accountRepository: AccountRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Inserts or updates a transaction with all its attributes and dependencies.
     * This mirrors the legacy DatabaseAdapter.insertOrUpdate() method.
     *
     * @param transaction The transaction to insert/update
     * @param attributes Optional list of transaction attributes
     * @return Result with transaction ID on success, or exception on failure
     */
    suspend fun execute(
        transaction: TransactionEntity,
        attributes: List<TransactionAttributeEntity> = emptyList()
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            val transactionId = if (transaction.id == 0L) {
                // Insert new transaction
                transactionRepository.insertTransaction(transaction)
            } else {
                // Update existing transaction
                transactionRepository.updateTransaction(transaction)
                // Delete existing attributes for update
                attributeRepository.deleteTransactionAttributesForTransaction(transaction.id)
                transaction.id
            }

            // Insert new attributes if provided
            if (attributes.isNotEmpty()) {
                val attributesWithTransactionId = attributes.map { it.copy(transactionId = transactionId) }
                attributeRepository.insertTransactionAttributes(attributesWithTransactionId)
            }

            // Update account last transaction dates
            updateAccountLastTransactionDates(transaction)

            Result.success(transactionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateAccountLastTransactionDates(transaction: TransactionEntity) {
        val currentTime = System.currentTimeMillis()

        // Update from account if it exists
        if (transaction.fromAccountId > 0) {
            accountRepository.updateAccountBalance(
                accountId = transaction.fromAccountId,
                amount = 0L, // Balance update will be handled separately
                lastTransactionDate = currentTime
            )
        }

        // Update to account if it exists and is different from from account
        if (transaction.toAccountId > 0 && transaction.toAccountId != transaction.fromAccountId) {
            accountRepository.updateAccountBalance(
                accountId = transaction.toAccountId,
                amount = 0L, // Balance update will be handled separately
                lastTransactionDate = currentTime
            )
        }
    }
}
