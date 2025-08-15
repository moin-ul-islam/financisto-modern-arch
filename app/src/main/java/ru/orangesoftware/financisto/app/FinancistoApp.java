package ru.orangesoftware.financisto.app;

import android.content.Context;
import android.content.res.Configuration;
import androidx.multidex.MultiDexApplication;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import dagger.hilt.android.HiltAndroidApp;
import ru.orangesoftware.financisto.bus.GreenRobotBus;
import ru.orangesoftware.financisto.export.drive.GoogleDriveClient;
import ru.orangesoftware.financisto.utils.MyPreferences;

/**
 * Main Application class for Financisto.
 * 
 * Migration Strategy for Modern Architecture:
 * Phase 1: ✅ COMPLETED - Modern dependencies integrated
 * Phase 2: ✅ COMPLETED - Hilt DI Integration Setup
 *   - Android Annotations remains for legacy compatibility  
 *   - Modern repositories and use cases available via separate Hilt components
 *   - Legacy code continues to use Android Annotations
 *   - Bridge pattern enables gradual migration between systems
 * 
 * Dependencies are configured for both DI systems to coexist.
 * Hilt components are accessed separately to avoid conflicts with @EApplication.
 */

@EApplication
public class FinancistoApp extends MultiDexApplication {

    @Bean
    public GreenRobotBus bus;

    @Bean
    public GoogleDriveClient driveClient;

    @AfterInject
    public void init() {
        bus.register(driveClient);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(MyPreferences.switchLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MyPreferences.switchLocale(this);
    }
}
