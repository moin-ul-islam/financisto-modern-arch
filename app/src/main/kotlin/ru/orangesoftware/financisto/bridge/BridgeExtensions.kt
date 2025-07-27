@file:JvmName("BridgeExtensions")

package ru.orangesoftware.financisto.bridge

import ru.orangesoftware.financisto.db.DatabaseAdapter

/**
 * Extension functions for seamless Java-Kotlin interop with bridge classes.
 * These extensions allow Java Activities to easily work with Kotlin bridge classes.
 * 
 * Usage in Java Activities:
 * ```java
 * import static ru.orangesoftware.financisto.bridge.BridgeExtensions.*;
 * 
 * // Create bridges
 * BlotterBridge blotterBridge = createBlotterBridge(db);
 * AccountBridge accountBridge = createAccountBridge(db);
 * TransactionBridge transactionBridge = createTransactionBridge(db);
 * ```
 */

/**
 * Creates a BlotterBridge instance for the given DatabaseAdapter.
 * This extension provides a clean way for Java code to create bridge instances.
 */
@JvmName("createBlotterBridge")
fun DatabaseAdapter.createBlotterBridge(): BlotterBridge = BlotterBridge(this)

/**
 * Creates an AccountBridge instance for the given DatabaseAdapter.
 * This extension provides a clean way for Java code to create bridge instances.
 */
@JvmName("createAccountBridge")
fun DatabaseAdapter.createAccountBridge(): AccountBridge = AccountBridge(this)

/**
 * Creates a TransactionBridge instance for the given DatabaseAdapter.
 * This extension provides a clean way for Java code to create bridge instances.
 */
@JvmName("createTransactionBridge")
fun DatabaseAdapter.createTransactionBridge(): TransactionBridge = TransactionBridge(this)
