package ru.orangesoftware.financisto.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for network-related dependencies.
 * 
 * This module provides HTTP clients and network-related components
 * for modern API communication and cloud backup services.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // TODO: Add Retrofit or other HTTP clients when needed
    // @Provides
    // @Singleton
    // fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    //     return Retrofit.Builder()
    //         .baseUrl("https://api.example.com/")
    //         .client(okHttpClient)
    //         .addConverterFactory(GsonConverterFactory.create())
    //         .build()
    // }
}
