package ru.orangesoftware.financisto.data.model

import androidx.room.DatabaseView
import androidx.room.ColumnInfo

/**
 * Room DatabaseView for Category hierarchy with level calculation.
 *
 * This view represents the category tree with computed level information
 * using the nested set model.
 */
@DatabaseView(
    viewName = "v_category",
    value = """
        SELECT
            node._id as _id,
            node.title as title,
            node.`left` as `left`,
            node.`right` as `right`,
            node.type as type,
            node.last_location_id as last_location_id,
            node.last_project_id as last_project_id,
            node.sort_order as sort_order,
            COUNT(parent._id) - 1 as level
        FROM category as node, category as parent
        WHERE node.`left` BETWEEN parent.`left` AND parent.`right`
        GROUP BY node._id
        ORDER BY node.`left`
    """
)
data class CategoryView(
    @ColumnInfo(name = "_id")
    val id: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "left")
    val left: Int,

    @ColumnInfo(name = "right")
    val right: Int,

    @ColumnInfo(name = "type")
    val type: Int,

    @ColumnInfo(name = "last_location_id")
    val lastLocationId: Long,

    @ColumnInfo(name = "last_project_id")
    val lastProjectId: Long,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Long,

    @ColumnInfo(name = "level")
    val level: Int
)