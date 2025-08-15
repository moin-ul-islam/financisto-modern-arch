package ru.orangesoftware.financisto.app;

import android.content.Context;
import android.content.res.Configuration;
import androidx.multidex.MultiDexApplication;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import ru.orangesoftware.financisto.bus.GreenRobotBus;
import ru.orangesoftware.financisto.export.drive.GoogleDriveClient;
import ru.orangesoftware.financisto.utils.MyPreferences;

/**
 * Main Application class for Financisto.
 * 
 * Migration Strategy for Modern Architecture:
 * Phase 1: Currently using Android Annotations (@EApplication) for legacy DI
 * Phase 2: Will gradually migrate to Hilt DI by:
 *   - Adding @HiltAndroidApp annotation alongside @EApplication
 *   - Migrating individual components one by one
 *   - Eventually removing Android Annotations entirely
 * 
 * Dependencies are already configured for both DI systems to coexist.
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
