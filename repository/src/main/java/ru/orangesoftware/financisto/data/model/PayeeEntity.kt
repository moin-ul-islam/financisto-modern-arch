package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Payee data.
 *
 * This entity represents payees that can be associated with transactions.
 */
@Entity(tableName = "payee")
data class PayeeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "last_category_id")
    val lastCategoryId: Long = 0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Long = 0
)