package ru.orangesoftware.financisto.playground.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.playground.R
import ru.orangesoftware.financisto.playground.databinding.ActivityRunningBalanceTestBinding
import ru.orangesoftware.financisto.playground.ui.viewmodel.RunningBalanceTestViewModel

/**
 * Activity to test running balance related components.
 * 
 * This demonstrates the running balance use cases that were implemented
 * in Phase 2.3 and verifies they work with proper Hilt DI.
 */
@AndroidEntryPoint
class RunningBalanceTestActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRunningBalanceTestBinding
    private val viewModel: RunningBalanceTestViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRunningBalanceTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnRebuildBalance.setOnClickListener {
            val accountId = binding.etAccountId.text.toString().toLongOrNull()
            if (accountId != null) {
                viewModel.rebuildRunningBalance(accountId)
            } else {
                binding.tvResult.text = "Please enter a valid account ID"
            }
        }
        
        binding.btnGetLastBalance.setOnClickListener {
            val accountId = binding.etAccountId.text.toString().toLongOrNull()
            if (accountId != null) {
                viewModel.getLastRunningBalance(accountId)
            } else {
                binding.tvResult.text = "Please enter a valid account ID"
            }
        }
        
        binding.btnRebuildAllBalances.setOnClickListener {
            viewModel.rebuildAllRunningBalances()
        }
    }
    
    private fun updateUI(state: RunningBalanceTestViewModel.UiState) {
        binding.progressBar.visibility = if (state.isLoading) android.view.View.VISIBLE else android.view.View.GONE
        binding.tvResult.text = state.result
        
        // Enable/disable buttons based on loading state
        val isEnabled = !state.isLoading
        binding.btnRebuildBalance.isEnabled = isEnabled
        binding.btnGetLastBalance.isEnabled = isEnabled
        binding.btnRebuildAllBalances.isEnabled = isEnabled
        binding.etAccountId.isEnabled = isEnabled
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
