package ru.orangesoftware.financisto.data.model

import androidx.room.DatabaseView

/**
 * Room DatabaseView for Blotter transactions.
 *
 * This view represents transactions that are not templates and not split parents.
 */
@DatabaseView(
    viewName = "v_blotter",
    value = "SELECT * FROM v_all_transactions WHERE is_template = 0 AND parent_id = 0"
)
data class BlotterView(
    // This would have all the columns from v_all_transactions
    // For brevity, we'll define it as a marker class
    // In practice, you'd define all the column mappings
    val id: Long = 0 // Placeholder
)