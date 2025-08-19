package ru.orangesoftware.financisto.feature.blotter.ui

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
import ru.orangesoftware.financisto.feature.blotter.BlotterViewModel
import ru.orangesoftware.financisto.feature.blotter.BlotterUiState
import ru.orangesoftware.financisto.feature.blotter.BlotterScreenState
import ru.orangesoftware.financisto.feature.blotter.BlotterAction
import ru.orangesoftware.financisto.feature.blotter.BlotterContentData
import ru.orangesoftware.financisto.feature.blotter.TotalCalculationState

/**
 * Modern Fragment implementation of the Blotter (transaction list) screen.
 * This replaces BlotterActivity when USE_FRAGMENT_ARCHITECTURE is enabled.
 */
@AndroidEntryPoint
class BlotterFragment : BaseFragment() {

    private val viewModel: BlotterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (FeatureFlags.USE_FRAGMENT_ARCHITECTURE) {
            inflater.inflate(ru.orangesoftware.financisto.feature.blotter.R.layout.fragment_blotter, container, false)
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

    private fun handleUiState(state: BlotterUiState) {
        when (val screenState = state.screenState) {
            is BlotterScreenState.Loading -> {
                showLoading(true)
                showError(null)
                showContent(false)
            }
            is BlotterScreenState.Empty -> {
                showLoading(false)
                showError(null)
                showContent(false)
                showEmptyState("No transactions found")
            }
            is BlotterScreenState.Content -> {
                showLoading(false)
                showError(null)
                showContent(true)
                displayTransactions(screenState.data)
            }
            is BlotterScreenState.Error -> {
                showLoading(false)
                showError(screenState.message)
                showContent(false)
            }
        }

        // Handle total calculation state
        when (state.totalCalculationState) {
            is TotalCalculationState.Calculating -> {
                showTotalCalculating(true)
            }
            is TotalCalculationState.Completed -> {
                showTotalCalculating(false)
                displayTotals(mapOf("total" to state.totalCalculationState.total))
            }
            is TotalCalculationState.Failed -> {
                showTotalCalculating(false)
                showTotalError(state.totalCalculationState.error)
            }
            else -> {
                showTotalCalculating(false)
            }
        }
    }

    private fun handleArguments() {
        arguments?.let { args ->
            val accountId = args.getLong("accountId", -1L)
            if (accountId != -1L) {
                viewModel.handleAction(BlotterAction.LoadAccountTransactions(accountId))
            } else {
                viewModel.handleAction(BlotterAction.LoadAllTransactions)
            }
        } ?: run {
            viewModel.handleAction(BlotterAction.LoadAllTransactions)
        }
    }

    private fun showLoading(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.blotter.R.id.progress_bar)?.visibility = 
            if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showContent(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.blotter.R.id.placeholder_text)?.visibility = 
            if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(message: String) {
        // TODO: Implement empty state view
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun displayTransactions(data: BlotterContentData) {
        // TODO: Implement transaction list display
        // This would populate the ListView or RecyclerView with transaction data
        // For now, just show a toast with the count
        Toast.makeText(
            requireContext(), 
            "Loaded ${data.transactions.size} transactions", 
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showTotalCalculating(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.blotter.R.id.placeholder_text)?.alpha = if (show) 0.5f else 1.0f
    }

    private fun displayTotals(totals: Map<String, String>) {
        // TODO: Implement total display logic
        // This would update the totals display with calculated values
    }

    private fun showTotalError(error: String) {
        Toast.makeText(requireContext(), "Total calculation error: $error", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(accountId: Long = -1L): BlotterFragment {
            return BlotterFragment().apply {
                arguments = Bundle().apply {
                    if (accountId != -1L) {
                        putLong("accountId", accountId)
                    }
                }
            }
        }
    }
}
