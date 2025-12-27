package ru.orangesoftware.financisto.feature.account.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.orangesoftware.financisto.feature.account.*

/**
 * Screen for selecting a currency from common currencies or adding a custom one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionScreen(
    viewModel: CreateCurrencyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToCustomCurrency: () -> Unit = {},
    onCurrencySelected: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = { Text("Select Currency") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

            // Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                item {
                    Text(
                        text = "Choose from common currencies",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Common currencies
                items(getCommonCurrencies()) { currency ->
                    CurrencyListItem(
                        currency = currency,
                        onClick = {
                            // Create the currency and return its ID
                            viewModel.handleAction(CreateCurrencyAction.SetName(currency.code))
                            viewModel.handleAction(CreateCurrencyAction.SetTitle(currency.name))
                            viewModel.handleAction(CreateCurrencyAction.SetSymbol(currency.symbol))
                            viewModel.handleAction(CreateCurrencyAction.SetDecimals(currency.decimals.toString()))
                            viewModel.handleAction(CreateCurrencyAction.SetDecimalSeparator(currency.decimalSeparator))
                            viewModel.handleAction(CreateCurrencyAction.SetGroupSeparator(currency.groupSeparator))
                            viewModel.handleAction(CreateCurrencyAction.SetSymbolFormat(currency.symbolFormat))
                            viewModel.handleAction(CreateCurrencyAction.SaveCurrency)
                        }
                    )
                }

                // Add custom currency option
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToCustomCurrency),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add custom currency",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Custom Currency",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Handle save state changes for auto-created currencies
    LaunchedEffect(uiState.saveState) {
        when (val saveState = uiState.saveState) {
            is CurrencySaveState.Success -> {
                onCurrencySelected(saveState.currencyId)
            }
            else -> { /* Do nothing */ }
        }
    }
}

/**
 * Individual currency item in the list.
 */
@Composable
private fun CurrencyListItem(
    currency: CommonCurrency,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${currency.name} (${currency.code})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = currency.symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = currency.symbol,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Data class for common currencies.
 */
data class CommonCurrency(
    val code: String,
    val name: String,
    val symbol: String,
    val decimals: Int = 2,
    val decimalSeparator: String = ".",
    val groupSeparator: String = ",",
    val symbolFormat: String = "RS"
)

/**
 * Returns a list of common currencies.
 */
private fun getCommonCurrencies(): List<CommonCurrency> {
    return listOf(
        CommonCurrency("USD", "US Dollar", "$"),
        CommonCurrency("EUR", "Euro", "€"),
        CommonCurrency("GBP", "British Pound", "£"),
        CommonCurrency("JPY", "Japanese Yen", "¥"),
        CommonCurrency("CAD", "Canadian Dollar", "C$"),
        CommonCurrency("AUD", "Australian Dollar", "A$"),
        CommonCurrency("CHF", "Swiss Franc", "CHF"),
        CommonCurrency("CNY", "Chinese Yuan", "¥"),
        CommonCurrency("SEK", "Swedish Krona", "kr"),
        CommonCurrency("NZD", "New Zealand Dollar", "NZ$"),
        CommonCurrency("MXN", "Mexican Peso", "$"),
        CommonCurrency("SGD", "Singapore Dollar", "S$"),
        CommonCurrency("HKD", "Hong Kong Dollar", "HK$"),
        CommonCurrency("NOK", "Norwegian Krone", "kr"),
        CommonCurrency("KRW", "South Korean Won", "₩"),
        CommonCurrency("TRY", "Turkish Lira", "₺"),
        CommonCurrency("RUB", "Russian Ruble", "₽"),
        CommonCurrency("INR", "Indian Rupee", "₹"),
        CommonCurrency("BRL", "Brazilian Real", "R$"),
        CommonCurrency("ZAR", "South African Rand", "R")
    )
}