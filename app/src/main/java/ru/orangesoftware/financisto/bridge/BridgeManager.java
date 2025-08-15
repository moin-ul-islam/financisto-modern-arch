package ru.orangesoftware.financisto.bridge;

import android.content.Context;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.repository.modern.AccountRepository;
import ru.orangesoftware.financisto.repository.modern.TransactionRepository;
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsUseCase;
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsForAccountUseCase;
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase;
import ru.orangesoftware.financisto.usecase.modern.GetAccountByIdUseCase;
import ru.orangesoftware.financisto.usecase.modern.CreateAccountUseCase;
import ru.orangesoftware.financisto.usecase.modern.UpdateAccountUseCase;
import ru.orangesoftware.financisto.usecase.modern.DeleteAccountUseCase;
import ru.orangesoftware.financisto.usecase.modern.GetTransactionByIdUseCase;
import ru.orangesoftware.financisto.usecase.modern.CreateTransactionUseCase;
import ru.orangesoftware.financisto.usecase.modern.UpdateTransactionUseCase;
import ru.orangesoftware.financisto.usecase.modern.DeleteTransactionUseCase;
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsByDateRangeUseCase;
import ru.orangesoftware.financisto.usecase.modern.GetTransactionsByCategoryUseCase;
import ru.orangesoftware.financisto.usecase.modern.RebuildRunningBalanceForAccountUseCase;
import ru.orangesoftware.financisto.usecase.modern.RebuildAllRunningBalancesUseCase;
import ru.orangesoftware.financisto.usecase.modern.GetLastRunningBalanceForAccountUseCase;
import ru.orangesoftware.financisto.usecase.modern.GetAccountBalanceAtTimeUseCase;

/**
 * Bridge Manager utility for providing bridge instances in legacy activities.
 * 
 * This class provides bridge instances without requiring Hilt injection,
 * using manual dependency construction instead. This avoids conflicts
 * between AndroidAnnotations and Hilt dependency injection systems.
 * 
 * Phase 2.3 Implementation:
 * - Manually constructs bridge dependencies to avoid DI conflicts
 * - Provides singleton bridge instances for consistent behavior
 * - Enables bridge pattern to work with legacy application architecture
 * 
 * Note: This is a transitional solution. In future phases, we can migrate
 * to full Hilt injection once AndroidAnnotations is phased out.
 */
public class BridgeManager {
    
    private static BlotterBridge blotterBridge;
    private static AccountBridge accountBridge;
    private static TransactionBridge transactionBridge;
    
    /**
     * Get the BlotterBridge instance, creating it if needed.
     */
    public static synchronized BlotterBridge getBlotterBridge(Context context) {
        if (blotterBridge == null) {
            DatabaseAdapter db = new DatabaseAdapter(context);
            
            // Pass null for modern dependencies since feature flags are disabled by default
            // BlotterBridge code checks for null and uses safe calls so this is safe
            blotterBridge = new BlotterBridge(db, null, null, null);
        }
        return blotterBridge;
    }
    
    /**
     * Get the AccountBridge instance, creating it if needed.
     */
    public static synchronized AccountBridge getAccountBridge(Context context) {
        if (accountBridge == null) {
            DatabaseAdapter db = new DatabaseAdapter(context);
            
            // Pass null for modern dependencies since feature flags are disabled by default
            // Bridge code uses safe calls (?.) so this is safe
            accountBridge = new AccountBridge(db, null, null, null, null, null, null);
        }
        return accountBridge;
    }
    
    /**
     * Get the TransactionBridge instance, creating it if needed.
     */
    public static synchronized TransactionBridge getTransactionBridge(Context context) {
        if (transactionBridge == null) {
            DatabaseAdapter db = new DatabaseAdapter(context);
            
            // Pass null for modern dependencies since feature flags are disabled by default
            // Bridge code uses safe calls (?.) so this is safe
            transactionBridge = new TransactionBridge(db, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }
        return transactionBridge;
    }
    
}
