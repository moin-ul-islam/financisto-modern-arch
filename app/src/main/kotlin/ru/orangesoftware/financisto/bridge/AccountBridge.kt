package ru.orangesoftware.financisto.bridge

import kotlinx.coroutines.runBlocking
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.data.model.ModelConverters.toLegacyModel
import ru.orangesoftware.financisto.data.model.ModelConverters.toRoomEntity
import ru.orangesoftware.financisto.db.DatabaseAdapter
import ru.orangesoftware.financisto.model.Account
import ru.orangesoftware.financisto.repository.modern.AccountRepository
import ru.orangesoftware.financisto.usecase.modern.GetAccountByIdUseCase
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase
import ru.orangesoftware.financisto.usecase.modern.CreateAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.UpdateAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.DeleteAccountUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridge class that handles the transition from direct DatabaseAdapter calls
 * to modern architecture components for Account operations.
 * 
 * This class uses composition to wrap the legacy DatabaseAdapter and provides
 * routing logic based on feature flags. When modernization flags are enabled,
 * it routes calls to new implementations; otherwise, it delegates to legacy code.
 * 
 * Phase 2.3 Implementation:
 * - Uses modern repositories and use cases when flags are enabled
 * - Provides seamless data conversion between legacy models and Room entities
 * - Maintains full backward compatibility with existing code
 */
@Singleton
class AccountBridge @Inject constructor(
    private val legacyDb: DatabaseAdapter,
    private val accountRepository: AccountRepository,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val createAccountUseCase: CreateAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) {
    
    /**
     * Gets an account by ID with routing based on feature flags.
     */
    fun getAccount(accountId: Long): Account? {
        return if (FeatureFlags.USE_ACCOUNT_BRIDGE) {
            getAccountModern(accountId)
        } else {
            legacyDb.getAccount(accountId)
        }
    }

    /**
     * Gets all accounts with routing based on feature flags.
     */
    fun getAllAccounts(): List<Account> {
        return if (FeatureFlags.USE_ACCOUNT_BRIDGE) {
            getAllAccountsModern()
        } else {
            legacyDb.getAllAccountsList()
        }
    }

    /**
     * Creates or updates an account with routing based on feature flags.
     */
    fun saveOrUpdate(account: Account): Long {
        return if (FeatureFlags.USE_ACCOUNT_BRIDGE) {
            saveOrUpdateModern(account)
        } else {
            legacyDb.saveOrUpdate(account)
        }
    }

    /**
     * Deletes an account with routing based on feature flags.
     */
    fun deleteAccount(accountId: Long): Boolean {
        return if (FeatureFlags.USE_ACCOUNT_BRIDGE) {
            deleteAccountModern(accountId)
        } else {
            try {
                legacyDb.deleteAccount(accountId)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    // ========================================
    // Modern Implementations
    // ========================================

    /**
     * Modern implementation for getAccount using Room repository.
     */
    private fun getAccountModern(accountId: Long): Account? {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("AccountBridge", "Using modern getAccount implementation for ID: $accountId")
            }
            
            val accountEntity = runBlocking { 
                getAccountByIdUseCase.execute(accountId) 
            }
            
            val result = accountEntity?.toLegacyModel()
            
            // Validation: Compare with legacy result if enabled
            if (FeatureFlags.ENABLE_BRIDGE_VALIDATION && result != null) {
                validateAccountResult(accountId, result, legacyDb.getAccount(accountId))
            }
            
            result
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("AccountBridge", "Error in modern getAccount, falling back to legacy", e)
            }
            legacyDb.getAccount(accountId)
        }
    }

    /**
     * Modern implementation for getAllAccounts using Room repository.
     */
    private fun getAllAccountsModern(): List<Account> {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("AccountBridge", "Using modern getAllAccounts implementation")
            }
            
            val accountEntities = runBlocking { 
                getAccountsUseCase.execute() 
            }
            
            val result = accountEntities.map { it.toLegacyModel() }
            
            // Validation: Compare count with legacy result if enabled
            if (FeatureFlags.ENABLE_BRIDGE_VALIDATION) {
                val legacyResult = legacyDb.getAllAccountsList()
                if (result.size != legacyResult.size) {
                    android.util.Log.w("AccountBridge", 
                        "Account count mismatch: modern=${result.size}, legacy=${legacyResult.size}")
                }
            }
            
            result
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("AccountBridge", "Error in modern getAllAccounts, falling back to legacy", e)
            }
            legacyDb.getAllAccountsList()
        }
    }

    /**
     * Modern implementation for saveOrUpdate using Room repository.
     */
    private fun saveOrUpdateModern(account: Account): Long {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("AccountBridge", "Using modern saveOrUpdate implementation for: ${account.title}")
            }
            
            val accountEntity = account.toRoomEntity()
            val result = if (account.id <= 0) {
                // Create new account
                val createResult = runBlocking { 
                    createAccountUseCase.execute(accountEntity) 
                }
                createResult.getOrElse { -1L }
            } else {
                // Update existing account
                val updateResult = runBlocking { 
                    updateAccountUseCase.execute(accountEntity) 
                }
                if (updateResult.getOrElse { false }) account.id else -1L
            }
            
            result
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("AccountBridge", "Error in modern saveOrUpdate, falling back to legacy", e)
            }
            legacyDb.saveOrUpdate(account)
        }
    }

    /**
     * Modern implementation for deleteAccount using Room repository.
     */
    private fun deleteAccountModern(accountId: Long): Boolean {
        return try {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.d("AccountBridge", "Using modern deleteAccount implementation for ID: $accountId")
            }
            
            val result = runBlocking { 
                deleteAccountUseCase.execute(accountId) 
            }
            
            result.getOrElse { false }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
                android.util.Log.e("AccountBridge", "Error in modern deleteAccount, falling back to legacy", e)
            }
            try {
                legacyDb.deleteAccount(accountId)
                true
            } catch (legacyException: Exception) {
                false
            }
        }
    }

    /**
     * Validates that modern and legacy results match for account operations.
     */
    private fun validateAccountResult(accountId: Long, modernResult: Account?, legacyResult: Account?) {
        if (modernResult == null && legacyResult == null) return
        if (modernResult == null || legacyResult == null) {
            android.util.Log.w("AccountBridge", 
                "Account result mismatch for ID $accountId: modern=$modernResult, legacy=$legacyResult")
            return
        }
        
        if (modernResult.id != legacyResult.id || 
            modernResult.title != legacyResult.title ||
            modernResult.totalAmount != legacyResult.totalAmount) {
            android.util.Log.w("AccountBridge", 
                "Account data mismatch for ID $accountId: " +
                "modern=[${modernResult.id}, ${modernResult.title}, ${modernResult.totalAmount}], " +
                "legacy=[${legacyResult.id}, ${legacyResult.title}, ${legacyResult.totalAmount}]")
        }
    }
}
