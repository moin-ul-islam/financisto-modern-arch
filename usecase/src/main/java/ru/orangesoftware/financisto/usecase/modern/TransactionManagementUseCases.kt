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
            val transactionId = if (transaction.id == -1L) {
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

/**
 * Use case for deleting a transaction with all its dependencies.
 */
@Singleton
class DeleteTransactionWithDependenciesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val attributeRepository: AttributeRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Deletes a transaction and all its related data.
     * This includes transaction attributes and potentially split transactions.
     *
     * @param transactionId The ID of the transaction to delete
     * @return Result indicating success or failure
     */
    suspend fun execute(transactionId: Long): Result<Boolean> = withContext(ioDispatcher) {
        try {
            // Delete transaction attributes first
            attributeRepository.deleteTransactionAttributesForTransaction(transactionId)

            // Delete the transaction itself
            val success = transactionRepository.deleteTransaction(transactionId)

            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for creating a transfer transaction between two accounts.
 */
@Singleton
class CreateTransferTransactionUseCase @Inject constructor(
    private val insertOrUpdateTransactionUseCase: InsertOrUpdateTransactionUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Creates a transfer transaction between two accounts.
     *
     * @param fromAccountId The account to transfer from
     * @param toAccountId The account to transfer to
     * @param amount The transfer amount
     * @param currencyId The currency ID
     * @param datetime The transaction datetime
     * @param note Optional note for the transaction
     * @return Result with transaction ID on success
     */
    suspend fun execute(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        currencyId: Long,
        datetime: Long,
        note: String? = null
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            val transferTransaction = TransactionEntity(
                id = -1L,
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                fromAmount = amount,
                toAmount = amount, // Assuming same currency, adjust if needed
                originalFromAmount = amount,
                datetime = datetime,
                categoryId = 0L, // Transfers don't have categories
                locationId = 0L,
                projectId = 0L,
                payeeId = 0L,
                note = note,
                originalCurrencyId = currencyId,
                isTemplate = false,
                templateName = null,
                recurrence = null,
                notificationOptions = null,
                status = "CL", // CL = Cleared
                attachedPicture = null,
                lastRecurrence = datetime,
                parentId = 0L
            )

            insertOrUpdateTransactionUseCase.execute(transferTransaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}