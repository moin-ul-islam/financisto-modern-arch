package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.BlotterDao
import ru.orangesoftware.financisto.data.model.BlotterView
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.CurrencyRepository
import ru.orangesoftware.financisto.utils.CurrencyFormatter
import ru.orangesoftware.financisto.domain.model.Currency
import ru.orangesoftware.financisto.domain.model.CurrencyId
import ru.orangesoftware.financisto.domain.model.SymbolFormat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for getting blotter transactions for a specific account using the BlotterView.
 * 
 * This is an alternative to GetBlotterForAccountUseCase that leverages the database view
 * instead of manually joining data from multiple tables and repositories.
 * 
 * Key differences from GetBlotterForAccountUseCase:
 * - Uses v_blotter database view which pre-joins all needed tables
 * - Data is already enriched in the view (account titles, category titles, etc.)
 * - Running balance is included in the view
 * - Handles both sides of transfers via UNION in the view
 * - More efficient as SQL joins happen at database level
 * 
 * The BlotterView UNION structure:
 * - First SELECT: Transactions from the "from_account" perspective
 * - Second SELECT: Transactions from the "to_account" perspective (for transfers)
 *   - Amounts are swapped (to_amount becomes from_amount)
 *   - is_transfer = -1 to mark it as the "receiving side"
 * 
 * This matches the legacy app's v_blotter_for_account_with_splits view.
 */
@Singleton
class GetBlotterViewForAccountUseCase @Inject constructor(
    private val blotterDao: BlotterDao,
    private val currencyRepository: CurrencyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Execute the use case to get blotter items for an account.
     * 
     * @param accountId The account ID to get transactions for
     * @return Result containing list of BlotterItem objects ordered by most recent first
     */
    suspend fun execute(
        accountId: Long
    ): Result<List<BlotterItem>> = withContext(ioDispatcher) {
        try {
            // Get blotter view items for the account
            // The view already filters for:
            // - is_template = 0
            // - Includes both sides of transfers (via UNION)
            // We need to additionally filter by account and parent_id condition
            val blotterViews = blotterDao.getBlotterForAccount(accountId)
            
            if (blotterViews.isEmpty()) {
                return@withContext Result.success(emptyList())
            }
            
            // Convert BlotterView objects to BlotterItem domain objects
            val blotterItems = blotterViews.map { view ->
                convertToBlotterItem(view)
            }
            
            Result.success(blotterItems)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convert a BlotterView database entity to a BlotterItem domain model.
     * 
     * This includes formatting amounts with currency symbols.
     */
    private suspend fun convertToBlotterItem(view: BlotterView): BlotterItem {
        // Get currency for formatting amounts
        val currency = currencyRepository.getCurrencyById(view.fromAccountCurrencyId)
        
        val formattedFromAmount = if (currency != null) {
            val currencyModel = Currency(
                id = CurrencyId(currency.id),
                name = currency.name,
                title = currency.title,
                symbol = currency.symbol,
                isDefault = currency.isDefault,
                decimals = currency.decimals,
                decimalSeparator = currency.decimalSeparator,
                groupSeparator = currency.groupSeparator,
                symbolFormat = SymbolFormat.valueOf(currency.symbolFormat),
                isActive = true
            )
            CurrencyFormatter.formatAmount(view.fromAmount, currencyModel)
        } else {
            "$${view.fromAmount / 100}.${String.format("%02d", view.fromAmount % 100)}"
        }
        
        val formattedRunningBalance = view.fromAccountBalance?.let { balance ->
            if (currency != null) {
                val currencyModel = Currency(
                    id = CurrencyId(currency.id),
                    name = currency.name,
                    title = currency.title,
                    symbol = currency.symbol,
                    isDefault = currency.isDefault,
                    decimals = currency.decimals,
                    decimalSeparator = currency.decimalSeparator,
                    groupSeparator = currency.groupSeparator,
                    symbolFormat = SymbolFormat.valueOf(currency.symbolFormat),
                    isActive = true
                )
                CurrencyFormatter.formatAmount(balance, currencyModel)
            } else {
                "$${balance / 100}.${String.format("%02d", balance % 100)}"
            }
        } ?: "$0.00"
        
        return BlotterItem(
            transactionId = view.id,
            datetime = view.datetime,
            fromAmount = view.fromAmount,
            toAmount = view.toAmount,
            fromAccountId = view.fromAccountId,
            fromAccountTitle = view.fromAccountTitle,
            fromAccountCurrencyId = view.fromAccountCurrencyId,
            toAccountId = view.toAccountId ?: 0L,
            toAccountTitle = view.toAccountTitle,
            toAccountCurrencyId = view.toAccountCurrencyId,
            categoryId = view.categoryId,
            categoryTitle = view.categoryTitle,
            payeeId = view.payeeId ?: 0L,
            payeeTitle = view.payee,
            note = view.note,
            runningBalance = view.fromAccountBalance ?: 0L,
            isTransfer = (view.isTransfer ?: 0L) != 0L, // is_transfer can be toAccountId or -1
            isIncomingTransfer = (view.isTransfer ?: 0L) == -1L, // True when viewing from receiving account
            isSplit = view.categoryId == -1L, // Split category has ID -1
            status = view.status,
            originalCurrencyId = view.originalCurrencyId ?: 0L,
            originalFromAmount = view.originalFromAmount ?: 0L,
            formattedFromAmount = formattedFromAmount,
            formattedRunningBalance = formattedRunningBalance
        )
    }
}
