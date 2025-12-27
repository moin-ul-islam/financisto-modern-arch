package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import ru.orangesoftware.financisto.data.model.CategoryAttributeEntity

/**
 * Room DAO for Category-Attribute junction table operations.
 */
@Dao
interface CategoryAttributeDao {

    /**
     * Get all category-attribute relationships
     */
    @Query("SELECT * FROM category_attribute")
    suspend fun getAllCategoryAttributes(): List<CategoryAttributeEntity>

    /**
     * Get attributes for a specific category
     */
    @Query("SELECT * FROM category_attribute WHERE category_id = :categoryId")
    suspend fun getAttributesForCategory(categoryId: Long): List<CategoryAttributeEntity>

    /**
     * Get categories for a specific attribute
     */
    @Query("SELECT * FROM category_attribute WHERE attribute_id = :attributeId")
    suspend fun getCategoriesForAttribute(attributeId: Long): List<CategoryAttributeEntity>

    /**
     * Insert a category-attribute relationship
     */
    @Insert
    suspend fun insertCategoryAttribute(categoryAttribute: CategoryAttributeEntity)

    /**
     * Insert multiple category-attribute relationships
     */
    @Insert
    suspend fun insertCategoryAttributes(categoryAttributes: List<CategoryAttributeEntity>)

    /**
     * Delete a category-attribute relationship
     */
    @Delete
    suspend fun deleteCategoryAttribute(categoryAttribute: CategoryAttributeEntity)

    /**
     * Delete all attributes for a category
     */
    @Query("DELETE FROM category_attribute WHERE category_id = :categoryId")
    suspend fun deleteAttributesForCategory(categoryId: Long)

    /**
     * Delete all categories for an attribute
     */
    @Query("DELETE FROM category_attribute WHERE attribute_id = :attributeId")
    suspend fun deleteCategoriesForAttribute(attributeId: Long)

    /**
     * Check if a category has a specific attribute
     */
    @Query("SELECT COUNT(*) > 0 FROM category_attribute WHERE category_id = :categoryId AND attribute_id = :attributeId")
    suspend fun categoryHasAttribute(categoryId: Long, attributeId: Long): Boolean
}