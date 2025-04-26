package com.imadh.financetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imadh.financetracker.R
import com.imadh.financetracker.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        this.transactions = newTransactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val tvTransactionTitle: TextView = itemView.findViewById(R.id.tv_transaction_title)
        private val tvTransactionCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
        private val tvTransactionDate: TextView = itemView.findViewById(R.id.tv_transaction_date)
        private val tvTransactionAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)

        fun bind(transaction: Transaction) {
            tvTransactionTitle.text = transaction.title
            tvTransactionCategory.text = transaction.category
            tvTransactionDate.text = dateFormatter.format(transaction.date)

            // Format amount with currency
            val amountText = if (transaction.isExpense) {
                "-$${transaction.amount}"
            } else {
                "+$${transaction.amount}"
            }

            tvTransactionAmount.text = amountText

            // Set text color based on transaction type
            val textColor = if (transaction.isExpense) {
                itemView.context.getColor(R.color.expense_color) // Define this color in colors.xml
            } else {
                itemView.context.getColor(R.color.income_color) // Define this color in colors.xml
            }
            tvTransactionAmount.setTextColor(textColor)

            // Set appropriate icon based on category (simplified version)
            // You can add category-specific icons later
            ivCategoryIcon.setImageResource(
                if (transaction.isExpense) R.drawable.ic_swap else R.drawable.ic_swap
            )

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }
}