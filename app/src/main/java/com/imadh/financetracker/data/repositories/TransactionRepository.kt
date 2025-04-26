package com.imadh.financetracker.data.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.notifications.NotificationHelper
import com.imadh.financetracker.utils.SharedPreferencesManager

class TransactionRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("finance_tracker_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val key = "transaction_list"

    private val sharedPreferencesManager = SharedPreferencesManager(context)
    private val notificationHelper = NotificationHelper(context)

    // Save the full list of transactions
    fun saveTransaction(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString(key,json).apply()
        Log.d("TransactionRepository", "saveTransaction() called with ${transactions.size} transactions") // <-- 🔥
        checkBudgetLimit()
    }

    // Load all saved transactions
    fun loadTransactions(): List<Transaction> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>> () {}.type
        return gson.fromJson(json,type)
    }

    // Add a single transaction
    fun addTransaction(transaction: Transaction) {
        val transactions = loadTransactions().toMutableList()
        transactions.add(transaction)
        saveTransaction(transactions)
    }

    // Delete a transaction by ID
    fun deleteTransaction(id: String) {
        val transactions = loadTransactions().filterNot { it.id == id }
        saveTransaction(transactions)
    }

    // Optional: Clear all
    fun clearAll() {
        prefs.edit().remove(key).apply()
    }

    private fun checkBudgetLimit() {
        Log.d("TransactionRepository", "checkBudgetLimit() called") // <-- 🔥
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH)
        val year = calendar.get(java.util.Calendar.YEAR)

        val budget = sharedPreferencesManager.getCurrentMonthBudget()
        val totalExpense = sharedPreferencesManager.getTotalExpense(month, year)

        if (budget != null) {
            val percentSpent = budget.getPercentSpent(totalExpense)
            Log.d("TransactionRepository", "Budget: ${budget.amount}, Total Expense: $totalExpense, Percent Spent: $percentSpent")

            when {
                percentSpent >= 100 -> {
                    // Notify when budget exceeds 100%
                    notificationHelper.sendNotification(
                        "Budget Exceeded",
                        "You have exceeded your budget for this month!",
                        1001
                    )
                }
                percentSpent >= 80 -> {
                    // Notify when budget exceeds 80%
                    notificationHelper.sendNotification(
                        "Budget Alert",
                        "You have used $percentSpent% of your budget. Be cautious with spending!",
                        1002
                    )
                }
            }
        }
    }
}