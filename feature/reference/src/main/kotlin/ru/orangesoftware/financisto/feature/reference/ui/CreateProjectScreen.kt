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
import ru.orangesoftware.financisto.feature.reference.CreateProjectAction
import ru.orangesoftware.financisto.feature.reference.CreateProjectViewModel

/**
 * Screen for creating a new project.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    onNavigateBack: () -> Unit,
    onProjectCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateProjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Set navigation callback
    LaunchedEffect(viewModel, onProjectCreated) {
        viewModel.onProjectCreated = onProjectCreated
    }

    // Handle success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onProjectCreated(0) // The actual ID will be passed through the callback
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleAction(CreateProjectAction.CreateProject) },
                        enabled = !uiState.isLoading && uiState.projectName.isNotBlank()
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
                value = uiState.projectName,
                onValueChange = { name ->
                    viewModel.handleAction(CreateProjectAction.UpdateName(name))
                },
                label = { Text("Project Name") },
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