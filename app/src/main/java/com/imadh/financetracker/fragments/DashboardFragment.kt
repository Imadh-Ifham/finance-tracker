package com.imadh.financetracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.imadh.financetracker.adapters.CategoryBreakdownAdapter
import com.imadh.financetracker.databinding.FragmentDashboardBinding
import com.imadh.financetracker.models.CategoryBreakdown
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.utils.SharedPreferencesManager

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var barChart: BarChart

    private lateinit var expenseAdapter: CategoryBreakdownAdapter
    private lateinit var incomeAdapter: CategoryBreakdownAdapter

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
        // Setup RecyclerViews
        setupRecyclerViews()
        loadDashboardData()
    }

    private fun setupRecyclerViews() {
        // Setup Expense Breakdown RecyclerView
        expenseAdapter = CategoryBreakdownAdapter(sharedPreferencesManager)
        binding.rvExpenseBreakdown.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
        }

        // Setup Income Breakdown RecyclerView
        incomeAdapter = CategoryBreakdownAdapter(sharedPreferencesManager)
        binding.rvIncomeBreakdown.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = incomeAdapter
        }
    }
    private fun setupUI() {
        barChart = binding.barChartIncomeVsExpense
    }

    private fun loadDashboardData() {
        // Filter transactions for the current month and year
        val currentMonthTransactions = sharedPreferencesManager.getTransactionsForCurrentMonth()

        // Fetch the selected currency
        val currency = sharedPreferencesManager.getCurrency()

        // Calculate income, expense, and cash in hand for the current month
        val income = currentMonthTransactions.filter { !it.isExpense }.sumOf { it.amount }
        val expense = currentMonthTransactions.filter { it.isExpense }.sumOf { it.amount }
        val cashInHand = income - expense

        // Display income, expense, and cash in hand with selected currency
        binding.tvCashInHandValue.text = "$currency$cashInHand"
        binding.tvIncomeValue.text = "$currency$income"
        binding.tvExpenseValue.text = "$currency$expense"

        // Populate the bar chart
        populateBarChart(currentMonthTransactions, currency)

        // Populate Expense Breakdown RecyclerView
        val expenseBreakdown = calculateCategoryBreakdown(currentMonthTransactions.filter { it.isExpense }, expense)
        expenseAdapter.submitList(expenseBreakdown)

        // Populate Income Breakdown RecyclerView
        val incomeBreakdown = calculateCategoryBreakdown(currentMonthTransactions.filter { !it.isExpense }, income)
        incomeAdapter.submitList(incomeBreakdown)
    }

    private fun calculateCategoryBreakdown(transactions: List<Transaction>, totalAmount: Double): List<CategoryBreakdown> {
        return transactions
            .groupBy { it.category }
            .map { (category, transactionsInCategory) ->
                val categoryTotal = transactionsInCategory.sumOf { it.amount }
                val percentage = if (totalAmount > 0) (categoryTotal / totalAmount) * 100 else 0.0
                CategoryBreakdown(
                    category = category,
                    amount = categoryTotal,
                    percentage = percentage
                )
            }
            .filter { it.amount > 0 } // Exclude categories with zero amounts
    }

    private fun populateBarChart(transactions: List<Transaction>, currency: String) {
        // Get the current month and year
        val calendar = java.util.Calendar.getInstance()
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1 // Months are 0-indexed
        val currentYear = calendar.get(java.util.Calendar.YEAR)

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

        // Create BarEntries for income and expense
        val barEntriesIncome = listOf(BarEntry(0f, incomeForCurrentMonth.toFloat())) // x = 0
        val barEntriesExpense = listOf(BarEntry(1f, expenseForCurrentMonth.toFloat())) // x = 1

        // Create datasets
        val incomeDataSet = BarDataSet(barEntriesIncome, "Income ($currency)").apply {
            color = Color.GREEN
        }
        val expenseDataSet = BarDataSet(barEntriesExpense, "Expense ($currency)").apply {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}