package com.imadh.financetracker.models

// Data class to represent each category breakdown
data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Double
)