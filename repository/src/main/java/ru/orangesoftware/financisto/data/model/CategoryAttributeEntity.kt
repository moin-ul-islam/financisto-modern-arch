package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity for Category-Attribute junction table.
 *
 * This entity represents the many-to-many relationship between categories and attributes.
 */
@Entity(
    tableName = "category_attribute",
    primaryKeys = ["category_id", "attribute_id"]
)
data class CategoryAttributeEntity(
    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "attribute_id")
    val attributeId: Long
)