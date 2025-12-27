package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity for Credit Card Closing Date data.
 *
 * This entity represents the billing cycle information for credit card accounts.
 * Period is stored as MMYYYY format where MM = 0 to 11.
 */
@Entity(
    tableName = "ccard_closing_date",
    primaryKeys = ["account_id", "period"]
)
data class CreditCardClosingDateEntity(
    @ColumnInfo(name = "account_id")
    val accountId: Long,

    @ColumnInfo(name = "period")
    val period: Int, // MMYYYY format, MM = 0-11

    @ColumnInfo(name = "closing_day")
    val closingDay: Int
)