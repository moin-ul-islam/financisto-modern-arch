package ru.orangesoftware.financisto.feature.account.ui.components.createaccount

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ActionButtons(
    isFormValid: Boolean,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
    ) {
        OutlinedButton(
            onClick = onCancelClick,
            enabled = !isSaving,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }
        
        Button(
            onClick = onSaveClick,
            enabled = isFormValid && !isSaving,
            modifier = Modifier.weight(1f)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save")
            }
        }
    }
}

@Composable
fun SaveErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Error Saving Account")
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionButtons(
                isFormValid = true,
                isSaving = false,
                onSaveClick = {},
                onCancelClick = {}
            )
            
            ActionButtons(
                isFormValid = false,
                isSaving = false,
                onSaveClick = {},
                onCancelClick = {}
            )
            
            ActionButtons(
                isFormValid = true,
                isSaving = true,
                onSaveClick = {},
                onCancelClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SaveErrorDialogPreview() {
    MaterialTheme {
        SaveErrorDialog(
            message = "Account title is required",
            onDismiss = {}
        )
    }
}
