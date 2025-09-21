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
import ru.orangesoftware.financisto.data.dao.RunningBalanceDao
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.data.dao.CurrencyDao
import ru.orangesoftware.financisto.data.dao.CategoryDao
import javax.inject.Singleton

/**
 * Hilt module for database-related dependencies.
 * 
 * This module provides Room database and DAOs.
 * Moved to repository module so it can be shared between main app and playground.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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

    /**
     * Provides RunningBalanceDao from Room database
     */
    @Provides
    fun provideRunningBalanceDao(database: FinancistoDatabase): RunningBalanceDao {
        return database.runningBalanceDao()
    }

    /**
     * Provides CurrencyDao from Room database
     */
    @Provides
    fun provideCurrencyDao(database: FinancistoDatabase): CurrencyDao {
        return database.currencyDao()
    }

    /**
     * Provides CategoryDao from Room database
     */
    @Provides
    fun provideCategoryDao(database: FinancistoDatabase): CategoryDao {
        return database.categoryDao()
    }
}
