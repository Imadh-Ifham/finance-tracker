package com.imadh.financetracker.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imadh.financetracker.models.Budget
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.notifications.NotificationHelper
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*

class SharedPreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "finance_tracker_prefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_BUDGET = "budget"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET_NOTIFICATIONS = "budget_notifications"
        private const val KEY_DAILY_REMINDER = "daily_reminder"
        private const val BACKUP_DIR = "backup/transactions"
        private const val BACKUP_FILE_NAME = "transactions_backup.json"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val notificationHelper = NotificationHelper(context)

    private val gson = Gson()

    // Transaction Methods

    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
        checkBudgetLimit()
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
    fun getTransactionsForCurrentMonth(): List<Transaction> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return getTransactions().filter { transaction ->
            calendar.time = transaction.date
            val transactionMonth = calendar.get(Calendar.MONTH)
            val transactionYear = calendar.get(Calendar.YEAR)
            transactionMonth == currentMonth && transactionYear == currentYear
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

    private fun checkBudgetLimit() {
        Log.d("TransactionRepository", "checkBudgetLimit() called") // <-- 🔥
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH)
        val year = calendar.get(java.util.Calendar.YEAR)

        val budget = getCurrentMonthBudget()
        val totalExpense = getTotalExpense(month, year)

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

    /**
     * Backup transactions to a JSON file in internal storage.
     */
    fun backupTransactionsToFile(context: Context): Boolean {
        val transactions = getTransactions()
        val json = gson.toJson(transactions)

        val backupDir = File(context.filesDir, BACKUP_DIR)
        if (!backupDir.exists()) {
            backupDir.mkdirs() // Create the directory if it doesn't exist
        }

        val backupFile = File(backupDir, BACKUP_FILE_NAME)
        return try {
            FileWriter(backupFile).use { it.write(json) }
            true // Indicate success
        } catch (e: IOException) {
            Log.e("Backup", "Failed to backup transactions", e)
            false // Indicate failure
        }
    }

    /**
     * Restore transactions from the backup JSON file in internal storage.
     */
    fun restoreTransactionsFromFile(context: Context): Boolean {
        val backupFile = File(context.filesDir, "$BACKUP_DIR/$BACKUP_FILE_NAME")
        if (!backupFile.exists()) {
            Log.e("Restore", "Backup file not found")
            return false // Indicate failure
        }

        return try {
            val type = object : TypeToken<List<Transaction>>() {}.type
            val restoredTransactions: List<Transaction> = FileReader(backupFile).use { gson.fromJson(it, type) }
            val existingTransactions = getTransactions().toMutableList()

            // Merge transactions without duplicates
            val mergedTransactions = (restoredTransactions + existingTransactions)
                .distinctBy { it.id } // Ensure uniqueness by ID
                .sortedByDescending { it.date } // Sort by date (descending)

            saveTransactions(mergedTransactions) // Save merged transactions back to SharedPreferences
            true // Indicate success
        } catch (e: IOException) {
            Log.e("Restore", "Failed to restore transactions", e)
            false // Indicate failure
        }
    }

    fun resetBackup(context: Context): Boolean {
        val backupFile = File(context.filesDir, "$BACKUP_DIR/$BACKUP_FILE_NAME")
        return if (backupFile.exists()) {
            backupFile.delete() // Delete the file
        } else {
            false // File does not exist
        }
    }
}