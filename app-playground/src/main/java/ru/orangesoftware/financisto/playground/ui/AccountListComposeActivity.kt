package ru.orangesoftware.financisto.playground.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ru.orangesoftware.financisto.feature.account.ui.AccountListScreen

/**
 * Activity to showcase the new Compose-based Account List UI.
 * 
 * This activity demonstrates the modernized Account List implementation
 * using Jetpack Compose and the new feature module architecture.
 */
@AndroidEntryPoint
class AccountListComposeActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AccountListScreen()
                }
            }
        }
    }
}
