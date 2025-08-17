package ru.orangesoftware.financisto.playground.ui

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
import ru.orangesoftware.financisto.feature.blotter.BlotterAction
import ru.orangesoftware.financisto.feature.blotter.BlotterViewModel
import ru.orangesoftware.financisto.playground.R
import ru.orangesoftware.financisto.playground.ui.adapter.TransactionAdapter

/**
 * Demo activity that showcases the new BlotterViewModel from the feature:blotter module.
 * 
 * This demonstrates:
 * - Using feature module ViewModels
 * - StateFlow UI state observation
 * - User action handling
 * - Feature flag integration
 * - Modern MVVM architecture
 */
@AndroidEntryPoint
class BlotterViewModelDemoActivity : AppCompatActivity() {
    
    private val viewModel: BlotterViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var refreshButton: Button
    private lateinit var featureFlagStatus: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blotter_demo)
        
        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        displayFeatureFlagStatus()
        
        // Load initial data
        viewModel.handleAction(BlotterAction.RefreshTransactions)
    }
    
    private fun setupToolbar() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Blotter ViewModel Demo"
        }
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        refreshButton = findViewById(R.id.refreshButton)
        featureFlagStatus = findViewById(R.id.featureFlagStatus)
    }
    
    private fun setupRecyclerView() {
        adapter = TransactionAdapter { transaction ->
            // Handle transaction click
            android.util.Log.d("BlotterDemo", "Transaction clicked: ${transaction.id}")
        }
        
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                // Update loading state
                progressBar.visibility = if (uiState.isLoading) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // Update error state
                if (uiState.error != null) {
                    errorText.text = uiState.error
                    errorText.visibility = android.view.View.VISIBLE
                    recyclerView.visibility = android.view.View.GONE
                } else {
                    errorText.visibility = android.view.View.GONE
                    recyclerView.visibility = android.view.View.VISIBLE
                }
                
                // Update transaction list
                adapter.submitList(uiState.transactions)
                
                // Update total amount display (could add a text view for this)
                android.util.Log.d("BlotterDemo", "Total amount: ${uiState.totalAmount}")
            }
        }
    }
    
    private fun setupClickListeners() {
        refreshButton.setOnClickListener {
            viewModel.handleAction(BlotterAction.RefreshTransactions)
        }
        
        findViewById<Button>(R.id.filterAllButton)?.setOnClickListener {
            viewModel.handleAction(BlotterAction.ClearFilter)
        }
        
        findViewById<Button>(R.id.sortByDateButton)?.setOnClickListener {
            viewModel.handleAction(BlotterAction.LoadTransactions)
        }
        
        findViewById<Button>(R.id.sortByAmountButton)?.setOnClickListener {
            viewModel.handleAction(BlotterAction.LoadTransactions)
        }
    }
    
    private fun displayFeatureFlagStatus() {
        val status = if (FeatureFlags.USE_BLOTTER_VIEWMODEL) {
            "✅ Blotter ViewModel Feature Flag: ENABLED"
        } else {
            "❌ Blotter ViewModel Feature Flag: DISABLED"
        }
        featureFlagStatus.text = status
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
