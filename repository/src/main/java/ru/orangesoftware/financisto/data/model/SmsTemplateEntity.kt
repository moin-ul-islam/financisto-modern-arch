package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for SMS Template data.
 *
 * This entity represents templates for parsing SMS messages to automatically create transactions.
 */
@Entity(tableName = "sms_template")
data class SmsTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "template")
    val template: String,

    @ColumnInfo(name = "category_id")
    val categoryId: Long = 0,

    @ColumnInfo(name = "account_id")
    val accountId: Long = -1,

    @ColumnInfo(name = "is_income")
    val isIncome: Boolean = false,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Long = 0
)