package com.imadh.financetracker.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.imadh.financetracker.R
import com.imadh.financetracker.databinding.DialogEditTransactionBinding
import com.imadh.financetracker.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class EditTransactionDialog : DialogFragment() {

    private var transaction: Transaction? = null
    private var onTransactionUpdatedListener: ((Transaction) -> Unit)? = null
    private lateinit var binding: DialogEditTransactionBinding

    fun setOnTransactionUpdatedListener(listener: (Transaction) -> Unit) {
        onTransactionUpdatedListener = listener
    }

    override fun onStart() {
        super.onStart()

        // Set the dialog width to 90% of the screen width and height to wrap content
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogEditTransactionBinding.inflate(inflater, container, false)

        // Set up category spinner based on transaction type
        setupTransactionTypeToggle()

        // Pre-fill data
        transaction?.let {
            binding.etTitle.setText(it.title)
            binding.etAmount.setText(it.amount.toString())
            binding.etDate.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it.date))

            // Set transaction type and category
            if (it.isExpense) {
                binding.radioExpense.isChecked = true
            } else {
                binding.radioIncome.isChecked = true
            }
            populateCategorySpinner(it.isExpense)
            binding.spinnerCategory.setSelection(getCategoryPosition(it.category, it.isExpense))
        }

        // Date picker for date input
        binding.etDate.setOnClickListener {
            // TODO: Implement date picker dialog logic
        }

        // Save button click
        binding.btnSave.setOnClickListener {
            val amountInput = binding.etAmount.text.toString()
            if (amountInput.isEmpty() || amountInput.toDoubleOrNull() == null) {
                binding.etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }

            val updatedTransaction = transaction?.copy(
                title = binding.etTitle.text.toString(),
                amount = amountInput.toDouble(),
                category = binding.spinnerCategory.selectedItem.toString(),
                isExpense = binding.radioExpense.isChecked
            )

            updatedTransaction?.let { onTransactionUpdatedListener?.invoke(it) }
            dismiss()
        }

        return binding.root
    }

    private fun setupTransactionTypeToggle() {
        binding.radioGroupTransactionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioExpense -> populateCategorySpinner(true)
                R.id.radioIncome -> populateCategorySpinner(false)
            }
        }
    }

    private fun populateCategorySpinner(isExpense: Boolean) {
        val categories = if (isExpense) {
            resources.getStringArray(R.array.expense_categories)
        } else {
            resources.getStringArray(R.array.income_categories)
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun getCategoryPosition(category: String, isExpense: Boolean): Int {
        val categories = if (isExpense) {
            resources.getStringArray(R.array.expense_categories)
        } else {
            resources.getStringArray(R.array.income_categories)
        }
        return categories.indexOf(category).takeIf { it >= 0 } ?: 0
    }

    companion object {
        const val TAG = "EditTransactionDialog"

        fun newInstance(transaction: Transaction): EditTransactionDialog {
            val dialog = EditTransactionDialog()
            dialog.transaction = transaction
            return dialog
        }
    }
}