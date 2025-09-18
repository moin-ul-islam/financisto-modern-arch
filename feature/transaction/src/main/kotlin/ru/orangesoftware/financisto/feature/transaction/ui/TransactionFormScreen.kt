package ru.orangesoftware.financisto.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import ru.orangesoftware.financisto.feature.transaction.TransactionFormUiState
import ru.orangesoftware.financisto.feature.transaction.TransactionFormViewModel
import ru.orangesoftware.financisto.feature.transaction.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern Compose UI for creating/editing transactions.
 * 
 * This screen replicates the functionality of the legacy TransactionActivity
 * with modern Material Design 3 components and improved UX.
 * 
 * Key features:
 * - Account selection with balance display
 * - Amount input with currency formatting
 * - Category selection with icons
 * - Payee, project, and location selection
 * - Date/time picker
 * - Note input
 * - Transfer functionality
 * - Split transaction support
 * - Form validation and error handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    onNavigateBack: () -> Unit,
    onTransactionSaved: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Handle save result
    LaunchedEffect(uiState.saveState) {
        when (val saveState = uiState.saveState) {
            is ru.orangesoftware.financisto.feature.transaction.SaveState.Success -> {
                onTransactionSaved(saveState.transactionId)
            }
            else -> {}
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (uiState.isEditMode) "Edit Transaction" else "New Transaction",
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Save button
                TextButton(
                    onClick = { viewModel.saveTransaction() },
                    enabled = uiState.isFormValid && !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("SAVE")
                    }
                }
            }
        )
        
        // Form content
        TransactionFormContent(
            uiState = uiState,
            onAction = viewModel::handleAction,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun TransactionFormContent(
    uiState: TransactionFormUiState,
    onAction: (TransactionFormAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status and Date/Time Row
        StatusDateTimeRow(
            status = uiState.status,
            dateTime = uiState.dateTime,
            formattedDateTime = uiState.formattedDateTime,
            onStatusClick = { onAction(TransactionFormAction.ShowStatusPicker) },
            onDateTimeClick = { onAction(TransactionFormAction.ShowDateTimePicker) }
        )
        
        // Account Selection
        AccountSelectionField(
            selectedAccount = uiState.selectedAccount,
            availableAccounts = uiState.availableAccounts,
            onAccountSelected = { account ->
                onAction(TransactionFormAction.SelectAccount(account))
            }
        )
        
        // Transfer Toggle (if enabled)
        if (uiState.isTransferEnabled) {
            TransferToggle(
                isTransfer = uiState.isTransfer,
                onToggle = { onAction(TransactionFormAction.ToggleTransfer(it)) }
            )
        }
        
        // To Account (for transfers)
        if (uiState.isTransfer) {
            AccountSelectionField(
                label = "To Account",
                selectedAccount = uiState.selectedToAccount,
                availableAccounts = uiState.availableAccounts.filter { 
                    it.id != uiState.selectedAccount?.id 
                },
                onAccountSelected = { account ->
                    onAction(TransactionFormAction.SelectToAccount(account))
                }
            )
        }
        
        // Amount Input
        AmountInputField(
            amount = uiState.amount,
            formattedAmount = uiState.formattedAmount,
            currencySymbol = uiState.selectedAccount?.currencySymbol ?: "",
            isIncome = uiState.isIncome,
            onAmountChanged = { amount ->
                onAction(TransactionFormAction.UpdateAmount(amount))
            },
            onIncomeExpenseToggle = {
                onAction(TransactionFormAction.ToggleIncomeExpense)
            }
        )
        
        // Exchange Rate (for multi-currency transfers)
        if (uiState.isTransfer && uiState.isDifferentCurrency) {
            ExchangeRateField(
                exchangeRate = uiState.exchangeRate,
                fromCurrency = uiState.selectedAccount?.currencySymbol ?: "",
                toCurrency = uiState.selectedToAccount?.currencySymbol ?: "",
                toAmount = uiState.toAmount,
                onExchangeRateChanged = { rate ->
                    onAction(TransactionFormAction.UpdateExchangeRate(rate))
                }
            )
        }
        
        // Category Selection (not for transfers)
        if (!uiState.isTransfer) {
            CategorySelectionField(
                selectedCategory = uiState.selectedCategory,
                availableCategories = uiState.availableCategories,
                onCategorySelected = { category ->
                    onAction(TransactionFormAction.SelectCategory(category))
                }
            )
        }
        
        // Payee Selection
        if (uiState.isShowPayee) {
            PayeeSelectionField(
                selectedPayee = uiState.selectedPayee,
                availablePayees = uiState.availablePayees,
                onPayeeSelected = { payee ->
                    onAction(TransactionFormAction.SelectPayee(payee))
                }
            )
        }
        
        // Project Selection
        if (uiState.isShowProject) {
            ProjectSelectionField(
                selectedProject = uiState.selectedProject,
                availableProjects = uiState.availableProjects,
                onProjectSelected = { project ->
                    onAction(TransactionFormAction.SelectProject(project))
                }
            )
        }
        
        // Location Selection
        if (uiState.isShowLocation) {
            LocationSelectionField(
                selectedLocation = uiState.selectedLocation,
                availableLocations = uiState.availableLocations,
                onLocationSelected = { location ->
                    onAction(TransactionFormAction.SelectLocation(location))
                }
            )
        }
        
        // Note Input
        NoteInputField(
            note = uiState.note,
            onNoteChanged = { note ->
                onAction(TransactionFormAction.UpdateNote(note))
            }
        )
        
        // Split Transaction Section (if applicable)
        if (uiState.isSplitTransaction) {
            SplitTransactionSection(
                splitTransactions = uiState.splitTransactions,
                remainingAmount = uiState.remainingAmount,
                currencySymbol = uiState.selectedAccount?.currencySymbol ?: "",
                onAddSplit = { onAction(TransactionFormAction.AddSplit) },
                onEditSplit = { split -> onAction(TransactionFormAction.EditSplit(split)) },
                onDeleteSplit = { split -> onAction(TransactionFormAction.DeleteSplit(split)) }
            )
        }
        
        // Validation Errors
        if (uiState.validationErrors.isNotEmpty()) {
            ValidationErrorCard(
                errors = uiState.validationErrors
            )
        }
        
        // Bottom spacer for FAB
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * Actions that can be performed on the transaction form
 */
sealed class TransactionFormAction {
    object ShowStatusPicker : TransactionFormAction()
    object ShowDateTimePicker : TransactionFormAction()
    data class SelectAccount(val account: ru.orangesoftware.financisto.feature.transaction.AccountOption) : TransactionFormAction()
    data class SelectToAccount(val account: ru.orangesoftware.financisto.feature.transaction.AccountOption) : TransactionFormAction()
    data class ToggleTransfer(val isTransfer: Boolean) : TransactionFormAction()
    data class UpdateAmount(val amount: String) : TransactionFormAction()
    object ToggleIncomeExpense : TransactionFormAction()
    data class UpdateExchangeRate(val rate: String) : TransactionFormAction()
    data class SelectCategory(val category: ru.orangesoftware.financisto.feature.transaction.CategoryOption) : TransactionFormAction()
    data class SelectPayee(val payee: ru.orangesoftware.financisto.feature.transaction.PayeeOption) : TransactionFormAction()
    data class SelectProject(val project: ru.orangesoftware.financisto.feature.transaction.ProjectOption) : TransactionFormAction()
    data class SelectLocation(val location: ru.orangesoftware.financisto.feature.transaction.LocationOption) : TransactionFormAction()
    data class UpdateNote(val note: String) : TransactionFormAction()
    object AddSplit : TransactionFormAction()
    data class EditSplit(val split: ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem) : TransactionFormAction()
    data class DeleteSplit(val split: ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem) : TransactionFormAction()
}