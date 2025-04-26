package com.imadh.financetracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}