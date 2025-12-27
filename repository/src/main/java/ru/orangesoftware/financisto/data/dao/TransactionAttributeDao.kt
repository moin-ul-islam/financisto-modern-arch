package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import ru.orangesoftware.financisto.data.model.TransactionAttributeEntity

/**
 * Room DAO for Transaction-Attribute junction table operations.
 */
@Dao
interface TransactionAttributeDao {

    /**
     * Get all transaction-attribute relationships
     */
    @Query("SELECT * FROM transaction_attribute")
    suspend fun getAllTransactionAttributes(): List<TransactionAttributeEntity>

    /**
     * Get attributes for a specific transaction
     */
    @Query("SELECT * FROM transaction_attribute WHERE transaction_id = :transactionId")
    suspend fun getAttributesForTransaction(transactionId: Long): List<TransactionAttributeEntity>

    /**
     * Get transactions for a specific attribute
     */
    @Query("SELECT * FROM transaction_attribute WHERE attribute_id = :attributeId")
    suspend fun getTransactionsForAttribute(attributeId: Long): List<TransactionAttributeEntity>

    /**
     * Get attribute value for a transaction
     */
    @Query("SELECT value FROM transaction_attribute WHERE transaction_id = :transactionId AND attribute_id = :attributeId")
    suspend fun getAttributeValue(transactionId: Long, attributeId: Long): String?

    /**
     * Insert a transaction-attribute relationship
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionAttribute(transactionAttribute: TransactionAttributeEntity)

    /**
     * Insert multiple transaction-attribute relationships
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionAttributes(transactionAttributes: List<TransactionAttributeEntity>)

    /**
     * Update a transaction attribute value
     */
    @Query("UPDATE transaction_attribute SET value = :value WHERE transaction_id = :transactionId AND attribute_id = :attributeId")
    suspend fun updateAttributeValue(transactionId: Long, attributeId: Long, value: String?)

    /**
     * Delete a transaction-attribute relationship
     */
    @Delete
    suspend fun deleteTransactionAttribute(transactionAttribute: TransactionAttributeEntity)

    /**
     * Delete all attributes for a transaction
     */
    @Query("DELETE FROM transaction_attribute WHERE transaction_id = :transactionId")
    suspend fun deleteAttributesForTransaction(transactionId: Long)

    /**
     * Delete all transactions for an attribute
     */
    @Query("DELETE FROM transaction_attribute WHERE attribute_id = :attributeId")
    suspend fun deleteTransactionsForAttribute(attributeId: Long)

    /**
     * Delete transaction attributes for a transaction (alias for deleteAttributesForTransaction)
     */
    @Query("DELETE FROM transaction_attribute WHERE transaction_id = :transactionId")
    suspend fun deleteTransactionAttributesForTransaction(transactionId: Long)

    /**
     * Check if a transaction has a specific attribute
     */
    @Query("SELECT COUNT(*) > 0 FROM transaction_attribute WHERE transaction_id = :transactionId AND attribute_id = :attributeId")
    suspend fun transactionHasAttribute(transactionId: Long, attributeId: Long): Boolean
}