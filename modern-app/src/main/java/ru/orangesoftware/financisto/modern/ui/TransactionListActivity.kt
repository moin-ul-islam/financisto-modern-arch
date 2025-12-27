package ru.orangesoftware.financisto.modern.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.orangesoftware.financisto.modern.R

/**
 * Placeholder activity for testing transaction-related components.
 */
@AndroidEntryPoint
class TransactionListActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_placeholder)
        
        supportActionBar?.apply {
            title = "Transactions Test"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
