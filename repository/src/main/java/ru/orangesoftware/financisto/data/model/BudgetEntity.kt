package ru.orangesoftware.financisto.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Budget data.
 *
 * This entity represents budget definitions for tracking spending against limits.
 */
@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "category_id")
    val categories: String, // String of category IDs

    @ColumnInfo(name = "project_id")
    val projects: String, // String of project IDs

    @ColumnInfo(name = "currency_id")
    val currencyId: Long = -1,

    @ColumnInfo(name = "amount")
    val amount: Long,

    @ColumnInfo(name = "include_subcategories")
    val includeSubcategories: Boolean = false,

    @ColumnInfo(name = "expanded")
    val expanded: Boolean = false,

    @ColumnInfo(name = "include_credit")
    val includeCredit: Boolean = true,

    @ColumnInfo(name = "start_date")
    val startDate: Long,

    @ColumnInfo(name = "end_date")
    val endDate: Long,

    @ColumnInfo(name = "recur")
    val recur: String? = null,

    @ColumnInfo(name = "recur_num")
    val recurNum: Long = 0,

    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean = false,

    @ColumnInfo(name = "parent_budget_id")
    val parentBudgetId: Long = 0,

    @ColumnInfo(name = "updated_on")
    val updatedOn: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "remote_key")
    val remoteKey: String? = null,

    @ColumnInfo(name = "budget_account_id")
    val accountId: Long = 0,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Long = 0
)