package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Room entity for Running Balance data.
 * 
 * This entity tracks the cumulative balance of an account over time as transactions
 * are added. Each entry represents the balance after a specific transaction.
 * 
 * The running balance table is critical for performance as it allows quick retrieval
 * of account balances at any point in time without recalculating from all transactions.
 */
@Entity(
    tableName = "running_balance",
    primaryKeys = ["account_id", "transaction_id"],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["_id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["_id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RunningBalanceEntity(
    @ColumnInfo(name = "account_id")
    val accountId: Long,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,
    
    @ColumnInfo(name = "datetime")
    val datetime: Long,
    
    @ColumnInfo(name = "balance")
    val balance: Long
)
