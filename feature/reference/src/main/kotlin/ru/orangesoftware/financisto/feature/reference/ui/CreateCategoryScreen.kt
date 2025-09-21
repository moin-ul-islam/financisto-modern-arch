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
import ru.orangesoftware.financisto.feature.reference.CreateCategoryAction
import ru.orangesoftware.financisto.feature.reference.CreateCategoryViewModel

/**
 * Screen for creating a new category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryScreen(
    onNavigateBack: () -> Unit,
    onCategoryCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Set navigation callback
    LaunchedEffect(viewModel, onCategoryCreated) {
        viewModel.onCategoryCreated = onCategoryCreated
    }

    // Handle success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCategoryCreated(0) // The actual ID will be passed through the callback
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleAction(CreateCategoryAction.CreateCategory) },
                        enabled = !uiState.isLoading && uiState.categoryName.isNotBlank()
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
                value = uiState.categoryName,
                onValueChange = { name ->
                    viewModel.handleAction(CreateCategoryAction.UpdateName(name))
                },
                label = { Text("Category Name") },
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