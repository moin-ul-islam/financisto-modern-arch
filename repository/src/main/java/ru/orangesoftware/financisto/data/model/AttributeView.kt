package ru.orangesoftware.financisto.data.model

import androidx.room.DatabaseView
import androidx.room.ColumnInfo

/**
 * Room DatabaseView for Attributes joined with their associated categories.
 *
 * This view shows attributes linked to categories through the category_attribute junction table.
 */
@DatabaseView(
    viewName = "v_attributes",
    value = """
        SELECT
            a._id as _id,
            a.title as title,
            a.type as type,
            a.list_values as list_values,
            a.default_value as default_value,
            c._id as category_id,
            c.`left` as category_left,
            c.`right` as category_right
        FROM attributes as a,
             category_attribute as ca,
             category c
        WHERE ca.attribute_id = a._id
          AND ca.category_id = c._id
    """
)
data class AttributeView(
    @ColumnInfo(name = "_id")
    val id: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "type")
    val type: Int,

    @ColumnInfo(name = "list_values")
    val listValues: String?,

    @ColumnInfo(name = "default_value")
    val defaultValue: String?,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "category_left")
    val categoryLeft: Int,

    @ColumnInfo(name = "category_right")
    val categoryRight: Int
)