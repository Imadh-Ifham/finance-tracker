package com.imadh.financetracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imadh.financetracker.R
import com.imadh.financetracker.adapters.TransactionAdapter
import com.imadh.financetracker.bottomsheets.AddTransactionBottomSheet
import com.imadh.financetracker.databinding.FragmentTransactionsBinding
import com.imadh.financetracker.dialogs.EditTransactionDialog
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

        // Setup Toolbar
        setupToolbar()

        // Setup RecyclerView
        setupTransactionsList()
        setupSwipeToDelete()
        setupLongPressToEdit()

        // Setup FAB
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionBottomSheet()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.transactions)
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

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val transaction = transactionAdapter.getTransactionAt(position)

                // Show confirmation dialog
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { _, _ ->
                        // Delete transaction
                        sharedPreferencesManager.deleteTransaction(transaction.id)
                        transactionAdapter.removeTransaction(position)
                        updateEmptyState()
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        // Cancel deletion and restore item
                        transactionAdapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvTransactions)
    }

    private fun setupLongPressToEdit() {
        transactionAdapter.setOnItemLongClickListener { transaction ->
            // Open the edit dialog
            val editDialog = EditTransactionDialog.newInstance(transaction)
            editDialog.setOnTransactionUpdatedListener { updatedTransaction ->
                sharedPreferencesManager.updateTransaction(updatedTransaction)
                transactionAdapter.updateTransaction(updatedTransaction)
            }
            editDialog.show(parentFragmentManager, EditTransactionDialog.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TransactionsFragment()
    }
}