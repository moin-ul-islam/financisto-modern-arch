package ru.orangesoftware.financisto.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import ru.orangesoftware.financisto.repository.modern.AccountRepositoryImpl
import ru.orangesoftware.financisto.repository.modern.CurrencyRepository
import ru.orangesoftware.financisto.repository.modern.CurrencyRepositoryImpl
import ru.orangesoftware.financisto.repository.modern.TransactionRepository
import ru.orangesoftware.financisto.repository.modern.TransactionRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 * 
 * This module binds repository interfaces to their implementations
 * for dependency injection throughout the app.
 * 
 * Moved to repository module so it can be shared between main app and playground.
 * Coroutine dispatchers are provided by core:common module.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton  
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(
        currencyRepositoryImpl: CurrencyRepositoryImpl
    ): CurrencyRepository
}
