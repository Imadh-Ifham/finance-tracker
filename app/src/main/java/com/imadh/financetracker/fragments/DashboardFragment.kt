package com.imadh.financetracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.imadh.financetracker.databinding.FragmentDashboardBinding
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.utils.SharedPreferencesManager

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var barChart: BarChart
    private lateinit var expensePieChart: PieChart
    private lateinit var incomePieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferencesManager
        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        // Setup UI components
        setupUI()
        loadDashboardData()
    }

    private fun setupUI() {
        barChart = binding.barChartIncomeVsExpense
        expensePieChart = binding.pieChartExpense
        incomePieChart = binding.pieChartIncome
    }

    private fun loadDashboardData() {
        val transactions = sharedPreferencesManager.getTransactions()

        // Calculate income, expense, and cash in hand
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
        val expense = transactions.filter { it.isExpense }.sumOf { it.amount }
        val cashInHand = income - expense

        // Display income, expense, and cash in hand
        binding.tvCashInHandValue.text = "$${cashInHand}"
        binding.tvIncomeValue.text = "$${income}"
        binding.tvExpenseValue.text = "$${expense}"

        // Populate charts
        populateBarChart(transactions)
        populatePieCharts(transactions)
    }

    private fun populateBarChart(transactions: List<Transaction>) {
        // Get the current month and year
        val calendar = java.util.Calendar.getInstance()
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1 // Months are 0-indexed
        val currentYear = calendar.get(java.util.Calendar.YEAR)

        // Log all transactions for debugging
        transactions.forEach { transaction ->
            println("Transaction Date: ${transaction.date}, Amount: ${transaction.amount}, IsExpense: ${transaction.isExpense}")
        }

        // Filter transactions for the current month
        val incomeForCurrentMonth = transactions.filter { transaction ->
            calendar.time = transaction.date
            val transactionMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val transactionYear = calendar.get(java.util.Calendar.YEAR)
            !transaction.isExpense && transactionMonth == currentMonth && transactionYear == currentYear
        }.sumOf { it.amount }

        val expenseForCurrentMonth = transactions.filter { transaction ->
            calendar.time = transaction.date
            val transactionMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val transactionYear = calendar.get(java.util.Calendar.YEAR)
            transaction.isExpense && transactionMonth == currentMonth && transactionYear == currentYear
        }.sumOf { it.amount }

        // Log the filtered income and expense
        println("Income for Current Month: $incomeForCurrentMonth")
        println("Expense for Current Month: $expenseForCurrentMonth")

        // Create BarEntries for income and expense
        val barEntriesIncome = listOf(BarEntry(0f, incomeForCurrentMonth.toFloat())) // x = 0
        val barEntriesExpense = listOf(BarEntry(1f, expenseForCurrentMonth.toFloat())) // x = 1

        // Log the BarEntries
        println("BarEntries Income: $barEntriesIncome")
        println("BarEntries Expense: $barEntriesExpense")

        // Create datasets
        val incomeDataSet = BarDataSet(barEntriesIncome, "Income").apply {
            color = Color.GREEN
        }
        val expenseDataSet = BarDataSet(barEntriesExpense, "Expense").apply {
            color = Color.RED
        }

        // Combine datasets into BarData
        val barData = BarData(incomeDataSet, expenseDataSet)
        barData.barWidth = 0.4f // Set the bar width

        // Configure BarChart
        barChart.data = barData
        barChart.xAxis.apply {
            granularity = 1f // Ensure the x-axis labels are evenly spaced
            isGranularityEnabled = true
            setCenterAxisLabels(true) // Enable center alignment for grouped bars
            position = XAxis.XAxisPosition.BOTTOM // Place labels at the bottom
            axisMinimum = -0.5f // Start slightly before the first bar
            axisMaximum = 1.5f // End slightly after the second bar
        }

        // Configure the Y-axis to start at 0
        barChart.axisLeft.apply {
            axisMinimum = 0f // Force the Y-axis to start at 0
        }
        barChart.axisRight.isEnabled = false // Disable the right Y-axis

        // Group bars and adjust spacing
        barChart.groupBars(-0.5f, 0.4f, 0.1f) // (startX, groupSpace, barSpace)

        // Set chart description and refresh
        barChart.description = Description().apply {
            text = "Income vs Expense for Current Month"
            textColor = Color.BLACK
        }
        barChart.invalidate() // Refresh chart
    }

    private fun populatePieCharts(transactions: List<Transaction>) {
        // Expense Pie Chart
        val expenseEntries = transactions.filter { it.isExpense }
            .groupBy { it.category }
            .map { PieEntry(it.value.sumOf { transaction -> transaction.amount }.toFloat(), it.key) }
        val expenseDataSet = PieDataSet(expenseEntries, "Expenses by Category").apply {
            colors = listOf(Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA)
        }
        expensePieChart.data = PieData(expenseDataSet)
        expensePieChart.description = Description().apply {
            text = "Expense Breakdown"
            textColor = Color.BLACK
        }
        expensePieChart.invalidate()

        // Income Pie Chart
        val incomeEntries = transactions.filter { !it.isExpense }
            .groupBy { it.category }
            .map { PieEntry(it.value.sumOf { transaction -> transaction.amount }.toFloat(), it.key) }
        val incomeDataSet = PieDataSet(incomeEntries, "Income by Category").apply {
            colors = listOf(Color.GREEN, Color.CYAN, Color.LTGRAY, Color.DKGRAY)
        }
        incomePieChart.data = PieData(incomeDataSet)
        incomePieChart.description = Description().apply {
            text = "Income Breakdown"
            textColor = Color.BLACK
        }
        incomePieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}