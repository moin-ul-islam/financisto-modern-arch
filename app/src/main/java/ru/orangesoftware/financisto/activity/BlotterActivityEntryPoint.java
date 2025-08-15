package ru.orangesoftware.financisto.activity;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import ru.orangesoftware.financisto.bridge.AccountBridge;
import ru.orangesoftware.financisto.bridge.BlotterBridge;
import ru.orangesoftware.financisto.bridge.TransactionBridge;

/**
 * Hilt EntryPoint interface for manual injection into BlotterActivity.
 * 
 * This is needed because BlotterActivity extends AbstractListActivity which extends
 * the legacy Android ListActivity, not ComponentActivity. Since Hilt requires
 * ComponentActivity for @AndroidEntryPoint, we use manual injection via EntryPoint.
 * 
 * Phase 2.3 Implementation:
 * - Provides access to all bridge instances for activities that can't use @AndroidEntryPoint
 * - Maintains dependency injection benefits while working with legacy activity hierarchy
 * - Ensures bridges are properly instantiated with all their dependencies
 */
@EntryPoint
@InstallIn(SingletonComponent.class)
public interface BlotterActivityEntryPoint {
    
    /**
     * Get the BlotterBridge instance with all dependencies injected
     */
    BlotterBridge getBlotterBridge();
    
    /**
     * Get the AccountBridge instance with all dependencies injected
     */
    AccountBridge getAccountBridge();
    
    /**
     * Get the TransactionBridge instance with all dependencies injected
     */
    TransactionBridge getTransactionBridge();
}
