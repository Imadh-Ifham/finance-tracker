package com.imadh.financetracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.imadh.financetracker.R
import com.imadh.financetracker.adapters.TransactionAdapter
import com.imadh.financetracker.bottomsheets.AddTransactionBottomSheet
import com.imadh.financetracker.databinding.FragmentTransactionsBinding
import com.imadh.financetracker.models.Transaction
import com.imadh.financetracker.utils.SharedPreferencesManager

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferencesManager
        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        // Setup RecyclerView
        setupTransactionsList()

        // Setup FAB
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionBottomSheet()
        }
    }

    private fun setupTransactionsList() {
        transactionAdapter = TransactionAdapter(
            transactions = sharedPreferencesManager.getTransactions(),
            onItemClick = { transaction ->
                // Handle transaction item click if needed
            }
        )

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }

        // Show/hide empty state
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val transactions = sharedPreferencesManager.getTransactions()
        if (transactions.isEmpty()) {
            binding.rvTransactions.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.rvTransactions.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun showAddTransactionBottomSheet() {
        val bottomSheet = AddTransactionBottomSheet.newInstance()

        // Set callback for when transaction is added
        bottomSheet.onTransactionAdded = { transaction ->
            // Update the transactions list
            transactionAdapter.updateTransactions(sharedPreferencesManager.getTransactions())
            // Update empty state visibility
            updateEmptyState()
        }

        bottomSheet.show(parentFragmentManager, AddTransactionBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TransactionsFragment()
    }
}