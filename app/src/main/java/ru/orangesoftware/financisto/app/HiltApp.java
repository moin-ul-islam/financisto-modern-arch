package ru.orangesoftware.financisto.app;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

/**
 * Hilt Application class for modern dependency injection.
 * This class is used alongside the legacy Android Annotations DI system
 * during the migration phase to provide coexistence.
 * 
 * Note: This is not the main Application class - FinancistoApp remains the main one.
 * This class provides Hilt components that can be accessed when needed in new code.
 */
@HiltAndroidApp
public class HiltApp extends Application {
    // Hilt will generate the necessary code for dependency injection
    // This class serves as the entry point for Hilt DI during migration
}
