package ru.orangesoftware.financisto.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.orangesoftware.financisto.db.DatabaseHelper
import javax.inject.Singleton

/**
 * Hilt module for application-level dependencies.
 * 
 * This module provides legacy dependencies that are specific to the main app
 * and not shared with the playground.
 * 
 * Coroutine dispatchers are now provided by core:common module.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    /**
     * Provides legacy DatabaseHelper for backward compatibility
     * Only needed in main app, not in playground
     */
    @Provides
    @Singleton
    fun provideDatabaseHelper(@ApplicationContext context: Context): DatabaseHelper {
        return DatabaseHelper(context)
    }
}

/**
 * Bridge module for providing bridge classes that facilitate
 * gradual migration from legacy code to modern architecture.
 * 
 * Bridges use composition and feature flags to route calls
 * between legacy DatabaseAdapter and modern repositories.
 * 
 * This module is specific to the main app as it handles legacy integration.
 */
@Module
@InstallIn(SingletonComponent::class)
object BridgeModule {

    // Note: Bridge classes are already annotated with @Singleton and @Inject
    // so Hilt will automatically provide them. No explicit @Provides needed.
    // This module is left here for future bridge-specific configuration.
}
