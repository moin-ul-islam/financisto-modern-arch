package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.AttributeEntity
import ru.orangesoftware.financisto.data.model.AttributeView

/**
 * Room DAO for Attribute operations.
 */
@Dao
interface AttributeDao {

    /**
     * Get all attributes as a Flow for reactive updates
     */
    @Query("SELECT * FROM attributes WHERE is_active = 1 ORDER BY sort_order, title")
    fun getAllAttributesFlow(): Flow<List<AttributeEntity>>

    /**
     * Get all attributes as a one-time operation
     */
    @Query("SELECT * FROM attributes WHERE is_active = 1 ORDER BY sort_order, title")
    suspend fun getAllAttributes(): List<AttributeEntity>

    /**
     * Get attribute by ID
     */
    @Query("SELECT * FROM attributes WHERE _id = :attributeId")
    suspend fun getAttributeById(attributeId: Long): AttributeEntity?

    /**
     * Get attributes by type
     */
    @Query("SELECT * FROM attributes WHERE type = :type AND is_active = 1 ORDER BY sort_order, title")
    suspend fun getAttributesByType(type: Int): List<AttributeEntity>

    /**
     * Insert a new attribute
     */
    @Insert
    suspend fun insertAttribute(attribute: AttributeEntity): Long

    /**
     * Update an existing attribute
     */
    @Update
    suspend fun updateAttribute(attribute: AttributeEntity)

    /**
     * Delete an attribute
     */
    @Delete
    suspend fun deleteAttribute(attribute: AttributeEntity)

    /**
     * Delete attribute by ID
     */
    @Query("DELETE FROM attributes WHERE _id = :attributeId")
    suspend fun deleteAttributeById(attributeId: Long)

    /**
     * Get attribute count
     */
    @Query("SELECT COUNT(*) FROM attributes WHERE is_active = 1")
    suspend fun getAttributeCount(): Int

    /**
     * Search attributes by title
     */
    @Query("SELECT * FROM attributes WHERE is_active = 1 AND title LIKE '%' || :query || '%' ORDER BY title")
    suspend fun searchAttributes(query: String): List<AttributeEntity>

    // AttributeView queries for category-linked attributes

    /**
     * Get all attributes with their associated category information
     */
    @Query("SELECT * FROM v_attributes ORDER BY category_id, title")
    suspend fun getAllAttributesWithCategories(): List<AttributeView>

    /**
     * Get attributes for a specific category
     */
    @Query("SELECT * FROM v_attributes WHERE category_id = :categoryId ORDER BY title")
    suspend fun getAttributesForCategory(categoryId: Long): List<AttributeView>

    /**
     * Get attributes for categories within a hierarchy range (using nested set model)
     */
    @Query("SELECT * FROM v_attributes WHERE category_left >= :left AND category_right <= :right ORDER BY category_id, title")
    suspend fun getAttributesForCategoryHierarchy(left: Int, right: Int): List<AttributeView>
}