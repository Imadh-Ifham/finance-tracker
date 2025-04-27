package com.imadh.financetracker.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.imadh.financetracker.R
import com.imadh.financetracker.databinding.FragmentSettingsBinding
import com.imadh.financetracker.utils.SharedPreferencesManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferencesManager
        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        // Set up Backup button
        binding.btnBackup.setOnClickListener {
            showBackupConfirmationDialog()
        }

        // Set up Restore button
        binding.btnRestore.setOnClickListener {
            showRestoreConfirmationDialog()
        }

        // Set up Restore button
        binding.btnResetBackup.setOnClickListener {
            showResetBackupConfirmationDialog()
        }

        // Populate the Currency Spinner
        setupCurrencySpinner()
    }

    private fun setupCurrencySpinner() {
        // Get the currency array from resources
        val currencies = resources.getStringArray(R.array.currencies)

        // Set up the Spinner adapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter

        // Set the selected item to the saved currency
        val savedCurrency = sharedPreferencesManager.getCurrency()
        val savedCurrencyIndex = currencies.indexOfFirst { it.contains("($savedCurrency)") }
        if (savedCurrencyIndex != -1) {
            binding.spinnerCurrency.setSelection(savedCurrencyIndex)
        }

        // Handle selection changes
        binding.spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCurrency = currencies[position]

                // Extract the value inside parentheses using regex
                val regex = "\\((.*?)\\)".toRegex()
                val matchResult = regex.find(selectedCurrency)
                val currencySymbol = matchResult?.groups?.get(1)?.value ?: selectedCurrency

                // Save only the currency symbol
                sharedPreferencesManager.setCurrency(currencySymbol)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun showBackupConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Backup")
            .setMessage("Do you want to back up your transactions?")
            .setPositiveButton("Yes") { _, _ ->
                val success = sharedPreferencesManager.backupTransactionsToFile(requireContext())
                if (success) {
                    Toast.makeText(requireContext(), "Backup successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Backup failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showRestoreConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Restore")
            .setMessage("Do you want to restore your transactions from the backup?")
            .setPositiveButton("Yes") { _, _ ->
                val success = sharedPreferencesManager.restoreTransactionsFromFile(requireContext())
                if (success) {
                    Toast.makeText(requireContext(), "Restore successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Restore failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showResetBackupConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Warning: Reset Backup")
            .setMessage("This action will permanently delete your backup. Are you sure you want to proceed?")
            .setIcon(android.R.drawable.ic_dialog_alert) // Warning icon
            .setPositiveButton("Yes") { _, _ ->
                val success = sharedPreferencesManager.resetBackup(requireContext())
                if (success) {
                    Toast.makeText(requireContext(), "Backup reset successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to reset backup", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}