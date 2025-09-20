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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.orangesoftware.financisto.feature.account.CreateAccountAction
import ru.orangesoftware.financisto.feature.account.CreateAccountScreenState
import ru.orangesoftware.financisto.feature.account.CreateAccountViewModel
import ru.orangesoftware.financisto.feature.account.SaveState
import ru.orangesoftware.financisto.feature.account.ui.components.LoadingIndicator
import ru.orangesoftware.financisto.feature.account.ui.components.createaccount.*

/**
 * Main Create Account Screen composable.
 * 
 * This composable maintains the same visual appearance and functionality as the legacy
 * AccountActivity while using modern Compose UI and clean architecture.
 * Supports both create and edit modes based on the accountId parameter in navigation.
 */
@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddCurrency: () -> Unit = {},
    onAccountCreated: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Check for refresh flag from navigation
    val shouldRefreshCurrencies = remember { mutableStateOf(false) }
    
    // Refresh currencies when returning from currency creation
    LaunchedEffect(shouldRefreshCurrencies.value) {
        if (shouldRefreshCurrencies.value) {
            viewModel.handleAction(CreateAccountAction.RefreshCurrencies)
            shouldRefreshCurrencies.value = false
        }
    }

    // Handle save state changes
    LaunchedEffect(uiState.saveState) {
        when (val saveState = uiState.saveState) {
            is SaveState.Success -> {
                onAccountCreated(saveState.accountId)
            }
            else -> { /* Do nothing */ }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val screenState = uiState.screenState) {
            is CreateAccountScreenState.Loading -> {
                LoadingIndicator()
            }
            
            is CreateAccountScreenState.Error -> {
                ErrorContent(
                    message = screenState.message,
                    canRetry = screenState.canRetry,
                    onRetryClick = {
                        viewModel.handleAction(CreateAccountAction.LoadInitialData)
                    },
                    onBackClick = onNavigateBack
                )
            }
            
            is CreateAccountScreenState.Content -> {
                CreateAccountContent(
                    uiState = uiState,
                    contentData = screenState.data,
                    onAction = viewModel::handleAction,
                    onNavigateBack = onNavigateBack,
                    onNavigateToAddCurrency = onNavigateToAddCurrency
                )
            }
        }
    }
}

