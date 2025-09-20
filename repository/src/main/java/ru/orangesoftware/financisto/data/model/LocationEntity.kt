package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Location data.
 *
 * This entity represents geographic locations that can be associated with transactions.
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "datetime")
    val datetime: Long,

    @ColumnInfo(name = "provider")
    val provider: String? = null,

    @ColumnInfo(name = "accuracy")
    val accuracy: Float = 0.0f,

    @ColumnInfo(name = "latitude")
    val latitude: Double = 0.0,

    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "is_payee")
    val isPayee: Boolean = false,

    @ColumnInfo(name = "resolved_address")
    val resolvedAddress: String? = null,

    @ColumnInfo(name = "count")
    val count: Int = 0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Long = 0
)