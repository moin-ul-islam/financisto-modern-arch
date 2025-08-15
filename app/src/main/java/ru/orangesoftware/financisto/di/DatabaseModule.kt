package ru.orangesoftware.financisto.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.orangesoftware.financisto.data.database.FinancistoDatabase
import ru.orangesoftware.financisto.data.dao.AccountDao
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.db.DatabaseHelper
import javax.inject.Singleton

/**
 * Hilt module for database-related dependencies.
 * 
 * This module provides both legacy DatabaseHelper and modern Room database
 * to support gradual migration. Both systems can coexist during the transition.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides legacy DatabaseHelper for backward compatibility
     */
    @Provides
    @Singleton
    fun provideDatabaseHelper(@ApplicationContext context: Context): DatabaseHelper {
        return DatabaseHelper(context)
    }

    /**
     * Provides modern Room database
     */
    @Provides
    @Singleton
    fun provideFinancistoDatabase(@ApplicationContext context: Context): FinancistoDatabase {
        return FinancistoDatabase.create(context)
    }

    /**
     * Provides AccountDao from Room database
     */
    @Provides
    fun provideAccountDao(database: FinancistoDatabase): AccountDao {
        return database.accountDao()
    }

    /**
     * Provides TransactionDao from Room database
     */
    @Provides
    fun provideTransactionDao(database: FinancistoDatabase): TransactionDao {
        return database.transactionDao()
    }
}
