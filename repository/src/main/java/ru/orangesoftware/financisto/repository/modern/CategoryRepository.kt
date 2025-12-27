package ru.orangesoftware.financisto.repository.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.CategoryDao
import ru.orangesoftware.financisto.data.model.CategoryEntity
import ru.orangesoftware.financisto.data.model.CategoryView
import ru.orangesoftware.financisto.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern repository interface for category data operations.
 *
 * This interface defines the contract for category data access
 * using modern patterns (Coroutines, Flow) with Room entities.
 */
interface CategoryRepository {
    suspend fun getAllCategories(): List<CategoryEntity>
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>
    suspend fun getCategoryById(id: Long): CategoryEntity?
    suspend fun getCategoriesByType(type: Int): List<CategoryEntity>
    suspend fun getExpenseCategories(): List<CategoryEntity>
    suspend fun getIncomeCategories(): List<CategoryEntity>
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun updateCategory(category: CategoryEntity): Boolean
    suspend fun deleteCategory(id: Long): Boolean
    suspend fun searchCategories(query: String): List<CategoryEntity>

    // CategoryView operations for hierarchical queries
    suspend fun getAllCategoriesWithLevel(): List<CategoryView>
    suspend fun getCategoryWithLevelById(categoryId: Long): CategoryView?
    suspend fun getCategoriesWithLevelByType(type: Int): List<CategoryView>
    suspend fun getExpenseCategoriesWithLevel(): List<CategoryView>
    suspend fun getIncomeCategoriesWithLevel(): List<CategoryView>
}

/**
 * Implementation of CategoryRepository using Room DAOs and Hilt DI.
 *
 * This repository implementation:
 * - Uses Room DAOs for type-safe database operations
 * - Uses Coroutines for async operations
 * - Uses Flow for reactive data streams
 * - Provides error handling and transaction safety
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CategoryRepository {

    override suspend fun getAllCategories(): List<CategoryEntity> = withContext(ioDispatcher) {
        categoryDao.getAllCategories()
    }

    override fun getAllCategoriesFlow(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategoriesFlow().flowOn(ioDispatcher)

    override suspend fun getCategoryById(id: Long): CategoryEntity? = withContext(ioDispatcher) {
        categoryDao.getCategoryById(id)
    }

    override suspend fun getCategoriesByType(type: Int): List<CategoryEntity> = withContext(ioDispatcher) {
        categoryDao.getCategoriesByType(type)
    }

    override suspend fun getExpenseCategories(): List<CategoryEntity> = withContext(ioDispatcher) {
        categoryDao.getExpenseCategories()
    }

    override suspend fun getIncomeCategories(): List<CategoryEntity> = withContext(ioDispatcher) {
        categoryDao.getIncomeCategories()
    }

    override suspend fun insertCategory(category: CategoryEntity): Long = withContext(ioDispatcher) {
        categoryDao.insertCategory(category)
    }

    override suspend fun updateCategory(category: CategoryEntity): Boolean = withContext(ioDispatcher) {
        try {
            categoryDao.updateCategory(category)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteCategory(id: Long): Boolean = withContext(ioDispatcher) {
        try {
            categoryDao.deleteCategoryById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchCategories(query: String): List<CategoryEntity> = withContext(ioDispatcher) {
        categoryDao.searchCategories(query)
    }

    // CategoryView operations
    override suspend fun getAllCategoriesWithLevel(): List<CategoryView> = withContext(ioDispatcher) {
        categoryDao.getAllCategoriesWithLevel()
    }

    override suspend fun getCategoryWithLevelById(categoryId: Long): CategoryView? = withContext(ioDispatcher) {
        categoryDao.getCategoryWithLevelById(categoryId)
    }

    override suspend fun getCategoriesWithLevelByType(type: Int): List<CategoryView> = withContext(ioDispatcher) {
        categoryDao.getCategoriesWithLevelByType(type)
    }

    override suspend fun getExpenseCategoriesWithLevel(): List<CategoryView> = withContext(ioDispatcher) {
        categoryDao.getExpenseCategoriesWithLevel()
    }

    override suspend fun getIncomeCategoriesWithLevel(): List<CategoryView> = withContext(ioDispatcher) {
        categoryDao.getIncomeCategoriesWithLevel()
    }
}