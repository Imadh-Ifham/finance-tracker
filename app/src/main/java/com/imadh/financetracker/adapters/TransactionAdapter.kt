package com.imadh.financetracker.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imadh.financetracker.R
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.utils.SharedPreferencesManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val currencyFormatter = NumberFormat.getCurrencyInstance()

    private var onItemLongClickListener: ((Transaction) -> Unit)? = null

    // Define listener for long press
    fun setOnItemLongClickListener(listener: (Transaction) -> Unit) {
        onItemLongClickListener = listener
    }

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

    fun updateTransaction(updatedTransaction: Transaction) {
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions = transactions.toMutableList().apply { this[index] = updatedTransaction }
            notifyItemChanged(index)
        }
    }

    // Method to get a transaction by its position
    fun getTransactionAt(position: Int): Transaction = transactions[position]

    // Method to remove a transaction by its position
    fun removeTransaction(position: Int) {
        transactions = transactions.toMutableList().apply { removeAt(position) }
        notifyItemRemoved(position)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val tvTransactionTitle: TextView = itemView.findViewById(R.id.tv_transaction_title)
        private val tvTransactionCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
        private val tvTransactionDate: TextView = itemView.findViewById(R.id.tv_transaction_date)
        private val tvTransactionAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)

        fun bind(transaction: Transaction) {

            // Get the preferred currency from SharedPreferences
            val sharedPreferencesManager = SharedPreferencesManager(itemView.context)
            val currency = sharedPreferencesManager.getCurrency()

            // Set title
            tvTransactionTitle.text = transaction.title

            // Set category
            tvTransactionCategory.text = transaction.category

            // Set formatted date
            val formattedDate = dateFormatter.format(transaction.date)
            tvTransactionDate.text = formattedDate

            // Format the amount with the currency symbol
            val amountText = if (transaction.isExpense) {
                "-$currency${transaction.amount}"
            } else {
                "+$currency${transaction.amount}"
            }
            tvTransactionAmount.text = amountText

            val textColor = if (transaction.isExpense) {
                itemView.context.getColor(R.color.expense_color) // Red for Expenses
            } else {
                itemView.context.getColor(R.color.income_color) // Green for Income
            }
            tvTransactionAmount.setTextColor(textColor)

            // Set category icon
            val iconResId = getCategoryIcon(transaction.category, transaction.isExpense)
            ivCategoryIcon.setImageResource(iconResId)

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(transaction)
            }

            // Set long click listener
            itemView.setOnLongClickListener {
                onItemLongClickListener?.invoke(transaction)
                true
            }

        }


        // Get the appropriate icon for the transaction category
        private fun getCategoryIcon(category: String, isExpense: Boolean): Int {
            return when (category) {
                // Expense Categories
                "Food" -> R.drawable.ic_food
                "Transport" -> R.drawable.ic_transport
                "Entertainment" -> R.drawable.ic_entertainment
                "Shopping" -> R.drawable.ic_shopping
                "Bills" -> R.drawable.ic_bills
                "Health" -> R.drawable.ic_health
                "Education" -> R.drawable.ic_education
                "Housing" -> R.drawable.ic_housing
                "Travel" -> R.drawable.ic_travel

                // Income Categories
                "Salary" -> R.drawable.ic_salary
                "Freelance" -> R.drawable.ic_freelance
                "Business" -> R.drawable.ic_businness
                "Investments" -> R.drawable.ic_investment
                "Gift" -> R.drawable.ic_gifts

                // Default Icon
                else -> R.drawable.ic_swap
            }
        }
    }
}