package com.imadh.financetracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imadh.financetracker.models.Budget
import com.imadh.financetracker.models.Transaction
import java.util.*

class SharedPreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "finance_tracker_prefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_BUDGET = "budget"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET_NOTIFICATIONS = "budget_notifications"
        private const val KEY_DAILY_REMINDER = "daily_reminder"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val gson = Gson()

    // Transaction Methods

    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }

        if (index != -1) {
            transactions[index] = updatedTransaction
            saveTransactions(transactions)
        }
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getTransactions().toMutableList()
        transactions.removeIf { it.id == transactionId }
        saveTransactions(transactions)
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Get transactions for a specific month and year
    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        return getTransactions().filter { transaction ->
            val calendar = Calendar.getInstance().apply {
                time = transaction.date
            }
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
    }

    // Budget Methods

    fun saveBudget(budget: Budget) {
        val json = gson.toJson(budget)
        sharedPreferences.edit().putString(KEY_BUDGET, json).apply()
    }

    fun getBudget(): Budget? {
        val json = sharedPreferences.getString(KEY_BUDGET, null) ?: return null
        return gson.fromJson(json, Budget::class.java)
    }

    fun getCurrentMonthBudget(): Budget? {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val budget = getBudget() ?: return null

        // Return the budget only if it matches the current month and year
        return if (budget.month == currentMonth && budget.year == currentYear) {
            budget
        } else {
            null
        }
    }

    // Settings Methods

    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    fun setBudgetNotifications(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BUDGET_NOTIFICATIONS, enabled).apply()
    }

    fun areBudgetNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BUDGET_NOTIFICATIONS, true)
    }

    fun setDailyReminder(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DAILY_REMINDER, enabled).apply()
    }

    fun isDailyReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DAILY_REMINDER, false)
    }

    // Summary Methods

    fun getTotalIncome(month: Int? = null, year: Int? = null): Double {
        val transactions = if (month != null && year != null) {
            getTransactionsForMonth(month, year)
        } else {
            getTransactions()
        }

        return transactions
            .filter { !it.isExpense }
            .sumOf { it.amount }
    }

    fun getTotalExpense(month: Int? = null, year: Int? = null): Double {
        val transactions = if (month != null && year != null) {
            getTransactionsForMonth(month, year)
        } else {
            getTransactions()
        }

        return transactions
            .filter { it.isExpense }
            .sumOf { it.amount }
    }

    fun getBalance(): Double {
        val transactions = getTransactions()
        return transactions.sumOf { if (it.isExpense) -it.amount else it.amount }
    }

    // Backup Methods

    fun getTransactionsJson(): String {
        return sharedPreferences.getString(KEY_TRANSACTIONS, "[]") ?: "[]"
    }

    fun restoreTransactionsFromJson(json: String) {
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }
}