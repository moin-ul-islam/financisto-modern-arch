package ru.orangesoftware.financisto.modern.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.feature.account.AccountListAction
import ru.orangesoftware.financisto.feature.account.AccountListScreenState
import ru.orangesoftware.financisto.feature.account.AccountListViewModel
import ru.orangesoftware.financisto.feature.account.AccountSortOrder
import ru.orangesoftware.financisto.modern.R
import ru.orangesoftware.financisto.modern.ui.adapter.AccountDemoAdapter

/**
 * Demo activity that showcases the new AccountListViewModel from the feature:account module.
 * 
 * This demonstrates:
 * - Using feature module ViewModels  
 * - StateFlow UI state observation
 * - User action handling
 * - Feature flag integration
 * - Modern MVVM architecture
 */
@AndroidEntryPoint
class AccountViewModelDemoActivity : AppCompatActivity() {
    
    private val viewModel: AccountListViewModel by viewModels()
    private lateinit var adapter: AccountDemoAdapter
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var refreshButton: Button
    private lateinit var featureFlagStatus: TextView
    private lateinit var totalBalanceText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_demo)
        
        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        displayFeatureFlagStatus()
        
        // Load initial data
        viewModel.handleAction(AccountListAction.LoadAccounts)
    }
    
    private fun setupToolbar() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Account ViewModel Demo"
        }
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        refreshButton = findViewById(R.id.refreshButton)
        featureFlagStatus = findViewById(R.id.featureFlagStatus)
        totalBalanceText = findViewById(R.id.totalBalanceText)
    }
    
    private fun setupRecyclerView() {
        adapter = AccountDemoAdapter { account ->
            // Handle account click
            android.util.Log.d("AccountDemo", "Account clicked: ${account.title}")
            // Could navigate to account details or edit
        }
        
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (val screenState = uiState.screenState) {
                    is AccountListScreenState.Loading -> {
                        progressBar.visibility = android.view.View.VISIBLE
                        errorText.visibility = android.view.View.GONE
                        recyclerView.visibility = android.view.View.GONE
                    }
                    is AccountListScreenState.Error -> {
                        progressBar.visibility = android.view.View.GONE
                        errorText.text = screenState.message
                        errorText.visibility = android.view.View.VISIBLE
                        recyclerView.visibility = android.view.View.GONE
                    }
                    is AccountListScreenState.Empty -> {
                        progressBar.visibility = android.view.View.GONE
                        errorText.text = "No accounts found"
                        errorText.visibility = android.view.View.VISIBLE
                        recyclerView.visibility = android.view.View.GONE
                    }
                    is AccountListScreenState.Content -> {
                        progressBar.visibility = android.view.View.GONE
                        errorText.visibility = android.view.View.GONE
                        recyclerView.visibility = android.view.View.VISIBLE
                        
                        // Update account list
                        adapter.submitList(screenState.data.accounts)
                        
                        // Update total balance
                        totalBalanceText.text = "Total Balance: ${screenState.data.totalBalance}"
                        
                        android.util.Log.d("AccountDemo", "Accounts loaded: ${screenState.data.accounts.size}")
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        refreshButton.setOnClickListener {
            viewModel.handleAction(AccountListAction.RefreshAccounts)
        }
        
        findViewById<Button>(R.id.sortByNameButton)?.setOnClickListener {
            viewModel.handleAction(AccountListAction.SortBy(AccountSortOrder.NAME))
        }
        
        findViewById<Button>(R.id.sortByBalanceButton)?.setOnClickListener {
            viewModel.handleAction(AccountListAction.SortBy(AccountSortOrder.BALANCE))
        }
        
        findViewById<Button>(R.id.toggleActiveButton)?.setOnClickListener {
            // Toggle showing only active accounts
            android.util.Log.d("AccountDemo", "Toggle active accounts filter")
        }
    }
    
    private fun displayFeatureFlagStatus() {
        val status = if (FeatureFlags.USE_ACCOUNT_LIST_VIEWMODEL) {
            "✅ Account ViewModel Feature Flag: ENABLED"
        } else {
            "❌ Account ViewModel Feature Flag: DISABLED"
        }
        featureFlagStatus.text = status
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
