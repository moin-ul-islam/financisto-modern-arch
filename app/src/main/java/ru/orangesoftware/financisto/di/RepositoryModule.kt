package ru.orangesoftware.financisto.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import ru.orangesoftware.financisto.repository.modern.AccountRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 * 
 * This module binds repository interfaces to their implementations
 * for dependency injection throughout the app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    // TODO: Add other repository bindings as they are migrated
    // @Binds
    // @Singleton  
    // abstract fun bindTransactionRepository(
    //     transactionRepositoryImpl: TransactionRepositoryImpl
    // ): TransactionRepository
}

/**
 * Provides coroutine dispatchers for repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryDispatchersModule {

    @Provides
    @Singleton
    fun provideIoDispatcherForRepository() = Dispatchers.IO
}
