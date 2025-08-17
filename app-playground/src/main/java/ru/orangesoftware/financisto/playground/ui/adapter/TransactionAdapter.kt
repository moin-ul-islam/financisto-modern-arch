package ru.orangesoftware.financisto.playground.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.orangesoftware.financisto.feature.blotter.BlotterTransactionItem
import ru.orangesoftware.financisto.playground.R

/**
 * Adapter for displaying transaction items from BlotterViewModel in the demo.
 */
class TransactionAdapter(
    private val onItemClick: (BlotterTransactionItem) -> Unit
) : ListAdapter<BlotterTransactionItem, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_demo, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val amountText: TextView = itemView.findViewById(R.id.amountText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val accountText: TextView = itemView.findViewById(R.id.accountText)
        private val noteText: TextView = itemView.findViewById(R.id.noteText)

        fun bind(transaction: BlotterTransactionItem) {
            categoryText.text = transaction.categoryName
            amountText.text = transaction.formattedAmount
            dateText.text = transaction.formattedDate
            accountText.text = transaction.fromAccountTitle
            noteText.text = transaction.note.ifEmpty { "No note" }
            
            // Show transfer info if applicable
            if (transaction.isTransfer && transaction.toAccountTitle.isNotEmpty()) {
                accountText.text = "${transaction.fromAccountTitle} → ${transaction.toAccountTitle}"
            }

            itemView.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BlotterTransactionItem>() {
        override fun areItemsTheSame(
            oldItem: BlotterTransactionItem,
            newItem: BlotterTransactionItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: BlotterTransactionItem,
            newItem: BlotterTransactionItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
