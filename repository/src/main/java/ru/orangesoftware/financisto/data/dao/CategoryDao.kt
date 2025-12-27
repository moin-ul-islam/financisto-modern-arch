package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.CategoryEntity
import ru.orangesoftware.financisto.data.model.CategoryView

/**
 * Room DAO for Category operations.
 *
 * Provides comprehensive category data access with support for
 * hierarchical queries and reactive data streams.
 */
@Dao
interface CategoryDao {

    /**
     * Get all categories as a Flow for reactive updates
     */
    @Query("SELECT * FROM category WHERE is_active = 1 ORDER BY sort_order, title")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    /**
     * Get all categories as a one-time operation
     */
    @Query("SELECT * FROM category WHERE is_active = 1 ORDER BY sort_order, title")
    suspend fun getAllCategories(): List<CategoryEntity>

    /**
     * Get category by ID
     */
    @Query("SELECT * FROM category WHERE _id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): CategoryEntity?

    /**
     * Get categories by type (expense/income)
     */
    @Query("SELECT * FROM category WHERE type = :type AND is_active = 1 ORDER BY sort_order, title")
    suspend fun getCategoriesByType(type: Int): List<CategoryEntity>

    /**
     * Get expense categories
     */
    @Query("SELECT * FROM category WHERE type = 0 AND is_active = 1 ORDER BY sort_order, title")
    suspend fun getExpenseCategories(): List<CategoryEntity>

    /**
     * Get income categories
     */
    @Query("SELECT * FROM category WHERE type = 1 AND is_active = 1 ORDER BY sort_order, title")
    suspend fun getIncomeCategories(): List<CategoryEntity>

    /**
     * Insert a new category
     */
    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    /**
     * Update an existing category
     */
    @Update
    suspend fun updateCategory(category: CategoryEntity)

    /**
     * Delete a category
     */
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    /**
     * Delete category by ID
     */
    @Query("DELETE FROM category WHERE _id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    /**
     * Get category count
     */
    @Query("SELECT COUNT(*) FROM category WHERE is_active = 1")
    suspend fun getCategoryCount(): Int

    /**
     * Search categories by title
     */
    @Query("SELECT * FROM category WHERE is_active = 1 AND title LIKE '%' || :query || '%' ORDER BY title")
    suspend fun searchCategories(query: String): List<CategoryEntity>

    // CategoryView queries for hierarchical operations

    /**
     * Get all categories with hierarchy level information
     */
    @Query("SELECT * FROM v_category ORDER BY `left`")
    suspend fun getAllCategoriesWithLevel(): List<CategoryView>

    /**
     * Get category with hierarchy level by ID
     */
    @Query("SELECT * FROM v_category WHERE _id = :categoryId")
    suspend fun getCategoryWithLevelById(categoryId: Long): CategoryView?

    /**
     * Get categories by type with hierarchy level
     */
    @Query("SELECT * FROM v_category WHERE type = :type ORDER BY `left`")
    suspend fun getCategoriesWithLevelByType(type: Int): List<CategoryView>

    /**
     * Get expense categories with hierarchy level
     */
    @Query("SELECT * FROM v_category WHERE type = 0 ORDER BY `left`")
    suspend fun getExpenseCategoriesWithLevel(): List<CategoryView>

    /**
     * Get income categories with hierarchy level
     */
    @Query("SELECT * FROM v_category WHERE type = 1 ORDER BY `left`")
    suspend fun getIncomeCategoriesWithLevel(): List<CategoryView>
}