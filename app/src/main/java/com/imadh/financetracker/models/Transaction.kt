package com.imadh.financetracker.models

import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val date: Date = Date(),
    val createdAt: Date = Date()
) {
    // Helper function to get positive or negative amount based on transaction type
    fun getSignedAmount(): Double {
        return if (isExpense) -amount else amount
    }
}