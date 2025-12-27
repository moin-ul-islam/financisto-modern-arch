package ru.orangesoftware.financisto.repository.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.AttributeDao
import ru.orangesoftware.financisto.data.dao.TransactionAttributeDao
import ru.orangesoftware.financisto.data.model.AttributeEntity
import ru.orangesoftware.financisto.data.model.AttributeView
import ru.orangesoftware.financisto.data.model.TransactionAttributeEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern repository interface for attribute data operations.
 *
 * This interface defines the contract for attribute data access
 * using modern patterns (Coroutines, Flow) with Room entities.
 */
interface AttributeRepository {
    suspend fun getAllAttributes(): List<AttributeEntity>
    fun getAllAttributesFlow(): Flow<List<AttributeEntity>>
    suspend fun getAttributeById(id: Long): AttributeEntity?
    suspend fun getAttributesByType(type: Int): List<AttributeEntity>
    suspend fun insertAttribute(attribute: AttributeEntity): Long
    suspend fun updateAttribute(attribute: AttributeEntity): Boolean
    suspend fun deleteAttribute(id: Long): Boolean
    suspend fun searchAttributes(query: String): List<AttributeEntity>

    // Transaction attribute operations
    suspend fun getTransactionAttributes(transactionId: Long): List<TransactionAttributeEntity>
    suspend fun insertTransactionAttribute(transactionAttribute: TransactionAttributeEntity)
    suspend fun insertTransactionAttributes(attributes: List<TransactionAttributeEntity>)
    suspend fun deleteTransactionAttributesForTransaction(transactionId: Long)
    suspend fun updateTransactionAttributes(transactionId: Long, attributes: List<TransactionAttributeEntity>)

    // AttributeView operations for category-linked attributes
    suspend fun getAllAttributesWithCategories(): List<AttributeView>
    suspend fun getAttributesForCategory(categoryId: Long): List<AttributeView>
    suspend fun getAttributesForCategoryHierarchy(left: Int, right: Int): List<AttributeView>
}

/**
 * Implementation of AttributeRepository using Room DAOs and Hilt DI.
 *
 * This repository implementation:
 * - Uses Room DAOs for type-safe database operations
 * - Uses Coroutines for async operations
 * - Uses Flow for reactive data streams
 * - Provides error handling and transaction safety
 */
@Singleton
class AttributeRepositoryImpl @Inject constructor(
    private val attributeDao: AttributeDao,
    private val transactionAttributeDao: TransactionAttributeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AttributeRepository {

    override suspend fun getAllAttributes(): List<AttributeEntity> = withContext(ioDispatcher) {
        attributeDao.getAllAttributes()
    }

    override fun getAllAttributesFlow(): Flow<List<AttributeEntity>> =
        attributeDao.getAllAttributesFlow().flowOn(ioDispatcher)

    override suspend fun getAttributeById(id: Long): AttributeEntity? = withContext(ioDispatcher) {
        attributeDao.getAttributeById(id)
    }

    override suspend fun getAttributesByType(type: Int): List<AttributeEntity> = withContext(ioDispatcher) {
        attributeDao.getAttributesByType(type)
    }

    override suspend fun insertAttribute(attribute: AttributeEntity): Long = withContext(ioDispatcher) {
        attributeDao.insertAttribute(attribute)
    }

    override suspend fun updateAttribute(attribute: AttributeEntity): Boolean = withContext(ioDispatcher) {
        try {
            attributeDao.updateAttribute(attribute)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteAttribute(id: Long): Boolean = withContext(ioDispatcher) {
        try {
            attributeDao.deleteAttributeById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchAttributes(query: String): List<AttributeEntity> = withContext(ioDispatcher) {
        attributeDao.searchAttributes(query)
    }

    // Transaction attribute operations
    override suspend fun getTransactionAttributes(transactionId: Long): List<TransactionAttributeEntity> = withContext(ioDispatcher) {
        transactionAttributeDao.getAttributesForTransaction(transactionId)
    }

    override suspend fun insertTransactionAttribute(transactionAttribute: TransactionAttributeEntity) = withContext(ioDispatcher) {
        transactionAttributeDao.insertTransactionAttribute(transactionAttribute)
    }

    override suspend fun insertTransactionAttributes(attributes: List<TransactionAttributeEntity>) = withContext(ioDispatcher) {
        transactionAttributeDao.insertTransactionAttributes(attributes)
    }

    override suspend fun deleteTransactionAttributesForTransaction(transactionId: Long) = withContext(ioDispatcher) {
        transactionAttributeDao.deleteTransactionAttributesForTransaction(transactionId)
    }

    override suspend fun updateTransactionAttributes(transactionId: Long, attributes: List<TransactionAttributeEntity>) = withContext(ioDispatcher) {
        // Delete existing attributes for this transaction
        deleteTransactionAttributesForTransaction(transactionId)
        // Insert new attributes
        if (attributes.isNotEmpty()) {
            insertTransactionAttributes(attributes)
        }
    }

    // AttributeView operations
    override suspend fun getAllAttributesWithCategories(): List<AttributeView> = withContext(ioDispatcher) {
        attributeDao.getAllAttributesWithCategories()
    }

    override suspend fun getAttributesForCategory(categoryId: Long): List<AttributeView> = withContext(ioDispatcher) {
        attributeDao.getAttributesForCategory(categoryId)
    }

    override suspend fun getAttributesForCategoryHierarchy(left: Int, right: Int): List<AttributeView> = withContext(ioDispatcher) {
        attributeDao.getAttributesForCategoryHierarchy(left, right)
    }
}