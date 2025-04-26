package com.imadh.financetracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.imadh.financetracker.databinding.FragmentBudgetBinding
import com.imadh.financetracker.dialogs.EditBudgetDialog
import com.imadh.financetracker.utils.SharedPreferencesManager

import android.graphics.Color
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.imadh.financetracker.R

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        pieChart = binding.root.findViewById(R.id.pie_chart)

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

            // Update the PieChart
            updatePieChart(totalExpense, remaining)
        } else {
            // No budget set
            binding.tvBudgetAmount.text = "No budget set"
            binding.tvBudgetStatus.text = "Set a budget to start tracking"
            binding.progressBudget.progress = 0

            // Clear the PieChart
            updatePieChart(0.0, 0.0)
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

    private fun updatePieChart(expense: Double, remaining: Double) {
        val entries = ArrayList<PieEntry>()

        // Add data to the PieChart
        if (expense > 0) {
            entries.add(PieEntry(expense.toFloat(), "Spent"))
        }
        if (remaining > 0) {
            entries.add(PieEntry(remaining.toFloat(), "Remaining"))
        }

        val dataSet = PieDataSet(entries, "Budget Breakdown")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "Budget"
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setUsePercentValues(true)
        pieChart.invalidate() // Refresh the chart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}