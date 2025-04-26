package com.imadh.financetracker.bottomsheets

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.imadh.financetracker.R
import com.imadh.financetracker.databinding.BottomSheetAddTransactionBinding
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.utils.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAddTransactionBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private var selectedDate = Date()
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Callback to notify parent fragment when transaction is added
    var onTransactionAdded: ((Transaction) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        setupCategorySpinner()
        setupDatePicker()
        setupSaveButton()
    }

    private fun setupCategorySpinner() {
        // Load appropriate categories based on transaction type
        updateCategorySpinner(binding.radioExpense.isChecked)

        // Listen for transaction type changes
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
        // Set current date as default
        binding.etDate.setText(dateFormatter.format(selectedDate))

        // Show date picker when field is clicked
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply {
            time = selectedDate
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                selectedDate = calendar.time
                binding.etDate.setText(dateFormatter.format(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate title
        if (binding.etTitle.text.isNullOrBlank()) {
            binding.etTitle.error = "Please enter a title"
            isValid = false
        }

        // Validate amount
        val amountStr = binding.etAmount.text.toString()
        if (amountStr.isBlank()) {
            binding.etAmount.error = "Please enter an amount"
            isValid = false
        } else {
            try {
                val amount = amountStr.toDouble()
                if (amount <= 0) {
                    binding.etAmount.error = "Amount must be greater than zero"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.etAmount.error = "Invalid amount"
                isValid = false
            }
        }

        return isValid
    }

    private fun saveTransaction() {
        val title = binding.etTitle.text.toString().trim()
        val amount = binding.etAmount.text.toString().toDouble()
        val category = binding.spinnerCategory.selectedItem.toString()
        val isExpense = binding.radioExpense.isChecked

        val transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            isExpense = isExpense,
            date = selectedDate
        )

        // Save to SharedPreferences
        sharedPreferencesManager.saveTransaction(transaction)

        // Notify parent fragment
        onTransactionAdded?.invoke(transaction)

        // Close the bottom sheet
        dismiss()
    }

    companion object {
        const val TAG = "AddTransactionBottomSheet"

        fun newInstance(): AddTransactionBottomSheet {
            return AddTransactionBottomSheet()
        }
    }
}