package com.imadh.financetracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.imadh.financetracker.R
import com.imadh.financetracker.databinding.FragmentBudgetBinding
import com.imadh.financetracker.dialogs.EditBudgetDialog
import com.imadh.financetracker.utils.SharedPreferencesManager
import java.util.Calendar

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var barChart: HorizontalBarChart
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        // Initialize the charts
        barChart = binding.root.findViewById(R.id.bar_chart_budget)
        lineChart = binding.root.findViewById(R.id.line_chart_budget)

        // Load budget and display it
        loadBudget()

        // Set up edit budget button
        binding.btnEditBudget.setOnClickListener {
            showEditBudgetDialog()
        }
    }

    private fun loadBudget() {
        // Get current month and year
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH)
        val year = calendar.get(java.util.Calendar.YEAR)

        val budget = sharedPreferencesManager.getCurrentMonthBudget()
        val totalIncome = sharedPreferencesManager.getTotalIncome(month, year)
        val totalExpense = sharedPreferencesManager.getTotalExpense(month, year)
        val currency = sharedPreferencesManager.getCurrency()

        if (budget != null) {
            val percentSpent = budget.getPercentSpent(totalExpense)
            val remaining = budget.getRemainingBudget(totalExpense)

            // Update UI
            binding.tvBudgetAmount.text = "${currency}${budget.amount}"
            binding.tvBudgetStatus.text =
                "${currency}${totalExpense} of ${currency}${budget.amount} (${percentSpent.toInt()}%)"
            binding.progressBudget.progress = percentSpent.toInt()

            // Update the charts
            updateHorizontalBarChart(totalExpense, remaining)
            updateLineChart(budget.amount, totalExpense)
        } else {
            // No budget set
            binding.tvBudgetAmount.text = "No budget set"
            binding.tvBudgetStatus.text = "Set a budget to start tracking"
            binding.progressBudget.progress = 0

            // Clear the charts
            updateHorizontalBarChart(0.0, 0.0)
            clearLineChart()
        }
    }

    private fun showEditBudgetDialog() {
        val dialog = EditBudgetDialog { budget ->
            // Save the budget to SharedPreferences
            sharedPreferencesManager.saveBudget(budget)
            // Reload the budget to update the UI
            loadBudget()
        }
        dialog.show(parentFragmentManager, "EditBudgetDialog")
    }

    private fun updateHorizontalBarChart(expense: Double, remaining: Double) {
        val entries = ArrayList<BarEntry>()

        // Add data to the BarChart
        if (expense > 0) {
            entries.add(BarEntry(0f, expense.toFloat(), "Spent"))
        }
        if (remaining > 0) {
            entries.add(BarEntry(1f, remaining.toFloat(), "Remaining"))
        }

        val dataSet = BarDataSet(entries, "Budget Breakdown")
        dataSet.colors = listOf(Color.RED, Color.GREEN)
        dataSet.valueTextColor = Color.WHITE // Set value labels to white for dark mode
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.setFitBars(true)

        // Customize X-axis labels
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = entries.size
            valueFormatter = BarChartXAxisFormatter(listOf("Spent", "Remaining"))
            textColor = Color.WHITE // Labels visible in dark mode
        }

        // Hide right Y-axis
        barChart.axisRight.isEnabled = false

        // Configure Y-axis (Left)
        val maxValue = (expense + remaining).toFloat()
        barChart.axisLeft.apply {
            axisMinimum = 0f // Start from 0
            axisMaximum = (maxValue * 1.2f) // Add 20% padding above the max value
            granularity = maxValue / 5 // 5 equal intervals
            setDrawGridLines(true)
            textColor = Color.WHITE // Labels visible in dark mode
        }

        // Set the chart background color for dark mode
        barChart.setBackgroundColor(Color.BLACK)

        // Refresh the chart
        barChart.invalidate()
    }

    private fun updateLineChart(budgetLimit: Double, totalExpense: Double) {
        val entries = ArrayList<Entry>()

        // Fetch cumulative expenses for the current month
        val cumulativeExpenses = getCumulativeExpensesForCurrentMonth()

        // Populate the LineChart with cumulative expenses
        cumulativeExpenses.forEach { (day, expense) ->
            entries.add(Entry(day.toFloat(), expense.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Cumulative Expenses")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        dataSet.setCircleColor(Color.BLUE)
        dataSet.setDrawValues(false) // Hide value labels on the graph

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Add a red line for the budget limit
        val budgetLimitLine = LimitLine(budgetLimit.toFloat(), "Budget Limit")
        budgetLimitLine.lineColor = Color.RED
        budgetLimitLine.lineWidth = 2f
        budgetLimitLine.textColor = Color.RED
        budgetLimitLine.textSize = 12f
        lineChart.axisLeft.addLimitLine(budgetLimitLine)

        // Add a yellow line for 80% of the budget
        val eightyPercentLine = LimitLine((budgetLimit * 0.8).toFloat(), "80% of Budget")
        eightyPercentLine.lineColor = Color.YELLOW
        eightyPercentLine.lineWidth = 2f
        eightyPercentLine.textColor = Color.YELLOW
        eightyPercentLine.textSize = 12f
        lineChart.axisLeft.addLimitLine(eightyPercentLine)

        // Configure chart appearance
        lineChart.description.isEnabled = false
        lineChart.axisRight.isEnabled = false

        // Set the chart background color for dark mode
        lineChart.setBackgroundColor(Color.BLACK)

        // Update axis and grid line colors for dark mode
        lineChart.axisLeft.apply {
            textColor = Color.WHITE // Labels visible in dark mode
            gridColor = Color.DKGRAY // Gridlines visible in dark mode
        }
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.apply {
            textColor = Color.WHITE // Labels visible in dark mode
            gridColor = Color.DKGRAY // Gridlines visible in dark mode
        }

        // Refresh the chart
        lineChart.invalidate()
    }

    private fun getCumulativeExpensesForCurrentMonth(): Map<Int, Double> {
        val transactions = sharedPreferencesManager.getTransactionsForCurrentMonth()
        val calendar = Calendar.getInstance()

        // Group transactions by day and sum expenses
        val dailyExpenses = mutableMapOf<Int, Double>()
        transactions.filter { it.isExpense }.forEach { transaction ->
            calendar.time = transaction.date
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            dailyExpenses[day] = dailyExpenses.getOrDefault(day, 0.0) + transaction.amount
        }

        // Ensure all days of the month have an entry (default to 0.0)
        val currentMonthDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..currentMonthDays) {
            dailyExpenses.putIfAbsent(day, 0.0)
        }

        // Calculate cumulative expenses
        val cumulativeExpenses = mutableMapOf<Int, Double>()
        var runningTotal = 0.0
        for (day in 1..currentMonthDays) {
            runningTotal += dailyExpenses[day] ?: 0.0
            cumulativeExpenses[day] = runningTotal
        }

        return cumulativeExpenses
    }

    private fun clearLineChart() {
        lineChart.clear()
        lineChart.invalidate()
    }

    private fun getDailyExpensesForCurrentMonth(): Map<Int, Double> {
        val transactions = sharedPreferencesManager.getTransactionsForCurrentMonth()
        val calendar = Calendar.getInstance()

        // Group transactions by day and sum expenses
        val dailyExpenses = mutableMapOf<Int, Double>()
        transactions.filter { it.isExpense }.forEach { transaction ->
            calendar.time = transaction.date
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            dailyExpenses[day] = dailyExpenses.getOrDefault(day, 0.0) + transaction.amount
        }

        // Ensure all days of the month have an entry (default to 0.0)
        val currentMonthDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..currentMonthDays) {
            dailyExpenses.putIfAbsent(day, 0.0)
        }

        return dailyExpenses.toSortedMap()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Formatter for BarChart X-axis labels
class BarChartXAxisFormatter(private val labels: List<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return labels.getOrNull(value.toInt()) ?: ""
    }
}