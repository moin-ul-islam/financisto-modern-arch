package ru.orangesoftware.financisto.feature.transaction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.core.ui.fragment.BaseFragment
import ru.orangesoftware.financisto.feature.transaction.TransactionFormViewModel
import ru.orangesoftware.financisto.feature.transaction.TransactionFormUiState
import ru.orangesoftware.financisto.feature.transaction.TransactionFormScreenState
import ru.orangesoftware.financisto.feature.transaction.TransactionFormAction
import ru.orangesoftware.financisto.feature.transaction.TransactionFormContentData
import ru.orangesoftware.financisto.feature.transaction.SaveState
import ru.orangesoftware.financisto.feature.transaction.ValidationError

/**
 * Modern Fragment implementation of the Transaction Form screen.
 * This replaces TransactionActivity when USE_FRAGMENT_ARCHITECTURE is enabled.
 */
@AndroidEntryPoint
class TransactionFormFragment : BaseFragment() {

    private val viewModel: TransactionFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (FeatureFlags.USE_FRAGMENT_ARCHITECTURE) {
            inflater.inflate(ru.orangesoftware.financisto.feature.transaction.R.layout.fragment_transaction_form, container, false)
        } else {
            // Fallback to empty view - should not be used when flag is disabled
            View(requireContext())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (!FeatureFlags.USE_FRAGMENT_ARCHITECTURE) {
            return
        }

        setupObservers()
        setupClickListeners()
        handleArguments()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun handleUiState(state: TransactionFormUiState) {
        when (val screenState = state.screenState) {
            is TransactionFormScreenState.Loading -> {
                showLoading(true)
                showError(null)
                showContent(false)
            }
            is TransactionFormScreenState.Empty -> {
                showLoading(false)
                showError(null)
                showContent(false)
                showEmptyState("Form not initialized")
            }
            is TransactionFormScreenState.Content -> {
                showLoading(false)
                showError(null)
                showContent(true)
                displayForm(screenState.data)
            }
            is TransactionFormScreenState.Error -> {
                showLoading(false)
                showError(screenState.message)
                showContent(false)
            }
        }

        // Handle save state
        when (state.saveState) {
            is SaveState.Saving -> {
                showSaving(true)
                enableFormInputs(false)
            }
            is SaveState.Success -> {
                showSaving(false)
                enableFormInputs(true)
                handleSaveSuccess(state.saveState.message)
            }
            is SaveState.Failed -> {
                showSaving(false)
                enableFormInputs(true)
                showSaveError(state.saveState.error)
            }
            else -> {
                showSaving(false)
                enableFormInputs(true)
            }
        }

        // Handle validation errors
        displayValidationErrors(state.validationErrors)
    }

    private fun setupClickListeners() {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.transaction.R.id.placeholder_text)?.setOnClickListener {
            collectFormDataAndSave()
        }
        
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.transaction.R.id.progress_bar)?.setOnClickListener {
            viewModel.handleAction(TransactionFormAction.ClearForm)
            closeCurrentScreen()
        }

        // TODO: Implement other click listeners for form fields
    }

    private fun handleArguments() {
        arguments?.let { args ->
            val transactionId = args.getLong("transactionId", -1L)
            val accountId = args.getLong("accountId", -1L)
            val duplicate = args.getBoolean("duplicate", false)
            val fromTemplate = args.getBoolean("fromTemplate", false)

            when {
                transactionId != -1L && duplicate -> {
                    viewModel.handleAction(TransactionFormAction.LoadTransaction(transactionId))
                    // TODO: Set duplicate mode
                }
                transactionId != -1L -> {
                    viewModel.handleAction(TransactionFormAction.LoadTransaction(transactionId))
                }
                fromTemplate -> {
                    viewModel.handleAction(TransactionFormAction.LoadTransaction(transactionId))
                    // TODO: Set template mode
                }
                accountId != -1L -> {
                    viewModel.handleAction(TransactionFormAction.InitializeForm)
                    // TODO: Set account
                }
                else -> {
                    viewModel.handleAction(TransactionFormAction.InitializeForm)
                }
            }
        } ?: run {
            viewModel.handleAction(TransactionFormAction.InitializeForm)
        }
    }

    private fun collectFormDataAndSave() {
        // TODO: Collect form data from UI fields and create save action
        // For now, just trigger a basic save action
        viewModel.handleAction(TransactionFormAction.SaveTransaction)
    }

    private fun showLoading(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.transaction.R.id.progress_bar)?.visibility = 
            if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showContent(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.transaction.R.id.placeholder_text)?.visibility = 
            if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun displayForm(data: TransactionFormContentData) {
        // TODO: Implement form display logic
        // This would populate all form fields with transaction data
        // For now, just show a toast
        Toast.makeText(
            requireContext(), 
            "Form loaded successfully", 
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSaving(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.transaction.R.id.placeholder_text)?.isEnabled = !show
        // TODO: Show saving indicator
    }

    private fun enableFormInputs(enable: Boolean) {
        // TODO: Enable/disable all form input fields
    }

    private fun handleSaveSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        closeCurrentScreen()
    }

    private fun showSaveError(error: String) {
        Toast.makeText(requireContext(), "Save failed: $error", Toast.LENGTH_LONG).show()
    }

    private fun displayValidationErrors(errors: List<ValidationError>) {
        // TODO: Display validation errors on form fields
        if (errors.isNotEmpty()) {
            val errorMessage = errors.joinToString("\n") { 
                when (it) {
                    ValidationError.AmountRequired -> it.message
                    ValidationError.AccountRequired -> it.message
                    ValidationError.CategoryRequired -> it.message
                    ValidationError.InvalidAmount -> it.message
                    ValidationError.NegativeAmount -> it.message
                    ValidationError.ToAccountRequired -> it.message
                    ValidationError.SameAccountTransfer -> it.message
                    ValidationError.SplitAmountMismatch -> it.message
                }
            }
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun newInstance(
            transactionId: Long = -1L,
            accountId: Long = -1L,
            duplicate: Boolean = false,
            fromTemplate: Boolean = false
        ): TransactionFormFragment {
            return TransactionFormFragment().apply {
                arguments = Bundle().apply {
                    if (transactionId != -1L) {
                        putLong("transactionId", transactionId)
                    }
                    if (accountId != -1L) {
                        putLong("accountId", accountId)
                    }
                    putBoolean("duplicate", duplicate)
                    putBoolean("fromTemplate", fromTemplate)
                }
            }
        }
    }
}
