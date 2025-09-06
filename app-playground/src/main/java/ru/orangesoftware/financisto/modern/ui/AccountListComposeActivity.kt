package ru.orangesoftware.financisto.modern.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.orangesoftware.financisto.feature.account.ui.AccountListScreen
import ru.orangesoftware.financisto.feature.account.ui.CreateAccountScreen

/**
 * Activity to showcase the new Compose-based Account List UI.
 * 
 * This activity demonstrates the modernized Account List implementation
 * using Jetpack Compose, Navigation Component, and the new feature module architecture.
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
                    AccountNavigationGraph()
                }
            }
        }
    }
}

@Composable
private fun AccountNavigationGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "account_list"
    ) {
        composable("account_list") {
            AccountListScreen(
                onNavigateToCreateAccount = {
                    navController.navigate("create_account")
                },
                onNavigateToAccountDetails = { accountId ->
                    // TODO: Implement account details navigation
                    android.util.Log.d("Navigation", "Navigate to account details: $accountId")
                },
                onNavigateToAccountTotals = {
                    // TODO: Implement account totals navigation
                    android.util.Log.d("Navigation", "Navigate to account totals")
                },
                onNavigateToBlotter = { accountId ->
                    // TODO: Implement blotter navigation
                    android.util.Log.d("Navigation", "Navigate to blotter for account: $accountId")
                },
                onNavigateToEditAccount = { accountId ->
                    navController.navigate("edit_account/$accountId")
                },
                onNavigateToAddTransaction = { accountId ->
                    // TODO: Implement transaction navigation
                    android.util.Log.d("Navigation", "Navigate to add transaction for account: $accountId")
                },
                onNavigateToAddTransfer = { accountId ->
                    // TODO: Implement transfer navigation
                    android.util.Log.d("Navigation", "Navigate to add transfer for account: $accountId")
                },
                onNavigateToUpdateBalance = { accountId ->
                    // TODO: Implement update balance navigation
                    android.util.Log.d("Navigation", "Navigate to update balance for account: $accountId")
                },
                onNavigateToPurgeAccount = { accountId ->
                    // TODO: Implement purge account navigation
                    android.util.Log.d("Navigation", "Navigate to purge account: $accountId")
                }
            )
        }
        
        composable("create_account") {
            CreateAccountScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCurrency = {
                    // TODO: Implement add currency navigation
                    android.util.Log.d("Navigation", "Navigate to add currency")
                },
                onAccountCreated = { accountId ->
                    android.util.Log.d("Navigation", "Account created with ID: $accountId")
                    navController.popBackStack()
                }
            )
        }
        
        composable("edit_account/{accountId}") { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: -1
            // TODO: Implement edit account screen with proper account ID handling
            // For now, redirect to create account screen
            CreateAccountScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCurrency = {
                    android.util.Log.d("Navigation", "Navigate to add currency from edit account: $accountId")
                },
                onAccountCreated = { updatedAccountId ->
                    android.util.Log.d("Navigation", "Account $accountId updated with ID: $updatedAccountId")
                    navController.popBackStack()
                }
            )
        }
    }
}
