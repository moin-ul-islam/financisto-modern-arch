package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity for Transaction-Attribute junction table.
 *
 * This entity represents the many-to-many relationship between transactions and attributes,
 * storing the actual values for each attribute on each transaction.
 */
@Entity(
    tableName = "transaction_attribute",
    primaryKeys = ["transaction_id", "attribute_id"]
)
data class TransactionAttributeEntity(
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,

    @ColumnInfo(name = "attribute_id")
    val attributeId: Long,

    @ColumnInfo(name = "value")
    val value: String? = null
)