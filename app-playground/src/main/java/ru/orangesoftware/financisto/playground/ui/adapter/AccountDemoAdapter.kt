package ru.orangesoftware.financisto.playground.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.orangesoftware.financisto.feature.account.AccountListItem
import ru.orangesoftware.financisto.playground.R

/**
 * Adapter for displaying account items from AccountListViewModel in the demo.
 */
class AccountDemoAdapter(
    private val onItemClick: (AccountListItem) -> Unit
) : ListAdapter<AccountListItem, AccountDemoAdapter.AccountViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account_demo, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val balanceText: TextView = itemView.findViewById(R.id.balanceText)
        private val typeText: TextView = itemView.findViewById(R.id.typeText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)

        fun bind(account: AccountListItem) {
            titleText.text = account.title
            balanceText.text = account.formattedBalance
            typeText.text = account.accountType
            statusText.text = if (account.isActive) "Active" else "Inactive"
            
            // Style based on account status
            val textColor = if (account.isActive) {
                androidx.core.content.ContextCompat.getColor(itemView.context, android.R.color.black)
            } else {
                androidx.core.content.ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
            }
            
            titleText.setTextColor(textColor)
            balanceText.setTextColor(textColor)

            itemView.setOnClickListener {
                onItemClick(account)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AccountListItem>() {
        override fun areItemsTheSame(
            oldItem: AccountListItem,
            newItem: AccountListItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: AccountListItem,
            newItem: AccountListItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
