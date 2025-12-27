package ru.orangesoftware.financisto.feature.account.ui.components.createaccount

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.orangesoftware.financisto.feature.account.AccountTypeOption
import ru.orangesoftware.financisto.feature.account.CardIssuerOption
import ru.orangesoftware.financisto.feature.account.ElectronicPaymentTypeOption
import ru.orangesoftware.financisto.feature.account.CurrencyOption

/**
 * Account Type Selector Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTypeSelector(
    selectedAccountType: AccountTypeOption?,
    availableAccountTypes: List<AccountTypeOption>,
    onAccountTypeSelected: (AccountTypeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Account Type",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedAccountType?.displayName ?: "Select Account Type",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                leadingIcon = {
                    selectedAccountType?.let { accountType ->
                        Icon(
                            painter = painterResource(accountType.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableAccountTypes.forEach { accountType ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(accountType.iconResId),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(accountType.displayName)
                            }
                        },
                        onClick = {
                            onAccountTypeSelected(accountType)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Card Issuer Selector Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardIssuerSelector(
    selectedCardIssuer: CardIssuerOption?,
    availableCardIssuers: List<CardIssuerOption>,
    onCardIssuerSelected: (CardIssuerOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Card Issuer",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCardIssuer?.displayName ?: "Select Card Issuer",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                leadingIcon = {
                    selectedCardIssuer?.let { issuer ->
                        Icon(
                            painter = painterResource(issuer.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableCardIssuers.forEach { issuer ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(issuer.iconResId),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(issuer.displayName)
                            }
                        },
                        onClick = {
                            onCardIssuerSelected(issuer)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Electronic Payment Type Selector Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectronicPaymentTypeSelector(
    selectedPaymentType: ElectronicPaymentTypeOption?,
    availablePaymentTypes: List<ElectronicPaymentTypeOption>,
    onPaymentTypeSelected: (ElectronicPaymentTypeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Electronic Payment Type",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedPaymentType?.displayName ?: "Select Payment Type",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                leadingIcon = {
                    selectedPaymentType?.let { paymentType ->
                        Icon(
                            painter = painterResource(paymentType.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availablePaymentTypes.forEach { paymentType ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(paymentType.iconResId),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(paymentType.displayName)
                            }
                        },
                        onClick = {
                            onPaymentTypeSelected(paymentType)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Currency Selector Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    selectedCurrency: CurrencyOption?,
    availableCurrencies: List<CurrencyOption>,
    onCurrencySelected: (CurrencyOption) -> Unit,
    onAddCurrencyClick: () -> Unit,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Currency *",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedCurrency?.let { "${it.name} (${it.symbol})" } ?: "Select Currency",
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    isError = hasError,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableCurrencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text("${currency.name} (${currency.symbol})") },
                            onClick = {
                                onCurrencySelected(currency)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedButton(
                onClick = onAddCurrencyClick,
                modifier = Modifier.height(56.dp)
            ) {
                Text("+")
            }
        }
        
        if (hasError) {
            Text(
                text = "Currency is required",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
