package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Category data.
 *
 * This entity represents the hierarchical category system used for organizing transactions.
 * Categories support nested structures using the nested set model (left/right values).
 */
@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "left")
    val left: Int = 1,

    @ColumnInfo(name = "right")
    val right: Int = 2,

    @ColumnInfo(name = "type")
    val type: Int = 0, // 0 = expense, 1 = income

    @ColumnInfo(name = "last_location_id")
    val lastLocationId: Long = 0,

    @ColumnInfo(name = "last_project_id")
    val lastProjectId: Long = 0,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Long = 0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)