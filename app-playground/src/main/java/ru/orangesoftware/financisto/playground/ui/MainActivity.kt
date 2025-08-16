package ru.orangesoftware.financisto.playground.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.orangesoftware.financisto.playground.R

/**
 * Main playground activity that provides access to test various
 * modern architecture components.
 * 
 * This activity uses Hilt for dependency injection and demonstrates
 * how the new architecture components work in isolation.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        findViewById<android.widget.Button>(R.id.btnTestAccounts)?.setOnClickListener {
            startActivity(Intent(this, AccountListActivity::class.java))
        }
        
        findViewById<android.widget.Button>(R.id.btnTestTransactions)?.setOnClickListener {
            startActivity(Intent(this, TransactionListActivity::class.java))
        }
        
        findViewById<android.widget.Button>(R.id.btnTestRunningBalance)?.setOnClickListener {
            startActivity(Intent(this, RunningBalanceTestActivity::class.java))
        }
        
        findViewById<android.widget.Button>(R.id.btnTestRoomMigration)?.setOnClickListener {
            // TODO: Implement Room migration test activity
            android.util.Log.d("MainActivity", "Room migration test clicked")
        }
    }
}
