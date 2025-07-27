package ru.orangesoftware.financisto.bridge;

import ru.orangesoftware.financisto.db.DatabaseAdapter;

/**
 * Test utility class to verify Java-Kotlin interoperability for bridge classes.
 * This demonstrates how Java code can easily use the Kotlin bridge classes.
 */
public class BridgeTestUtil {
    
    /**
     * Test method to verify extension functions work from Java.
     * This method demonstrates the usage pattern for Java Activities.
     */
    public static void testBridgeCreation(DatabaseAdapter db) {
        // Test direct constructor usage
        BlotterBridge blotterBridge1 = new BlotterBridge(db);
        AccountBridge accountBridge1 = new AccountBridge(db);  
        TransactionBridge transactionBridge1 = new TransactionBridge(db);
        
        // Test extension functions (when available)
        BlotterBridge blotterBridge2 = BridgeExtensions.createBlotterBridge(db);
        AccountBridge accountBridge2 = BridgeExtensions.createAccountBridge(db);
        TransactionBridge transactionBridge2 = BridgeExtensions.createTransactionBridge(db);
        
        // Both approaches should work identically
        // This validates our Java-Kotlin interop implementation
    }
}
