package ru.orangesoftware.financisto.playground.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.feature.transaction.AccountOption
import ru.orangesoftware.financisto.feature.transaction.CategoryOption
import ru.orangesoftware.financisto.feature.transaction.SaveState
import ru.orangesoftware.financisto.feature.transaction.TransactionFormAction
import ru.orangesoftware.financisto.feature.transaction.TransactionFormScreenState
import ru.orangesoftware.financisto.feature.transaction.TransactionFormViewModel
import ru.orangesoftware.financisto.feature.transaction.ValidationError
import ru.orangesoftware.financisto.playground.R

/**
 * Demo activity that showcases the new TransactionFormViewModel from the feature:transaction module.
 * 
 * This demonstrates:
 * - Using feature module ViewModels
 * - StateFlow UI state observation  
 * - Form validation and state management
 * - User action handling
 * - Feature flag integration
 * - Modern MVVM architecture
 */
@AndroidEntryPoint
class TransactionFormDemoActivity : AppCompatActivity() {
    
    private val viewModel: TransactionFormViewModel by viewModels()
    
    private lateinit var amountEditText: EditText
    private lateinit var noteEditText: EditText
    private lateinit var accountSelectionText: TextView
    private lateinit var categorySelectionText: TextView
    private lateinit var dateTimeText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var featureFlagStatus: TextView
    private lateinit var validationStatus: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_form_demo)
        
        setupToolbar()
        setupViews()
        setupObservers()
        setupClickListeners()
        displayFeatureFlagStatus()
        
        // Initialize form for new transaction
        viewModel.handleAction(TransactionFormAction.ValidateForm)
    }
    
    private fun setupToolbar() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Transaction Form Demo"
        }
    }
    
    private fun setupViews() {
        amountEditText = findViewById(R.id.amountEditText)
        noteEditText = findViewById(R.id.noteEditText)
        accountSelectionText = findViewById(R.id.accountSelectionText)
        categorySelectionText = findViewById(R.id.categorySelectionText)
        dateTimeText = findViewById(R.id.dateTimeText)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        featureFlagStatus = findViewById(R.id.featureFlagStatus)
        validationStatus = findViewById(R.id.validationStatus)
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (val screenState = uiState.screenState) {
                    is TransactionFormScreenState.Loading -> {
                        progressBar.visibility = android.view.View.VISIBLE
                        errorText.visibility = android.view.View.GONE
                        // Disable form during loading
                        setFormEnabled(false)
                    }
                    is TransactionFormScreenState.Error -> {
                        progressBar.visibility = android.view.View.GONE
                        errorText.text = screenState.message
                        errorText.visibility = android.view.View.VISIBLE
                        setFormEnabled(true)
                    }
                    is TransactionFormScreenState.Empty -> {
                        progressBar.visibility = android.view.View.GONE
                        errorText.text = "No form data available"
                        errorText.visibility = android.view.View.VISIBLE
                        setFormEnabled(false)
                    }
                    is TransactionFormScreenState.Content -> {
                        progressBar.visibility = android.view.View.GONE
                        errorText.visibility = android.view.View.GONE
                        setFormEnabled(true)
                        
                        // Update form fields
                        if (amountEditText.text.toString() != uiState.amount) {
                            amountEditText.setText(uiState.amount)
                        }
                        
                        if (noteEditText.text.toString() != uiState.note) {
                            noteEditText.setText(uiState.note)
                        }
                        
                        // Update selected account display
                        accountSelectionText.text = uiState.selectedAccount?.let { account ->
                            "Account: ${account.title}"
                        } ?: "Select Account"
                        
                        // Update selected category display  
                        categorySelectionText.text = uiState.selectedCategory?.let { category ->
                            "Category: ${category.title}"
                        } ?: "Select Category"
                        
                        // Update date time display
                        dateTimeText.text = "Date: ${uiState.formattedDateTime}"
                        
                        // Update validation status
                        validationStatus.text = if (uiState.isFormValid) {
                            "✅ Form is valid"
                        } else {
                            "❌ Form has errors: ${uiState.validationErrors.joinToString(", ")}"
                        }
                        
                        // Enable/disable save button based on form validity and save state
                        val isSaving = uiState.saveState is SaveState.Saving
                        saveButton.isEnabled = uiState.isFormValid && !isSaving
                        
                        android.util.Log.d("TransactionDemo", "Form state updated - Valid: ${uiState.isFormValid}")
                    }
                }
            }
        }
    }
    
    private fun setFormEnabled(enabled: Boolean) {
        amountEditText.isEnabled = enabled
        noteEditText.isEnabled = enabled
        accountSelectionText.isEnabled = enabled
        categorySelectionText.isEnabled = enabled
        saveButton.isEnabled = enabled
        cancelButton.isEnabled = enabled
    }
    
    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            viewModel.handleAction(TransactionFormAction.SaveTransaction)
        }
        
        cancelButton.setOnClickListener {
            viewModel.handleAction(TransactionFormAction.ClearForm)
            finish()
        }
        
        // Amount field text watcher
        amountEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val amount = amountEditText.text.toString()
                viewModel.handleAction(TransactionFormAction.SetAmount(amount))
            }
        }
        
        // Note field text watcher
        noteEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val note = noteEditText.text.toString()
                viewModel.handleAction(TransactionFormAction.SetNote(note))
            }
        }
        
        // Account selection (demo)
        accountSelectionText.setOnClickListener {
            // In a real app, this would open account picker
            android.util.Log.d("TransactionDemo", "Account selection clicked")
            // Demo: Select first available account
            val demoAccount = AccountOption(1L, "Demo Cash Account", "$", "1000.00", android.R.drawable.ic_menu_gallery)
            viewModel.handleAction(TransactionFormAction.SetAccount(demoAccount))
        }
        
        // Category selection (demo)
        categorySelectionText.setOnClickListener {
            // In a real app, this would open category picker
            android.util.Log.d("TransactionDemo", "Category selection clicked")
            // Demo: Select demo category
            val demoCategory = CategoryOption(1L, "Demo Food", android.R.drawable.ic_menu_gallery, "EXPENSE")
            viewModel.handleAction(TransactionFormAction.SetCategory(demoCategory))
        }
        
        // Date time selection (demo)
        dateTimeText.setOnClickListener {
            // In a real app, this would open date/time picker
            android.util.Log.d("TransactionDemo", "Date time selection clicked")
            val currentTime = System.currentTimeMillis()
            viewModel.handleAction(TransactionFormAction.SetDateTime(currentTime))
        }
    }
    
    private fun displayFeatureFlagStatus() {
        val status = if (FeatureFlags.USE_TRANSACTION_FORM_VIEWMODEL) {
            "✅ Transaction Form ViewModel Feature Flag: ENABLED"
        } else {
            "❌ Transaction Form ViewModel Feature Flag: DISABLED"
        }
        featureFlagStatus.text = status
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
