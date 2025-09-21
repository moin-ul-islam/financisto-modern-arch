package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Room entity for Transaction data.
 * 
 * This entity represents the core transaction data with foreign key relationships
 * to accounts, categories, and currencies for data integrity.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["_id"],
            childColumns = ["from_account_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CurrencyEntity::class,
            parentColumns = ["_id"],
            childColumns = ["original_currency_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,
    
    @ColumnInfo(name = "from_account_id")
    val fromAccountId: Long,
    
    @ColumnInfo(name = "to_account_id")
    val toAccountId: Long = 0,
    
    @ColumnInfo(name = "category_id")
    val categoryId: Long = 0,
    
    @ColumnInfo(name = "project_id")
    val projectId: Long = 0,
    
    @ColumnInfo(name = "location_id")
    val locationId: Long = 0,
    
    @ColumnInfo(name = "payee_id")
    val payeeId: Long = 0,
    
    @ColumnInfo(name = "from_amount")
    val fromAmount: Long,
    
    @ColumnInfo(name = "to_amount")
    val toAmount: Long = 0,
    
    @ColumnInfo(name = "datetime")
    val datetime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "note")
    val note: String? = null,
    
    @ColumnInfo(name = "status")
    val status: String = "CL", // CL = Cleared
    
    @ColumnInfo(name = "is_template")
    val isTemplate: Boolean = false,
    
    @ColumnInfo(name = "template_name")
    val templateName: String? = null,
    
    @ColumnInfo(name = "recurrence")
    val recurrence: String? = null,
    
    @ColumnInfo(name = "notification_options")
    val notificationOptions: String? = null,
    
    @ColumnInfo(name = "provider")
    val provider: String? = null,
    
    @ColumnInfo(name = "accuracy")
    val accuracy: Float = 0.0f,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double = 0.0,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,
    
    @ColumnInfo(name = "attached_picture")
    val attachedPicture: String? = null,
    
    @ColumnInfo(name = "last_recurrence")
    val lastRecurrence: Long = 0,
    
    @ColumnInfo(name = "parent_id")
    val parentId: Long = 0,
    
    @ColumnInfo(name = "updated_on")
    val updatedOn: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "remote_key")
    val remoteKey: String? = null,
    
    @ColumnInfo(name = "original_currency_id")
    val originalCurrencyId: Long = 0,
    
    @ColumnInfo(name = "original_from_amount")
    val originalFromAmount: Long = 0
)
