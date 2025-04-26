package com.imadh.financetracker.models

data class Category(
    val id: String,
    val name: String,
    val isExpense: Boolean,
    // For UI display
    var amount: Double = 0.0,
    var percentage: Float = 0f
)