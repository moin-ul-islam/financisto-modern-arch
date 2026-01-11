package ru.orangesoftware.financisto.data.model

import androidx.room.DatabaseView
import androidx.room.ColumnInfo

/**
 * Room DatabaseView for Blotter transactions.
 *
 * This view represents transactions that are not templates, showing both sides of transfers.
 * It's a UNION of two queries:
 * 1. Transactions from the perspective of the from_account (normal transactions and outgoing transfers)
 * 2. Transactions from the perspective of the to_account (incoming transfers with swapped amounts and is_transfer=-1)
 *
 * This allows an account blotter to show both outgoing and incoming transfers as separate rows.
 * 
 * Note: Uses LEFT OUTER JOIN for category to support synthetic categories (split=-1, no-category=0)
 * that don't exist in the category table. Use case layer handles null category titles.
 */
@DatabaseView(
    viewName = "v_blotter",
    value = """
       SELECT
	t._id as _id,
	t.parent_id as parent_id,
	a._id as from_account_id,
	a.title as from_account_title,
	a.is_include_into_totals as from_account_is_include_into_totals,
	c._id as from_account_currency_id,
	a2._id as to_account_id,
	a2.title as to_account_title,
	a2.currency_id as to_account_currency_id,
	t.category_id as category_id,
	cat.title as category_title,
	cat.`left` as category_left,
	cat.`right` as category_right,
    cat.type as category_type,
	p._id as project_id,
	p.title as project,
	loc._id as location_id,
	loc.title as location,
	pp._id as payee_id,
	pp.title as payee,
	t.note as note,
	t.from_amount as from_amount,
	t.to_amount as to_amount,
	t.datetime as datetime,
	t.original_currency_id as original_currency_id,
	t.original_from_amount as original_from_amount,
	t.is_template as is_template,
	t.template_name as template_name,
	t.recurrence as recurrence,
	t.notification_options as notification_options,
	t.status as status,
	t.last_recurrence as last_recurrence,
	t.attached_picture as attached_picture,
	rb.balance as from_account_balance,
	0 as to_account_balance,
	t.to_account_id as is_transfer
FROM
	transactions as t
	INNER JOIN account as a ON a._id=t.from_account_id
	INNER JOIN currency as c ON c._id=a.currency_id
	LEFT OUTER JOIN category as cat ON cat._id=t.category_id
	LEFT OUTER JOIN running_balance as rb ON rb.transaction_id=(CASE WHEN t.parent_id=0 THEN t._id ELSE t.parent_id END) AND rb.account_id=t.from_account_id
	LEFT OUTER JOIN account as a2 ON a2._id=t.to_account_id
	LEFT OUTER JOIN locations as loc ON loc._id=t.location_id
	LEFT OUTER JOIN project as p ON p._id=t.project_id
	LEFT OUTER JOIN payee as pp ON pp._id=t.payee_id
WHERE is_template=0
UNION ALL
SELECT
	t._id as _id,
	t.parent_id as parent_id,
	a._id as from_account_id,
	a.title as from_account_title,
	a.is_include_into_totals as from_account_is_include_into_totals,
	c._id as from_account_currency_id,
	a2._id as to_account_id,
	a2.title as to_account_title,
	a2.currency_id as to_account_currency_id,
	t.category_id as category_id,
	cat.title as category_title,
	cat.`left` as category_left,
	cat.`right` as category_right,
	cat.type as category_type,
	p._id as project_id,
	p.title as project,
	loc._id as location_id,
	loc.title as location,
	pp._id as payee_id,
	pp.title as payee,
	t.note as note,
	t.to_amount as from_amount,
	t.from_amount as to_amount,
	t.datetime as datetime,
	t.original_currency_id as original_currency_id,
	t.original_from_amount as original_from_amount,
	t.is_template as is_template,
	t.template_name as template_name,
	t.recurrence as recurrence,
	t.notification_options as notification_options,
	t.status as status,
	t.last_recurrence as last_recurrence,
	t.attached_picture as attached_picture,
	rb.balance as from_account_balance,
	0 as to_account_balance,
	-1 as is_transfer
FROM
	transactions as t
	INNER JOIN account as a ON a._id=t.to_account_id
	INNER JOIN currency as c ON c._id=a.currency_id
	LEFT OUTER JOIN category as cat ON cat._id=t.category_id
	LEFT OUTER JOIN running_balance as rb ON rb.transaction_id=t._id AND rb.account_id=t.to_account_id
	LEFT OUTER JOIN account as a2 ON a2._id=t.from_account_id
	LEFT OUTER JOIN locations as loc ON loc._id=t.location_id
	LEFT OUTER JOIN project as p ON p._id=t.project_id
	LEFT OUTER JOIN payee as pp ON pp._id=t.payee_id
WHERE is_template=0; 
    """
)
data class BlotterView(
    @ColumnInfo(name = "_id")
    val id: Long,

    @ColumnInfo(name = "parent_id")
    val parentId: Long,

    @ColumnInfo(name = "from_account_id")
    val fromAccountId: Long,

    @ColumnInfo(name = "from_account_title")
    val fromAccountTitle: String,

    @ColumnInfo(name = "from_account_is_include_into_totals")
    val fromAccountIsIncludeIntoTotals: Int,

    @ColumnInfo(name = "from_account_currency_id")
    val fromAccountCurrencyId: Long,

    @ColumnInfo(name = "to_account_id")
    val toAccountId: Long?,

    @ColumnInfo(name = "to_account_title")
    val toAccountTitle: String?,

    @ColumnInfo(name = "to_account_currency_id")
    val toAccountCurrencyId: Long?,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "category_title")
    val categoryTitle: String?,

    @ColumnInfo(name = "category_left")
    val categoryLeft: Int?,

    @ColumnInfo(name = "category_right")
    val categoryRight: Int?,

    @ColumnInfo(name = "category_type")
    val categoryType: Int?,

    @ColumnInfo(name = "project_id")
    val projectId: Long?,

    @ColumnInfo(name = "project")
    val project: String?,

    @ColumnInfo(name = "location_id")
    val locationId: Long?,

    @ColumnInfo(name = "location")
    val location: String?,

    @ColumnInfo(name = "payee_id")
    val payeeId: Long?,

    @ColumnInfo(name = "payee")
    val payee: String?,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "from_amount")
    val fromAmount: Long,

    @ColumnInfo(name = "to_amount")
    val toAmount: Long,

    @ColumnInfo(name = "datetime")
    val datetime: Long,

    @ColumnInfo(name = "original_currency_id")
    val originalCurrencyId: Long?,

    @ColumnInfo(name = "original_from_amount")
    val originalFromAmount: Long?,

    @ColumnInfo(name = "is_template")
    val isTemplate: Int,

    @ColumnInfo(name = "template_name")
    val templateName: String?,

    @ColumnInfo(name = "recurrence")
    val recurrence: String?,

    @ColumnInfo(name = "notification_options")
    val notificationOptions: String?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "last_recurrence")
    val lastRecurrence: Long?,

    @ColumnInfo(name = "attached_picture")
    val attachedPicture: String?,

    @ColumnInfo(name = "from_account_balance")
    val fromAccountBalance: Long?,

    @ColumnInfo(name = "to_account_balance")
    val toAccountBalance: Long,

    @ColumnInfo(name = "is_transfer")
    val isTransfer: Long?
)