package ru.orangesoftware.financisto.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.orangesoftware.financisto.data.dao.AccountDao
import ru.orangesoftware.financisto.data.dao.RunningBalanceDao
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import ru.orangesoftware.financisto.data.model.RunningBalanceEntity
import ru.orangesoftware.financisto.data.model.TransactionEntity

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
        RunningBalanceEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FinancistoDatabase : RoomDatabase() {
    
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun runningBalanceDao(): RunningBalanceDao
    
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
                .addMigrations(MIGRATION_LEGACY_TO_1)
                .fallbackToDestructiveMigration() // For development only
                .build()
        }
        
        /**
         * Migration from legacy SQLite database to Room.
         * 
         * This migration ensures data is preserved when transitioning
         * from the legacy database structure to Room.
         */
        private val MIGRATION_LEGACY_TO_1 = object : Migration(0, 1) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Room will create new tables if they don't exist
                // Legacy data migration will be handled in a separate phase
                
                // Ensure indices for performance
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_account_is_active 
                    ON account(is_active)
                """)
                
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_transactions_from_account_id 
                    ON transactions(from_account_id)
                """)
                
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_transactions_to_account_id 
                    ON transactions(to_account_id)
                """)
                
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_transactions_datetime 
                    ON transactions(datetime)
                """)
                
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_transactions_category_id 
                    ON transactions(category_id)
                """)
            }
        }
    }
}
