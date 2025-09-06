package ru.orangesoftware.financisto.modern.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.modern.R

/**
 * RecyclerView adapter for displaying accounts in the playground app.
 */
class AccountListAdapter(
    private val onAccountClick: (AccountEntity) -> Unit
) : ListAdapter<AccountEntity, AccountListAdapter.AccountViewHolder>(AccountDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class AccountViewHolder(
        itemView: android.view.View
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val tvAccountName: android.widget.TextView = itemView.findViewById(R.id.tvAccountName)
        private val tvAccountType: android.widget.TextView = itemView.findViewById(R.id.tvAccountType)
        private val tvAccountBalance: android.widget.TextView = itemView.findViewById(R.id.tvAccountBalance)
        
        fun bind(account: AccountEntity) {
            tvAccountName.text = account.title
            tvAccountType.text = account.type
            
            // Format balance
            val balance = account.totalAmount / 100.0 // Convert from cents
            tvAccountBalance.text = String.format("$%.2f", balance)
            
            // Set balance color based on amount
            val colorRes = when {
                account.totalAmount > 0 -> R.color.positive_amount
                account.totalAmount < 0 -> R.color.negative_amount
                else -> R.color.neutral_amount
            }
            tvAccountBalance.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.context, colorRes))
            
            itemView.setOnClickListener {
                onAccountClick(account)
            }
        }
    }
    
    private class AccountDiffCallback : DiffUtil.ItemCallback<AccountEntity>() {
        override fun areItemsTheSame(oldItem: AccountEntity, newItem: AccountEntity): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: AccountEntity, newItem: AccountEntity): Boolean {
            return oldItem.title == newItem.title &&
                    oldItem.type == newItem.type &&
                    oldItem.totalAmount == newItem.totalAmount
        }
    }
}
