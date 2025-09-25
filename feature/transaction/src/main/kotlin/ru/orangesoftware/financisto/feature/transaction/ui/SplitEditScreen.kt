package ru.orangesoftware.financisto.feature.transaction.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem
import ru.orangesoftware.financisto.feature.transaction.TransactionFormViewModel
import ru.orangesoftware.financisto.feature.transaction.ui.components.*

/**
 * Screen for editing split transaction details.
 * Similar to the legacy SplitTransactionActivity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitEditScreen(
    splitItem: SplitTransactionItem,
    onNavigateBack: () -> Unit,
    onSplitSaved: (SplitTransactionItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Local state for editing
    var selectedCategory by remember { mutableStateOf<ru.orangesoftware.financisto.feature.transaction.CategoryOption?>(null) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf<ru.orangesoftware.financisto.feature.transaction.ProjectOption?>(null) }
    var isIncome by remember { mutableStateOf(false) }

    // Initialize with existing split data if editing
    LaunchedEffect(splitItem) {
        if (splitItem.id != -1L) { // Not a new split
            selectedCategory = uiState.availableCategories.find { it.id == splitItem.categoryId }
            // Convert from cents to display format
            amount = String.format("%.2f", splitItem.amount / 100.0)
            note = splitItem.note ?: ""
            selectedProject = uiState.availableProjects.find { it.id == splitItem.projectId }
            isIncome = splitItem.type == 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (splitItem.id == -1L) "Add Split" else "Edit Split") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val amountInCents = Math.round((amount.toDoubleOrNull() ?: 0.0) * 100)
                            val splitId = if (splitItem.id == -1L) -System.currentTimeMillis() else splitItem.id
                            
                            val updatedSplit = splitItem.copy(
                                id = splitId,
                                categoryId = selectedCategory?.id ?: 0,
                                categoryName = selectedCategory?.title,
                                amount = amountInCents.toLong(),
                                formattedAmount = amount,
                                note = note.takeIf { it.isNotBlank() },
                                projectId = selectedProject?.id,
                                projectName = selectedProject?.name,
                                type = if (isIncome) 1 else 0
                            )
                            onSplitSaved(updatedSplit)
                        },
                        enabled = selectedCategory != null && amount.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Selection
            CategorySelectionField(
                selectedCategory = selectedCategory,
                availableCategories = uiState.availableCategories.filter { it.id != -1L }, // Exclude split category
                onCategorySelected = { category ->
                    selectedCategory = category
                },
                onAddNewCategory = {
                    // TODO: Navigate to create category
                }
            )

            // Amount Input with Income/Expense Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income/Expense Toggle
                IncomeExpenseToggle(
                    isIncome = isIncome,
                    onToggle = { isIncome = !isIncome }
                )
                
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    prefix = { Text(uiState.selectedAccount?.currencySymbol ?: "") }
                )
            }

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Project Selection
            ProjectSelectionField(
                selectedProject = selectedProject,
                availableProjects = uiState.availableProjects,
                onProjectSelected = { project ->
                    selectedProject = project
                },
                onAddNewProject = {
                    // TODO: Navigate to create project
                }
            )
        }
    }
}