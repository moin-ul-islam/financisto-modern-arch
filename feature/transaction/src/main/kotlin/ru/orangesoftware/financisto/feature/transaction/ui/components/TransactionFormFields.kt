package ru.orangesoftware.financisto.feature.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Status and Date/Time selection row
 */
@Composable
fun StatusDateTimeRow(
    status: String,
    dateTime: Long,
    formattedDateTime: String,
    onStatusClick: () -> Unit,
    onDateTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status button
        StatusButton(
            status = status,
            onClick = onStatusClick
        )
        
        // Date button
        DateTimeButton(
            formattedDateTime = formattedDateTime,
            onClick = onDateTimeClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusButton(
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (status) {
        "CL" -> Icons.Default.CheckCircle to Color(0xFF4CAF50) // Cleared - Green
        "RC" -> Icons.Default.CheckCircle to Color(0xFF2196F3) // Reconciled - Blue
        "PN" -> Icons.Default.Warning to Color(0xFFFF9800) // Pending - Orange
        else -> Icons.Default.Info to Color.Gray // Unreconciled - Gray
    }
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Transaction Status",
            tint = color
        )
    }
}

@Composable
private fun DateTimeButton(
    formattedDateTime: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    val displayText = if (formattedDateTime.isNotEmpty()) {
        formattedDateTime
    } else {
        dateFormat.format(Date())
    }
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = displayText,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Account selection field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectionField(
    selectedAccount: ru.orangesoftware.financisto.feature.transaction.AccountOption?,
    availableAccounts: List<ru.orangesoftware.financisto.feature.transaction.AccountOption>,
    onAccountSelected: (ru.orangesoftware.financisto.feature.transaction.AccountOption) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Account"
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedAccount?.title ?: "",
            shape = RoundedCornerShape(size = 24.dp),
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = null
                )
            }
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (account in availableAccounts) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = account.title,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${account.currencySymbol} ${account.balance}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}