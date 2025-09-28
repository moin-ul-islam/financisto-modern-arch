package ru.orangesoftware.financisto.modern.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.modern.R
import ru.orangesoftware.financisto.modern.ui.adapter.AccountListAdapter
import ru.orangesoftware.financisto.modern.ui.viewmodel.AccountListViewModel

/**
 * Activity to test account-related modern architecture components.
 * 
 * This demonstrates:
 * - Hilt dependency injection
 * - ViewModel with use cases
 * - Repository pattern
 * - Room database operations
 * - UI state management
 */
@AndroidEntryPoint
class AccountListActivity : AppCompatActivity() {
    
    private val viewModel: AccountListViewModel by viewModels()
    private lateinit var adapter: AccountListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_list)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
        
    
    private fun setupToolbar() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = AccountListAdapter { account ->
            // Handle account click
            android.util.Log.d("AccountList", "Account clicked: ${account.title}")
        }
        
        findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            layoutManager = LinearLayoutManager(this@AccountListActivity)
            adapter = this@AccountListActivity.adapter
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun setupClickListeners() {
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabCreateAccount)?.setOnClickListener {
            viewModel.createTestAccount()
        }
    }
    
    private fun updateUI(state: AccountListViewModel.UiState) {
        findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = 
            if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
        
        findViewById<android.widget.TextView>(R.id.tvError)?.apply {
            visibility = if (state.error != null) android.view.View.VISIBLE else android.view.View.GONE
            state.error?.let { text = "Error: $it" }
        }
        
        findViewById<android.widget.TextView>(R.id.tvEmpty)?.visibility = 
            if (state.accounts.isEmpty() && !state.isLoading && state.error == null) 
                android.view.View.VISIBLE else android.view.View.GONE
        
        findViewById<RecyclerView>(R.id.recyclerView)?.visibility = 
            if (state.accounts.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        
        adapter.submitList(state.accounts)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
