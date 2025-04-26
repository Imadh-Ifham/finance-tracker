package com.imadh.financetracker.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imadh.financetracker.models.Transaction

class TransactionRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("finance_tracker_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val key = "transaction_list"

    // Save the full list of transactions
    fun saveTransaction(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString(key,json).apply()
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
}