package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity for Currency Exchange Rate data.
 *
 * This entity represents exchange rates between currencies at specific dates.
 */
@Entity(
    tableName = "currency_exchange_rate",
    primaryKeys = ["from_currency_id", "to_currency_id", "rate_date"]
)
data class CurrencyExchangeRateEntity(
    @ColumnInfo(name = "from_currency_id")
    val fromCurrencyId: Long,

    @ColumnInfo(name = "to_currency_id")
    val toCurrencyId: Long,

    @ColumnInfo(name = "rate_date")
    val rateDate: Long,

    @ColumnInfo(name = "rate")
    val rate: Float
)