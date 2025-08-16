package ru.orangesoftware.financisto.playground

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Playground Application class with Hilt setup.
 * 
 * This is a clean application class that only uses Hilt dependency injection,
 * without any legacy AndroidAnnotations setup. This allows us to test
 * the new architecture components in isolation.
 */
@HiltAndroidApp
class PlaygroundApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging for debugging
        if (BuildConfig.DEBUG) {
            android.util.Log.d("PlaygroundApp", "Playground application started")
        }
    }
}
