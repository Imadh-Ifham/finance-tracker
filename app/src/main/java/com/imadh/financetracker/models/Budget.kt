package com.imadh.financetracker.models

data class Budget(
    val amount: Double, // Total budget amount
    val month: Int,     // Month for the budget (0 = January, 11 = December)
    val year: Int       // Year for the budget
) {
    // Calculate the percentage of the budget spent
    fun getPercentSpent(amountSpent: Double): Float {
        return if (amount <= 0) 0f else (amountSpent / amount * 100).toFloat()
    }

    // Calculate the remaining budget
    fun getRemainingBudget(amountSpent: Double): Double {
        return amount - amountSpent
    }
}