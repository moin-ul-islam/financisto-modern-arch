package ru.orangesoftware.financisto.feature.transaction.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import ru.orangesoftware.financisto.feature.transaction.TransactionFormUiState
import ru.orangesoftware.financisto.feature.transaction.TransactionFormViewModel
import ru.orangesoftware.financisto.feature.transaction.ui.components.*
import ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem
import androidx.navigation.NavBackStackEntry
import ru.orangesoftware.financisto.core.ui.theme.*

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
    onNavigateToCreateCategory: () -> Unit = {},
    onNavigateToCreatePayee: () -> Unit = {},
    onNavigateToCreateProject: () -> Unit = {},
    onNavigateToEditSplit: (SplitTransactionItem) -> Unit = { _ -> },
    onSplitSaved: (SplitTransactionItem) -> Unit = { _ -> },
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel(),
    navBackStackEntry: NavBackStackEntry? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle save result
    LaunchedEffect(uiState.saveState) {
        when (val saveState = uiState.saveState) {
            is ru.orangesoftware.financisto.feature.transaction.SaveState.Success -> {
                onTransactionSaved(saveState.transactionId)
            }

            else -> {}
        }
    }

    // Handle refresh after creating entities
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.let { backStackEntry ->
            val refreshEntityType =
                backStackEntry.savedStateHandle.get<String>("refresh_entity_type")
            val refreshEntityId = backStackEntry.savedStateHandle.get<Long>("refresh_entity_id")

            if (refreshEntityType != null) {
                val entityType = when (refreshEntityType) {
                    "CATEGORY" -> TransactionFormViewModel.CreatedEntityType.CATEGORY
                    "PAYEE" -> TransactionFormViewModel.CreatedEntityType.PAYEE
                    "PROJECT" -> TransactionFormViewModel.CreatedEntityType.PROJECT
                    else -> null
                }

                entityType?.let {
                    viewModel.refreshDataAfterCreation(it, refreshEntityId)
                }

                // Clear the flags
                backStackEntry.savedStateHandle.set("refresh_entity_type", null)
                backStackEntry.savedStateHandle.set("refresh_entity_id", null)
            }

            // Handle saved split from split edit screen
            val savedSplit =
                backStackEntry.savedStateHandle.get<ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem>(
                    "saved_split"
                )
            savedSplit?.let {
                viewModel.saveSplit(it)
                // Clear the saved split
                backStackEntry.savedStateHandle.set("saved_split", null)
            }
        }
    }

    // Set navigation callbacks on ViewModel
    LaunchedEffect(
        viewModel,
        onNavigateToCreateCategory,
        onNavigateToCreatePayee,
        onNavigateToCreateProject,
        onNavigateToEditSplit,
        onSplitSaved
    ) {
        android.util.Log.d("TransactionFormScreen", "Setting navigation callbacks on ViewModel")
        viewModel.onNavigateToCreateCategory = onNavigateToCreateCategory
        viewModel.onNavigateToCreatePayee = onNavigateToCreatePayee
        viewModel.onNavigateToCreateProject = onNavigateToCreateProject
        viewModel.onNavigateToEditSplit = onNavigateToEditSplit
        viewModel.onSplitSaved = { split ->
            viewModel.saveSplit(split)
            onSplitSaved(split)
        }
        android.util.Log.d(
            "TransactionFormScreen",
            "Navigation callbacks set: category=${onNavigateToCreateCategory != null}, payee=${onNavigateToCreatePayee != null}, project=${onNavigateToCreateProject != null}"
        )
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
    Box(modifier = modifier) {
        val scrollState = rememberScrollState()

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            // Beautiful gradient amount header (not scrollable)
            AmountHeaderCard(
                amount = uiState.amount,
                currencySymbol = uiState.selectedAccount?.currencySymbol ?: "$",
                isIncome = uiState.isIncome,
                onAmountChanged = { newAmount ->
                    onAction(TransactionFormAction.UpdateAmount(newAmount))
                },
                onToggleIncomeExpense = {
                    onAction(TransactionFormAction.ToggleIncomeExpense)
                },
                isTransfer = uiState.isTransfer
            )

            // Form fields card that overlaps with header slightly
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-16).dp), // Overlap with the header
                shape = RoundedCornerShape(
                    topStart = CardDimensions.RadiusLarge,
                    topEnd = CardDimensions.RadiusLarge,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = CardDimensions.ElevationMedium),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = CardDimensions.PaddingLarge,
                        end = CardDimensions.PaddingLarge,
                        top = CardDimensions.PaddingLarge + 16.dp, // Extra top padding for overlap
                        bottom = CardDimensions.PaddingLarge
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
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

                    // Category Selection (not for transfers or split transactions)
                    if (!uiState.isTransfer && !uiState.isSplitTransaction) {
                        CategorySelectionField(
                            selectedCategory = uiState.selectedCategory,
                            availableCategories = uiState.availableCategories, // Include split category for selection
                            onCategorySelected = { category ->
                                onAction(TransactionFormAction.SelectCategory(category))
                            },
                            onAddNewCategory = {
                                onAction(TransactionFormAction.AddNewCategory)
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
                            },
                            onAddNewPayee = {
                                onAction(TransactionFormAction.AddNewPayee)
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
                            },
                            onAddNewProject = {
                                onAction(TransactionFormAction.AddNewProject)
                            }
                        )
                    }

                    // Note Input
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = { note: String ->
                            onAction(TransactionFormAction.UpdateNote(note))
                        },
                        label = { Text("Note") },
                        placeholder = { Text("Add a note...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Split Transaction Section (if applicable)
                    if (uiState.isSplitTransaction) {
                        SplitTransactionSection(
                            splitTransactions = uiState.splitTransactions,
                            remainingAmount = uiState.remainingAmount,
                            currencySymbol = uiState.selectedAccount?.currencySymbol ?: "",
                            onAddSplit = { onAction(TransactionFormAction.AddSplit) },
                            onEditSplit = { split -> onAction(TransactionFormAction.EditSplit(split)) },
                            onDeleteSplit = { split ->
                                onAction(
                                    TransactionFormAction.DeleteSplit(
                                        split
                                    )
                                )
                            }
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
        }
    }
}

/**
 * Actions that can be performed on the transaction form
 */
sealed class TransactionFormAction {
    object ShowStatusPicker : TransactionFormAction()
    object ShowDateTimePicker : TransactionFormAction()
    data class SelectAccount(val account: ru.orangesoftware.financisto.feature.transaction.AccountOption) :
        TransactionFormAction()

    data class SelectToAccount(val account: ru.orangesoftware.financisto.feature.transaction.AccountOption) :
        TransactionFormAction()

    data class ToggleTransfer(val isTransfer: Boolean) : TransactionFormAction()
    data class UpdateAmount(val amount: String) : TransactionFormAction()
    object ToggleIncomeExpense : TransactionFormAction()
    data class UpdateExchangeRate(val rate: String) : TransactionFormAction()
    data class SelectCategory(val category: ru.orangesoftware.financisto.feature.transaction.CategoryOption) :
        TransactionFormAction()

    data class SelectPayee(val payee: ru.orangesoftware.financisto.feature.transaction.PayeeOption) :
        TransactionFormAction()

    data class SelectProject(val project: ru.orangesoftware.financisto.feature.transaction.ProjectOption) :
        TransactionFormAction()

    object AddNewCategory : TransactionFormAction()
    object AddNewPayee : TransactionFormAction()
    object AddNewProject : TransactionFormAction()
    data class UpdateNote(val note: String) : TransactionFormAction()
    object AddSplit : TransactionFormAction()
    data class EditSplit(val split: SplitTransactionItem) : TransactionFormAction()
    data class DeleteSplit(val split: SplitTransactionItem) : TransactionFormAction()
}