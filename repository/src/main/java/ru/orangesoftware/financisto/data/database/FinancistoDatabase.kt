package ru.orangesoftware.financisto.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.orangesoftware.financisto.data.dao.AccountDao
import ru.orangesoftware.financisto.data.dao.RunningBalanceDao
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.data.dao.CategoryDao
import ru.orangesoftware.financisto.data.dao.BudgetDao
import ru.orangesoftware.financisto.data.dao.ProjectDao
import ru.orangesoftware.financisto.data.dao.AttributeDao
import ru.orangesoftware.financisto.data.dao.SmsTemplateDao
import ru.orangesoftware.financisto.data.dao.LocationDao
import ru.orangesoftware.financisto.data.dao.PayeeDao
import ru.orangesoftware.financisto.data.dao.CategoryAttributeDao
import ru.orangesoftware.financisto.data.dao.TransactionAttributeDao
import ru.orangesoftware.financisto.data.dao.CreditCardClosingDateDao
import ru.orangesoftware.financisto.data.dao.CurrencyExchangeRateDao
import ru.orangesoftware.financisto.data.dao.CurrencyDao
import ru.orangesoftware.financisto.data.model.*

/**
 * Modern Room database for Financisto.
 * 
 * This database provides type-safe access to financial data using Room's
 * compile-time verification and modern reactive patterns.
 * 
 * Migration Strategy:
 * - Starts with existing table structure for compatibility
 * - Gradual migration from legacy SQLite to Room
 * - Maintains data integrity during transition
 */
@Database(
    entities = [
        AccountEntity::class,
        CurrencyEntity::class,
        TransactionEntity::class,
        RunningBalanceEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        ProjectEntity::class,
        AttributeEntity::class,
        SmsTemplateEntity::class,
        LocationEntity::class,
        PayeeEntity::class,
        CategoryAttributeEntity::class,
        TransactionAttributeEntity::class,
        CreditCardClosingDateEntity::class,
        CurrencyExchangeRateEntity::class
    ],
    views = [
        CategoryView::class,
        AttributeView::class
    ],
    version = 4,
    exportSchema = true
)
abstract class FinancistoDatabase : RoomDatabase() {
    
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun runningBalanceDao(): RunningBalanceDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun projectDao(): ProjectDao
    abstract fun attributeDao(): AttributeDao
    abstract fun smsTemplateDao(): SmsTemplateDao
    abstract fun locationDao(): LocationDao
    abstract fun payeeDao(): PayeeDao
    abstract fun categoryAttributeDao(): CategoryAttributeDao
    abstract fun transactionAttributeDao(): TransactionAttributeDao
    abstract fun creditCardClosingDateDao(): CreditCardClosingDateDao
    abstract fun currencyExchangeRateDao(): CurrencyExchangeRateDao
    abstract fun currencyDao(): CurrencyDao
    
    companion object {
        const val DATABASE_NAME = "financisto_modern.db"
        
        /**
         * Creates the Room database instance.
         * 
         * Note: This is called from Hilt modules and should not be used directly.
         * The database instance is provided through dependency injection.
         */
        fun create(
            context: android.content.Context,
            useInMemory: Boolean = false
        ): FinancistoDatabase {
            val builder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, FinancistoDatabase::class.java)
            } else {
                Room.databaseBuilder(context, FinancistoDatabase::class.java, DATABASE_NAME)
            }
            
            return builder
                .fallbackToDestructiveMigration() // For development only
                .build()
        }

    }
}
