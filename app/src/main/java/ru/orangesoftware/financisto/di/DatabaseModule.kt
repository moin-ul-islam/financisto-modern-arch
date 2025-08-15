package ru.orangesoftware.financisto.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.orangesoftware.financisto.db.DatabaseHelper
import javax.inject.Singleton

/**
 * Hilt module for database-related dependencies.
 * 
 * This module will provide Room database instances and DAOs
 * when we migrate from the legacy SQLite implementation.
 * 
 * For now, it provides access to the legacy DatabaseHelper
 * to maintain compatibility during migration.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabaseHelper(@ApplicationContext context: Context): DatabaseHelper {
        return DatabaseHelper(context)
    }

    // TODO: Add Room database providers when migration is ready
    // @Provides
    // @Singleton
    // fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    //     return Room.databaseBuilder(
    //         context,
    //         AppDatabase::class.java,
    //         "financisto_database"
    //     ).build()
    // }
}
