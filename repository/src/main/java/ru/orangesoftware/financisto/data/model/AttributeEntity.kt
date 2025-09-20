package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Attribute data.
 *
 * This entity represents custom attributes that can be attached to categories and transactions.
 * Attributes can be of different types: text, number, list, or checkbox.
 */
@Entity(tableName = "attributes")
data class AttributeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "type")
    val type: Int, // 1 = text, 2 = number, 3 = list, 4 = checkbox

    @ColumnInfo(name = "list_values")
    val listValues: String? = null,

    @ColumnInfo(name = "default_value")
    val defaultValue: String? = null,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Long = 0
)