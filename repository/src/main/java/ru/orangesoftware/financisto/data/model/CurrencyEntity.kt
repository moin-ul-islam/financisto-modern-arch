package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Currency data.
 * 
 * Represents currency information in the modern Room database.
 */
@Entity(tableName = "currency")
data class CurrencyEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "symbol")
    val symbol: String,
    
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,
    
    @ColumnInfo(name = "decimals")
    val decimals: Int = 2,
    
    @ColumnInfo(name = "decimal_separator")
    val decimalSeparator: String = ".",
    
    @ColumnInfo(name = "group_separator")
    val groupSeparator: String = ",",
    
    @ColumnInfo(name = "symbol_format")
    val symbolFormat: String = "RS",
    
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = System.currentTimeMillis()
)
