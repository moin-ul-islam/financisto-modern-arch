package ru.orangesoftware.financisto.feature.reference.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.orangesoftware.financisto.feature.reference.CreatePayeeAction
import ru.orangesoftware.financisto.feature.reference.CreatePayeeViewModel

/**
 * Screen for creating a new payee.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePayeeScreen(
    onNavigateBack: () -> Unit,
    onPayeeCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatePayeeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Set navigation callback
    LaunchedEffect(viewModel, onPayeeCreated) {
        viewModel.onPayeeCreated = onPayeeCreated
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Payee") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleAction(CreatePayeeAction.CreatePayee) },
                        enabled = !uiState.isLoading && uiState.payeeName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Create")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.payeeName,
                onValueChange = { name ->
                    viewModel.handleAction(CreatePayeeAction.UpdateName(name))
                },
                label = { Text("Payee Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = true,
                isError = uiState.errorMessage != null
            )

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}