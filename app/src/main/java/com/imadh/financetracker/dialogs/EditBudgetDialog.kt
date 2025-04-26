package com.imadh.financetracker.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.imadh.financetracker.databinding.DialogEditBudgetBinding
import com.imadh.financetracker.models.Budget
import java.util.*

class EditBudgetDialog(private val onSave: (Budget) -> Unit) : DialogFragment() {

    private var _binding: DialogEditBudgetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveBudget.setOnClickListener {
            saveBudget()
        }
    }

    override fun onStart() {
        super.onStart()
        // Set the dialog width to match the parent and height to wrap content
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun saveBudget() {
        val amountText = binding.etBudgetAmount.text.toString().trim()

        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a budget amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current month and year
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        // Create Budget object
        val budget = Budget(
            amount = amount,
            month = month,
            year = year
        )

        // Pass the budget back to the fragment
        onSave(budget)

        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}