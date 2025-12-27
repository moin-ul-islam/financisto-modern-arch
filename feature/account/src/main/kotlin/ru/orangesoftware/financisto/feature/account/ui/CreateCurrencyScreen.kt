package ru.orangesoftware.financisto.feature.account.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.orangesoftware.financisto.feature.account.*

/**
 * Main Create Currency Screen composable.
 */
@Composable
fun CreateCurrencyScreen(
    viewModel: CreateCurrencyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onCurrencyCreated: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle save state changes
    LaunchedEffect(uiState.saveState) {
        when (val saveState = uiState.saveState) {
            is CurrencySaveState.Success -> {
                onCurrencyCreated(saveState.currencyId)
            }
            else -> { /* Do nothing */ }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        CreateCurrencyContent(
            uiState = uiState,
            onAction = viewModel::handleAction,
            onNavigateBack = onNavigateBack
        )
    }
}

/**
 * Main content for the Create Currency screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCurrencyContent(
    uiState: CreateCurrencyUiState,
    onAction: (CreateCurrencyAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Create Currency") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        // Form Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name (required)
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { value ->
                    onAction(CreateCurrencyAction.SetName(value))
                },
                label = { Text("Code *") },
                isError = uiState.validationErrors.contains(CurrencyValidationError.NameRequired),
                singleLine = true,
                supportingText = {
                    if (uiState.validationErrors.contains(CurrencyValidationError.NameRequired)) {
                        Text("Currency code is required")
                    } else {
                        Text("e.g., USD, EUR, GBP")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Title (required)
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { value ->
                    onAction(CreateCurrencyAction.SetTitle(value))
                },
                label = { Text("Name *") },
                isError = uiState.validationErrors.contains(CurrencyValidationError.TitleRequired),
                singleLine = true,
                supportingText = {
                    if (uiState.validationErrors.contains(CurrencyValidationError.TitleRequired)) {
                        Text("Currency name is required")
                    } else {
                        Text("e.g., US Dollar, Euro, British Pound")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Symbol (required)
            OutlinedTextField(
                value = uiState.symbol,
                onValueChange = { value ->
                    onAction(CreateCurrencyAction.SetSymbol(value))
                },
                label = { Text("Symbol *") },
                isError = uiState.validationErrors.contains(CurrencyValidationError.SymbolRequired),
                singleLine = true,
                supportingText = {
                    if (uiState.validationErrors.contains(CurrencyValidationError.SymbolRequired)) {
                        Text("Currency symbol is required")
                    } else {
                        Text("e.g., $, €, £")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Decimals
            OutlinedTextField(
                value = uiState.decimals,
                onValueChange = { value ->
                    onAction(CreateCurrencyAction.SetDecimals(value))
                },
                label = { Text("Decimal Places") },
                isError = uiState.validationErrors.contains(CurrencyValidationError.InvalidDecimals),
                singleLine = true,
                supportingText = {
                    if (uiState.validationErrors.contains(CurrencyValidationError.InvalidDecimals)) {
                        Text("Must be a number between 0 and 8")
                    } else {
                        Text("Number of decimal places (default: 2)")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Decimal Separator
            OutlinedTextField(
                value = uiState.decimalSeparator,
                onValueChange = { value ->
                    onAction(CreateCurrencyAction.SetDecimalSeparator(value))
                },
                label = { Text("Decimal Separator") },
                singleLine = true,
                supportingText = {
                    Text("Separator for decimal places (default: .)")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Group Separator
            OutlinedTextField(
                value = uiState.groupSeparator,
                onValueChange = { value ->
                    onAction(CreateCurrencyAction.SetGroupSeparator(value))
                },
                label = { Text("Group Separator") },
                singleLine = true,
                supportingText = {
                    Text("Separator for thousands (default: ,)")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Symbol Format
            OutlinedTextField(
                value = uiState.symbolFormat,
                onValueChange = { value ->
                    onAction(CreateCurrencyAction.SetSymbolFormat(value))
                },
                label = { Text("Symbol Format") },
                singleLine = true,
                supportingText = {
                    Text("RS = Right Symbol, LS = Left Symbol")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Is Default Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.isDefault,
                    onCheckedChange = { checked ->
                        onAction(CreateCurrencyAction.SetIsDefault(checked))
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set as default currency",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Bottom Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onAction(CreateCurrencyAction.SaveCurrency)
                },
                enabled = uiState.isFormValid && uiState.saveState !is CurrencySaveState.Saving,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.saveState is CurrencySaveState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        }

        // Save Error Dialog
        if (uiState.saveState is CurrencySaveState.Error) {
            AlertDialog(
                onDismissRequest = {
                    onAction(CreateCurrencyAction.DismissSaveError)
                },
                title = { Text("Error") },
                text = { Text(uiState.saveState.message) },
                confirmButton = {
                    TextButton(onClick = {
                        onAction(CreateCurrencyAction.DismissSaveError)
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}