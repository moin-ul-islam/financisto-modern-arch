package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Account data.
 * 
 * This modern entity mirrors the legacy Account model but uses Room annotations
 * for type-safe database operations with compile-time verification.
 * 
 * Migration Strategy:
 * - Maintains same table structure as legacy implementation
 * - Uses Room annotations for better type safety
 * - Will gradually replace legacy Account model usage
 */
@Entity(
    tableName = "account"
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "type")
    val type: String,
    
    @ColumnInfo(name = "currency_id")
    val currencyId: Long,
    
    @ColumnInfo(name = "total_amount")
    val totalAmount: Long = 0,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "is_include_into_totals")
    val isIncludeIntoTotals: Boolean = true,
    
    @ColumnInfo(name = "creation_date")
    val creationDate: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_transaction_date")
    val lastTransactionDate: Long = 0,
    
    @ColumnInfo(name = "note")
    val note: String? = null,
    
    @ColumnInfo(name = "issuer")
    val issuer: String? = null,
    
    @ColumnInfo(name = "number")
    val number: String? = null,
    
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    
    @ColumnInfo(name = "limit_amount")
    val limitAmount: Long = 0,
    
    @ColumnInfo(name = "card_issuer")
    val cardIssuer: String? = null,
    
    @ColumnInfo(name = "closing_day")
    val closingDay: Int = 0,
    
    @ColumnInfo(name = "payment_day")
    val paymentDay: Int = 0
)
