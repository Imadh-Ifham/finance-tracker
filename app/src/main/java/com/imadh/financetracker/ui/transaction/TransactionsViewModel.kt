package com.imadh.financetracker.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.data.repositories.TransactionRepository

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application)

    // LiveData to observe the list of transactions
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    init {
        // Load saved data initially
        _transactions.value = repository.loadTransactions()
    }

    // Add a new transaction
    fun addTransaction(transaction:Transaction) {
        repository.addTransaction(transaction)
        _transactions.value = repository.loadTransactions()
    }

    // Delete a transaction by ID
    fun deleteTransaction(id: String) {
        repository.deleteTransaction(id)
        _transactions.value = repository.loadTransactions()
    }
}