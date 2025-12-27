package ru.orangesoftware.financisto.modern.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ru.orangesoftware.financisto.feature.account.CreateAccountViewModel
import ru.orangesoftware.financisto.feature.account.CreateAccountAction
import ru.orangesoftware.financisto.feature.account.ui.AccountListScreen
import ru.orangesoftware.financisto.feature.account.ui.CreateAccountScreen
import ru.orangesoftware.financisto.feature.account.ui.CurrencySelectionScreen
import ru.orangesoftware.financisto.feature.account.ui.CreateCustomCurrencyScreen

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
}@Composable
private fun AccountNavigationGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "account_list"
    ) {
        composable("account_list") {
            AccountListScreen(
                backStackEntry = it,
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
                    navController.navigate("transaction_form?accountId=$accountId")
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
            val viewModel = hiltViewModel<CreateAccountViewModel>()
            val backStackEntry = navController.currentBackStackEntry
            val shouldRefreshCurrencies = backStackEntry
                ?.savedStateHandle
                ?.getLiveData<Boolean>("refresh_currencies")
                ?.value ?: false
            val selectedCurrencyId = backStackEntry
                ?.savedStateHandle
                ?.getLiveData<Long>("selected_currency_id")
                ?.value
            
            // Clear the flags
            backStackEntry?.savedStateHandle?.set("refresh_currencies", false)
            backStackEntry?.savedStateHandle?.set("selected_currency_id", null)
            
            CreateAccountScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCurrency = {
                    navController.navigate("currency_selection")
                },
                onAccountCreated = { accountId ->
                    android.util.Log.d("Navigation", "Account created with ID: $accountId")
                    navController.popBackStack()
                }
            )
            
            // Trigger refresh and auto-select if needed
            LaunchedEffect(shouldRefreshCurrencies) {
                if (shouldRefreshCurrencies) {
                    viewModel.handleAction(CreateAccountAction.RefreshCurrencies)
                    // Auto-select the currency after a short delay to ensure refresh is complete
                    selectedCurrencyId?.let { currencyId ->
                        kotlinx.coroutines.delay(100)
                        viewModel.handleAction(CreateAccountAction.SetCurrencyById(currencyId))
                    }
                }
            }
        }
        
        composable("currency_selection") {
            CurrencySelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCustomCurrency = {
                    navController.navigate("create_custom_currency")
                },
                onCurrencySelected = { currencyId ->
                    android.util.Log.d("Navigation", "Currency selected with ID: $currencyId")
                    // Refresh currencies and select the new currency
                    try {
                        val backStackEntry = navController.getBackStackEntry("create_account")
                        backStackEntry.savedStateHandle.set("refresh_currencies", true)
                        backStackEntry.savedStateHandle.set("selected_currency_id", currencyId)
                    } catch (e: Exception) {
                        android.util.Log.e("Navigation", "Could not find create_account back stack entry")
                    }
                    navController.popBackStack()
                }
            )
        }
        
        composable("create_custom_currency") {
            CreateCustomCurrencyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCurrencyCreated = { currencyId ->
                    android.util.Log.d("Navigation", "Custom currency created with ID: $currencyId")
                    // Refresh currencies in the account creation screen
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_currencies", true)
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
                    navController.navigate("currency_selection")
                },
                onAccountCreated = { updatedAccountId ->
                    android.util.Log.d("Navigation", "Account $accountId updated with ID: $updatedAccountId")
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "transaction_form?accountId={accountId}&transactionId={transactionId}",
            arguments = listOf(
                navArgument("accountId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("transactionId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getLong("accountId") ?: -1L
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: -1L
            
            ru.orangesoftware.financisto.feature.transaction.ui.TransactionFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransactionSaved = { savedTransactionId ->
                    android.util.Log.d("Navigation", "Transaction saved with ID: $savedTransactionId")
                    // Refresh account list after transaction is saved
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_accounts", true)
                    navController.popBackStack()
                },
                onNavigateToCreateCategory = {
                    navController.navigate("create_category")
                },
                onNavigateToCreatePayee = {
                    navController.navigate("create_payee")
                },
                onNavigateToCreateProject = {
                    navController.navigate("create_project")
                },
                onNavigateToEditSplit = { splitItem ->
                    // Navigate to split edit screen with split data
                    navController.currentBackStackEntry?.savedStateHandle?.set("split_item", splitItem)
                    navController.navigate("edit_split")
                },
                onSplitSaved = { splitItem ->
                    // Handle split saved - this would be called when returning from split edit
                    android.util.Log.d("Navigation", "Split saved: ${splitItem.categoryName} - ${splitItem.amount}")
                },
                navBackStackEntry = backStackEntry
            )
        }

        composable("create_category") {
            ru.orangesoftware.financisto.feature.reference.ui.CreateCategoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCategoryCreated = { categoryId ->
                    android.util.Log.d("Navigation", "Category created with ID: $categoryId")
                    // Refresh categories in the transaction form
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_entity_type", "CATEGORY")
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_entity_id", categoryId)
                    navController.popBackStack()
                }
            )
        }

        composable("create_payee") {
            ru.orangesoftware.financisto.feature.reference.ui.CreatePayeeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPayeeCreated = { payeeId ->
                    android.util.Log.d("Navigation", "Payee created with ID: $payeeId")
                    // Refresh payees in the transaction form
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_entity_type", "PAYEE")
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_entity_id", payeeId)
                    navController.popBackStack()
                }
            )
        }

        composable("create_project") {
            ru.orangesoftware.financisto.feature.reference.ui.CreateProjectScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProjectCreated = { projectId ->
                    android.util.Log.d("Navigation", "Project created with ID: $projectId")
                    // Refresh projects in the transaction form
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_entity_type", "PROJECT")
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_entity_id", projectId)
                    navController.popBackStack()
                }
            )
        }

        composable("edit_split") {
            val splitItem = navController.previousBackStackEntry?.savedStateHandle?.get<ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem>("split_item")
                ?: ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem(id = -1L)
            
            ru.orangesoftware.financisto.feature.transaction.ui.SplitEditScreen(
                splitItem = splitItem,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSplitSaved = { savedSplit ->
                    android.util.Log.d("Navigation", "Split saved: ${savedSplit.categoryName} - ${savedSplit.amount}")
                    // Pass the saved split back to the transaction form
                    navController.previousBackStackEntry?.savedStateHandle?.set("saved_split", savedSplit)
                    navController.popBackStack()
                }
            )
        }
    }
}
