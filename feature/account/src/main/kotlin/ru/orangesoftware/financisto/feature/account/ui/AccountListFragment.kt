package ru.orangesoftware.financisto.feature.account.ui

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
import ru.orangesoftware.financisto.feature.account.AccountListViewModel
import ru.orangesoftware.financisto.feature.account.AccountListUiState
import ru.orangesoftware.financisto.feature.account.AccountListScreenState
import ru.orangesoftware.financisto.feature.account.AccountListAction
import ru.orangesoftware.financisto.feature.account.AccountListContentData
import ru.orangesoftware.financisto.feature.account.TotalCalculationState

/**
 * Modern Fragment implementation of the Account List screen.
 * This replaces AccountListActivity when USE_FRAGMENT_ARCHITECTURE is enabled.
 */
@AndroidEntryPoint
class AccountListFragment : BaseFragment() {

    private val viewModel: AccountListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (FeatureFlags.USE_FRAGMENT_ARCHITECTURE) {
            inflater.inflate(ru.orangesoftware.financisto.feature.account.R.layout.fragment_account_list, container, false)
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
        loadData()
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

    private fun handleUiState(state: AccountListUiState) {
        when (val screenState = state.screenState) {
            is AccountListScreenState.Loading -> {
                showLoading(true)
                showError(null)
                showContent(false)
            }
            is AccountListScreenState.Empty -> {
                showLoading(false)
                showError(null)
                showContent(false)
                showEmptyState("No accounts found")
            }
            is AccountListScreenState.Content -> {
                showLoading(false)
                showError(null)
                showContent(true)
                displayAccounts(screenState.data)
            }
            is AccountListScreenState.Error -> {
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
                displayTotalBalance(mapOf("total" to state.totalCalculationState.total))
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

    private fun setupClickListeners() {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.account.R.id.placeholder_text)?.setOnClickListener {
            viewModel.handleAction(AccountListAction.CreateNewAccount)
        }
        
        // TODO: Handle account item clicks to navigate to account details
        // TODO: Handle account item clicks to navigate to account blotter
    }

    private fun loadData() {
        viewModel.handleAction(AccountListAction.LoadAccounts)
    }

    private fun showLoading(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.account.R.id.progress_bar)?.visibility = 
            if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showContent(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.account.R.id.placeholder_text)?.visibility = 
            if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(message: String) {
        // TODO: Implement empty state view
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun displayAccounts(data: AccountListContentData) {
        // TODO: Implement account list display
        // This would populate the ListView or RecyclerView with account data
        // For now, just show a toast with the count
        Toast.makeText(
            requireContext(), 
            "Loaded ${data.accounts.size} accounts", 
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showTotalCalculating(show: Boolean) {
        view?.findViewById<View>(ru.orangesoftware.financisto.feature.account.R.id.placeholder_text)?.alpha = if (show) 0.5f else 1.0f
    }

    private fun displayTotalBalance(totals: Map<String, String>) {
        // TODO: Implement total balance display logic
        // This would update the total balance display
    }

    private fun showTotalError(error: String) {
        Toast.makeText(requireContext(), "Balance calculation error: $error", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(): AccountListFragment {
            return AccountListFragment()
        }
    }
}
