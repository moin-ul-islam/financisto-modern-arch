package ru.orangesoftware.financisto.feature.account.ui.components.createaccount

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuerNameField(
    value: String,
    onValueChange: (String) -> Unit,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Issuer Name") },
        isError = hasError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Card Number") },
        isError = hasError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosingDayField(
    value: String,
    onValueChange: (String) -> Unit,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numbers and limit to 2 digits
            if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                onValueChange(newValue)
            }
        },
        label = { Text("Closing Day") },
        isError = hasError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        supportingText = {
            Text("Day of month (1-31)")
        },
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDayField(
    value: String,
    onValueChange: (String) -> Unit,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numbers and limit to 2 digits
            if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                onValueChange(newValue)
            }
        },
        label = { Text("Payment Day") },
        isError = hasError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        supportingText = {
            Text("Day of month (1-31)")
        },
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleField(
    value: String,
    onValueChange: (String) -> Unit,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Account Title *") },
        isError = hasError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String = "",
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Allow numbers, decimal point, and negative sign
            if (newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                onValueChange(newValue)
            }
        },
        label = { Text("Credit Limit${if (currencySymbol.isNotEmpty()) " ($currencySymbol)" else ""}") },
        isError = hasError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        supportingText = {
            Text("Leave empty for no limit")
        },
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String = "",
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Allow numbers, decimal point, and negative sign
            if (newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                onValueChange(newValue)
            }
        },
        label = { Text("Opening Balance${if (currencySymbol.isNotEmpty()) " ($currencySymbol)" else ""}") },
        isError = hasError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Note") },
        maxLines = 5,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOrderField(
    value: String,
    onValueChange: (String) -> Unit,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numbers
            if (newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        label = { Text("Sort Order") },
        isError = hasError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        supportingText = {
            Text("Higher numbers appear first")
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun IncludeInTotalsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Include in totals and budgets",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FormFieldsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TitleField(
                value = "Sample Account",
                hasError = false,
                onValueChange = {}
            )
            
            LimitAmountField(
                value = "1000.00",
                hasError = false,
                onValueChange = {}
            )
            
            OpeningAmountField(
                value = "500.00",
                hasError = false,
                onValueChange = {}
            )
            
            IncludeInTotalsCheckbox(
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}
