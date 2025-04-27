package com.imadh.financetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imadh.financetracker.R
import com.imadh.financetracker.models.CategoryBreakdown
import com.imadh.financetracker.utils.SharedPreferencesManager

class CategoryBreakdownAdapter(private val sharedPreferencesManager: SharedPreferencesManager) :
    RecyclerView.Adapter<CategoryBreakdownAdapter.CategoryBreakdownViewHolder>() {

    private val categories = mutableListOf<CategoryBreakdown>()


    fun submitList(newCategories: List<CategoryBreakdown>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryBreakdownViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryBreakdownViewHolder(view, sharedPreferencesManager)
    }

    override fun onBindViewHolder(holder: CategoryBreakdownViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    class CategoryBreakdownViewHolder(
        itemView: View,
        private val sharedPreferencesManager: SharedPreferencesManager
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvCategoryAmount: TextView = itemView.findViewById(R.id.tv_category_amount)
        private val progressCategory: ProgressBar = itemView.findViewById(R.id.progress_category)
        private val tvCategoryPercentage: TextView = itemView.findViewById(R.id.tv_category_percentage)

        val currency = sharedPreferencesManager.getCurrency()

        fun bind(categoryBreakdown: CategoryBreakdown) {
            tvCategoryName.text = categoryBreakdown.category
            tvCategoryAmount.text = "${currency}${categoryBreakdown.amount}"
            progressCategory.progress = categoryBreakdown.percentage.toInt()
            tvCategoryPercentage.text = "${categoryBreakdown.percentage.toInt()}% of total"
        }
    }
}