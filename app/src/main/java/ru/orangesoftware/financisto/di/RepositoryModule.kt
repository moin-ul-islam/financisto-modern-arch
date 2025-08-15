package ru.orangesoftware.financisto.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import ru.orangesoftware.financisto.bridge.AccountBridge
import ru.orangesoftware.financisto.bridge.BlotterBridge
import ru.orangesoftware.financisto.bridge.TransactionBridge
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import ru.orangesoftware.financisto.repository.modern.AccountRepositoryImpl
import ru.orangesoftware.financisto.repository.modern.TransactionRepository
import ru.orangesoftware.financisto.repository.modern.TransactionRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 * 
 * This module binds repository interfaces to their implementations
 * for dependency injection throughout the app.
 * 
 * Phase 2.3: Added bridge class bindings for gradual migration
 * from legacy DatabaseAdapter to modern repository patterns.
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

/**
 * Bridge module for providing bridge classes that facilitate
 * gradual migration from legacy code to modern architecture.
 * 
 * Bridges use composition and feature flags to route calls
 * between legacy DatabaseAdapter and modern repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
object BridgeModule {

    // Note: Bridge classes are already annotated with @Singleton and @Inject
    // so Hilt will automatically provide them. No explicit @Provides needed.
    // This module is left here for future bridge-specific configuration.
}
