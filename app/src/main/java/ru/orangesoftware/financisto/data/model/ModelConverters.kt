package ru.orangesoftware.financisto.data.model

import ru.orangesoftware.financisto.model.Account
import ru.orangesoftware.financisto.model.Currency
import ru.orangesoftware.financisto.model.SymbolFormat
import ru.orangesoftware.financisto.model.Transaction
import ru.orangesoftware.financisto.model.TransactionStatus
// Import Room entities from repository module
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import ru.orangesoftware.financisto.data.model.TransactionEntity

/**
 * Conversion utilities between legacy models and Room entities.
 * 
 * These utilities are in the app module since they need access to both
 * legacy models (in app module) and Room entities (from repository module).
 * This is proper architecture separation.
 */
object ModelConverters {

    /**
     * Convert legacy Account to AccountEntity
     */
    fun Account.toRoomEntity(): AccountEntity {
        return AccountEntity(
            id = this.id,
            title = this.title ?: "",
            type = this.type ?: "CASH",
            currencyId = this.currency?.id ?: 0,
            totalAmount = this.totalAmount,
            isActive = this.isActive,
            isIncludeIntoTotals = this.isIncludeIntoTotals,
            creationDate = this.creationDate,
            lastTransactionDate = this.lastTransactionDate,
            note = this.note ?: "",
            issuer = this.issuer ?: "",
            number = this.number ?: "",
            sortOrder = this.sortOrder,
            limitAmount = this.limitAmount,
            cardIssuer = this.cardIssuer ?: "",
            closingDay = this.closingDay,
            paymentDay = this.paymentDay
        )
    }

    /**
     * Convert AccountEntity to legacy Account
     */
    fun AccountEntity.toLegacyModel(): Account {
        val account = Account()
        account.id = this.id
        account.title = this.title
        account.type = this.type
        // Note: currency object will need to be resolved separately
        // account.currency = ... // This requires currency lookup
        account.totalAmount = this.totalAmount
        account.isActive = this.isActive
        account.isIncludeIntoTotals = this.isIncludeIntoTotals
        account.creationDate = this.creationDate
        account.lastTransactionDate = this.lastTransactionDate
        account.note = this.note
        account.issuer = this.issuer
        account.number = this.number
        account.sortOrder = this.sortOrder
        account.limitAmount = this.limitAmount
        account.cardIssuer = this.cardIssuer
        account.closingDay = this.closingDay
        account.paymentDay = this.paymentDay
        return account
    }

    /**
     * Convert legacy Currency to CurrencyEntity
     */
    fun Currency.toRoomEntity(): CurrencyEntity {
        return CurrencyEntity(
            id = this.id,
            name = this.name ?: "",
            title = this.title ?: "",
            symbol = this.symbol ?: "",
            isDefault = this.isDefault,
            decimals = this.decimals,
            decimalSeparator = this.decimalSeparator ?: ".",
            groupSeparator = this.groupSeparator ?: ",",
            symbolFormat = this.symbolFormat?.name ?: "RS",
            updatedDate = System.currentTimeMillis()
        )
    }

    /**
     * Convert CurrencyEntity to legacy Currency
     */
    fun CurrencyEntity.toLegacyModel(): Currency {
        val currency = Currency()
        currency.id = this.id
        currency.name = this.name
        currency.title = this.title
        currency.symbol = this.symbol
        currency.isDefault = this.isDefault
        currency.decimals = this.decimals
        currency.decimalSeparator = this.decimalSeparator
        currency.groupSeparator = this.groupSeparator
        currency.symbolFormat = try {
            SymbolFormat.valueOf(this.symbolFormat)
        } catch (e: Exception) {
            SymbolFormat.RS
        }
        return currency
    }

    /**
     * Convert legacy Transaction to TransactionEntity
     */
    fun Transaction.toRoomEntity(): TransactionEntity {
        return TransactionEntity(
            id = this.id,
            parentId = this.parentId,
            fromAccountId = this.fromAccountId,
            toAccountId = this.toAccountId,
            categoryId = this.categoryId,
            projectId = this.projectId,
            locationId = this.locationId,
            payeeId = this.payeeId,
            fromAmount = this.fromAmount,
            toAmount = this.toAmount,
            datetime = this.dateTime,
            note = this.note ?: "",
            status = this.status?.name ?: "UR",
            isTemplate = this.isTemplate(),
            templateName = this.templateName ?: "",
            recurrence = this.recurrence ?: "",
            notificationOptions = this.notificationOptions ?: "",
            originalCurrencyId = this.originalCurrencyId,
            originalFromAmount = this.originalFromAmount,
            isCCardPayment = this.isCCardPayment == 1
        )
    }

    /**
     * Convert TransactionEntity to legacy Transaction
     */
    fun TransactionEntity.toLegacyModel(): Transaction {
        val transaction = Transaction()
        transaction.id = this.id
        transaction.parentId = this.parentId
        transaction.fromAccountId = this.fromAccountId
        transaction.toAccountId = this.toAccountId
        transaction.categoryId = this.categoryId
        transaction.projectId = this.projectId
        transaction.locationId = this.locationId
        transaction.payeeId = this.payeeId
        transaction.fromAmount = this.fromAmount
        transaction.toAmount = this.toAmount
        transaction.dateTime = this.datetime
        transaction.note = this.note
        transaction.status = try {
            TransactionStatus.valueOf(this.status)
        } catch (e: Exception) {
            TransactionStatus.UR
        }
        transaction.isTemplate = if (this.isTemplate) 1 else 0
        transaction.templateName = this.templateName
        transaction.recurrence = this.recurrence
        transaction.notificationOptions = this.notificationOptions
        transaction.originalCurrencyId = this.originalCurrencyId
        transaction.originalFromAmount = this.originalFromAmount
        transaction.isCCardPayment = if (this.isCCardPayment) 1 else 0
        return transaction
    }
}