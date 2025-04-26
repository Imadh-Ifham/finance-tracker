package com.imadh.financetracker.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.imadh.financetracker.R
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.databinding.BottomSheetAddTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionBottomSheet(private val onSave: (Transaction) -> Unit) :
    BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddTransactionBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: Date = Date() // Changed to Date object to match Transaction model
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Added super call
        setupCategorySpinner()
        setupDatePicker()
        binding.btnSave.setOnClickListener { validateAndSaveTransaction() }
    }

    private fun setupCategorySpinner() {
        // Use resource arrays if available or define directly as you have
        val expenseCategories = resources.getStringArray(R.array.expense_categories)
        val incomeCategories = resources.getStringArray(R.array.income_categories)

        // Start with expense categories (since expense is checked by default)
        updateCategorySpinner(true)

        // Update categories when transaction type changes
        binding.radioGroupTransactionType.setOnCheckedChangeListener { _, checkedId ->
            val isExpense = checkedId == R.id.radioExpense
            updateCategorySpinner(isExpense)
        }
    }

    private fun updateCategorySpinner(isExpense: Boolean) {
        val categories = if (isExpense) {
            resources.getStringArray(R.array.expense_categories)
        } else {
            resources.getStringArray(R.array.income_categories)
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etDate.setText(dateFormat.format(selectedDate))

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                time = selectedDate // Use the stored Date
            }

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time // Store as Date object
                    binding.etDate.setText(dateFormat.format(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun validateAndSaveTransaction() {
        val title = binding.etTitle.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem?.toString() ?: ""

        if (title.isEmpty()) {
            showToast("Title is required")
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showToast("Enter a valid amount")
            return
        }

        if (category.isEmpty()) {
            showToast("Please select a category")
            return
        }

        val isExpense = when (binding.radioGroupTransactionType.checkedRadioButtonId) {
            R.id.radioExpense -> true
            R.id.radioIncome -> false
            else -> {
                showToast("Please select transaction type")
                return
            }
        }

        // Create transaction with all required parameters
        val transaction = Transaction(
            id = UUID.randomUUID().toString(), // Generate unique ID
            title = title,
            amount = amount,
            category = category,
            isExpense = isExpense,
            date = selectedDate,
            createdAt = Date() // Current timestamp
        )

        onSave(transaction)
        dismiss()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddTransactionBottomSheet"

        fun newInstance(onSave: (Transaction) -> Unit): AddTransactionBottomSheet {
            return AddTransactionBottomSheet(onSave)
        }
    }
}