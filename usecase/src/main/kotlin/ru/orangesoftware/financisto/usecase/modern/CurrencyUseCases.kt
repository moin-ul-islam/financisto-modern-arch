package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import javax.inject.Inject

/**
 * Use case to get the home (default) currency.
 * 
 * The home currency is the currency marked as default and is used
 * for calculating totals across accounts with different currencies.
 */
class GetHomeCurrencyUseCase @Inject constructor(
    private val getCurrenciesUseCase: GetCurrenciesUseCase
) {
    /**
     * Get the home currency synchronously.
     * Returns null if no home currency is set.
     */
    suspend fun execute(): CurrencyEntity? {
        val currencies = getCurrenciesUseCase.execute()
        return currencies.firstOrNull { it.isDefault }
    }
    
    /**
     * Observe the home currency as a Flow.
     */
    fun observe(): Flow<CurrencyEntity?> {
        return getCurrenciesUseCase.executeAsFlow().map { currencies ->
            currencies.firstOrNull { it.isDefault }
        }
    }
}

/**
 * Use case to set a currency as the home currency.
 * 
 * Only one currency can be the home currency at a time.
 * Setting a new home currency will automatically unset the previous one.
 */
class SetHomeCurrencyUseCase @Inject constructor(
    private val getCurrencyByIdUseCase: GetCurrencyByIdUseCase,
    private val updateCurrencyUseCase: UpdateCurrencyUseCase,
    private val getCurrenciesUseCase: GetCurrenciesUseCase
) {
    /**
     * Set the specified currency as the home currency.
     * Unsets any previously set home currency.
     */
    suspend fun execute(currencyId: Long): Result<Unit> {
        return try {
            // Get all currencies
            val allCurrencies = getCurrenciesUseCase.execute()
            
            // Get the currency to set as default
            val newHomeCurrency = getCurrencyByIdUseCase.execute(currencyId)
                ?: return Result.failure(Exception("Currency not found: $currencyId"))
            
            // Unset all other currencies' default flag
            allCurrencies.forEach { currency ->
                if (currency.isDefault && currency.id != currencyId) {
                    val updated = currency.copy(isDefault = false)
                    updateCurrencyUseCase.execute(updated)
                }
            }
            
            // Set the new home currency
            val updated = newHomeCurrency.copy(isDefault = true)
            updateCurrencyUseCase.execute(updated)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