/**
 * Main content for the Create Account screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAccountContent(
    uiState: ru.orangesoftware.financisto.feature.account.CreateAccountUiState,
    contentData: ru.orangesoftware.financisto.feature.account.CreateAccountContentData,
    onAction: (CreateAccountAction) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAddCurrency: () -> Unit
) {
    // Determine if we're in edit mode based on pre-filled data
    val isEditMode = uiState.title.isNotBlank() || uiState.selectedAccountType != null
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text(if (isEditMode) "Edit Account" else "Create Account") },
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
            // Account Type Selection
            AccountTypeSelector(
                selectedAccountType = uiState.selectedAccountType,
                availableAccountTypes = contentData.availableAccountTypes,
                onAccountTypeSelected = { accountType ->
                    onAction(CreateAccountAction.SetAccountType(accountType))
                }
            )

            // Card Issuer (conditional)
            if (uiState.selectedAccountType?.isCard == true) {
                CardIssuerSelector(
                    selectedCardIssuer = uiState.selectedCardIssuer,
                    availableCardIssuers = contentData.availableCardIssuers,
                    onCardIssuerSelected = { cardIssuer ->
                        onAction(CreateAccountAction.SetCardIssuer(cardIssuer))
                    }
                )
            }

            // Electronic Payment Type (conditional)
            if (uiState.selectedAccountType?.isElectronic == true) {
                ElectronicPaymentTypeSelector(
                    selectedPaymentType = uiState.selectedElectronicPaymentType,
                    availablePaymentTypes = contentData.availableElectronicPaymentTypes,
                    onPaymentTypeSelected = { paymentType ->
                        onAction(CreateAccountAction.SetElectronicPaymentType(paymentType))
                    }
                )
            }

            // Issuer Name (conditional)
            if (uiState.selectedAccountType?.hasIssuer == true) {
                IssuerNameField(
                    value = uiState.issuerName,
                    onValueChange = { value ->
                        onAction(CreateAccountAction.SetIssuerName(value))
                    }
                )
            }

            // Card Number (conditional)
            if (uiState.selectedAccountType?.hasNumber == true) {
                CardNumberField(
                    value = uiState.cardNumber,
                    onValueChange = { value ->
                        onAction(CreateAccountAction.SetCardNumber(value))
                    }
                )
            }

            // Closing Day (for credit cards)
            if (uiState.selectedAccountType?.isCreditCard == true) {
                ClosingDayField(
                    value = uiState.closingDay,
                    onValueChange = { value ->
                        onAction(CreateAccountAction.SetClosingDay(value))
                    },
                    hasError = uiState.validationErrors.contains(
                        ru.orangesoftware.financisto.feature.account.ValidationError.InvalidClosingDay
                    )
                )
            }

            // Payment Day (for credit cards)
            if (uiState.selectedAccountType?.isCreditCard == true) {
                PaymentDayField(
                    value = uiState.paymentDay,
                    onValueChange = { value ->
                        onAction(CreateAccountAction.SetPaymentDay(value))
                    },
                    hasError = uiState.validationErrors.contains(
                        ru.orangesoftware.financisto.feature.account.ValidationError.InvalidPaymentDay
                    )
                )
            }

            // Title (required)
            TitleField(
                value = uiState.title,
                onValueChange = { value ->
                    onAction(CreateAccountAction.SetTitle(value))
                },
                hasError = uiState.validationErrors.contains(
                    ru.orangesoftware.financisto.feature.account.ValidationError.TitleRequired
                )
            )

            // Currency Selection (required)
            CurrencySelector(
                selectedCurrency = uiState.selectedCurrency,
                availableCurrencies = contentData.availableCurrencies,
                onCurrencySelected = { currency ->
                    onAction(CreateAccountAction.SetCurrency(currency))
                },
                onAddCurrencyClick = onNavigateToAddCurrency,
                hasError = uiState.validationErrors.contains(
                    ru.orangesoftware.financisto.feature.account.ValidationError.CurrencyRequired
                )
            )

            // Limit Amount (for credit cards)
            if (uiState.selectedAccountType?.isCreditCard == true) {
                LimitAmountField(
                    value = uiState.limitAmount,
                    onValueChange = { value ->
                        onAction(CreateAccountAction.SetLimitAmount(value))
                    },
                    currencySymbol = uiState.selectedCurrency?.symbol ?: "",
                    hasError = uiState.validationErrors.contains(
                        ru.orangesoftware.financisto.feature.account.ValidationError.InvalidLimitAmount
                    )
                )
            }

            // Opening Amount
            OpeningAmountField(
                value = uiState.openingAmount,
                onValueChange = { value ->
                    onAction(CreateAccountAction.SetOpeningAmount(value))
                },
                currencySymbol = uiState.selectedCurrency?.symbol ?: "",
                hasError = uiState.validationErrors.contains(
                    ru.orangesoftware.financisto.feature.account.ValidationError.InvalidOpeningAmount
                )
            )

            // Note
            NoteField(
                value = uiState.note,
                onValueChange = { value ->
                    onAction(CreateAccountAction.SetNote(value))
                }
            )

            // Sort Order
            SortOrderField(
                value = uiState.sortOrder,
                onValueChange = { value ->
                    onAction(CreateAccountAction.SetSortOrder(value))
                },
                hasError = uiState.validationErrors.contains(
                    ru.orangesoftware.financisto.feature.account.ValidationError.InvalidSortOrder
                )
            )

            // Include in Totals Checkbox
            IncludeInTotalsCheckbox(
                checked = uiState.isIncludedInTotals,
                onCheckedChange = { checked ->
                    onAction(CreateAccountAction.SetIncludedInTotals(checked))
                }
            )
        }

        // Bottom Action Buttons
        ActionButtons(
            isFormValid = uiState.isFormValid,
            isSaving = uiState.saveState is SaveState.Saving,
            onSaveClick = {
                onAction(CreateAccountAction.SaveAccount)
            },
            onCancelClick = onNavigateBack,
            saveButtonText = if (isEditMode) "Update" else "Save"
        )

        // Save Error Dialog
        if (uiState.saveState is SaveState.Error) {
            SaveErrorDialog(
                message = uiState.saveState.message,
                onDismiss = {
                    onAction(CreateAccountAction.DismissSaveError)
                }
            )
        }
    }
}

/**
 * Error content when initial data loading fails.
 */
@Composable
private fun ErrorContent(
    message: String,
    canRetry: Boolean,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (canRetry) {
                Button(onClick = onRetryClick) {
                    Text("Retry")
                }
            }
            
            OutlinedButton(onClick = onBackClick) {
                Text("Back")
            }
        }
    }
}
