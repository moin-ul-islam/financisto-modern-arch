package ru.orangesoftware.financisto.playground.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import dagger.hilt.android.AndroidEntryPoint
import ru.orangesoftware.financisto.playground.R
import ru.orangesoftware.financisto.core.common.FeatureFlags
import ru.orangesoftware.financisto.core.ui.navigation.FragmentNavigator
import ru.orangesoftware.financisto.feature.account.ui.AccountListFragment
import ru.orangesoftware.financisto.feature.blotter.ui.BlotterFragment
import ru.orangesoftware.financisto.feature.transaction.ui.TransactionFormFragment

/**
 * Modern single-Activity container that hosts Fragments.
 * This will eventually replace the TabActivity-based MainActivity when
 * USE_FRAGMENT_ARCHITECTURE is enabled.
 * Located in app-playground module where Hilt is properly configured.
 */
@AndroidEntryPoint
class ModernMainActivity : AppCompatActivity(), FragmentNavigator {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (!FeatureFlags.USE_FRAGMENT_ARCHITECTURE) {
            // Fall back to legacy MainActivity
            finish()
            return
        }

        setContentView(R.layout.activity_modern_main)

        if (savedInstanceState == null) {
            // Show the initial fragment (Account List for now)
            navigateToAccountList()
        }
    }

    // FragmentNavigator implementation
    override fun navigateToAccountList() {
        replaceFragment(AccountListFragment.newInstance(), "AccountList")
    }

    override fun navigateToBlotter(accountId: Long) {
        replaceFragment(BlotterFragment.newInstance(accountId), "Blotter")
    }

    override fun navigateToTransactionForm(
        transactionId: Long,
        accountId: Long,
        duplicate: Boolean,
        fromTemplate: Boolean
    ) {
        replaceFragment(
            TransactionFormFragment.newInstance(transactionId, accountId, duplicate, fromTemplate),
            "TransactionForm"
        )
    }

    override fun navigateBack(): Boolean {
        return if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
            true
        } else {
            false
        }
    }

    override fun closeCurrentScreen() {
        if (!navigateBack()) {
            finish()
        }
    }

    // Legacy methods for compatibility (can be removed later)
    /**
     * Navigate to Account List screen
     */
    fun showAccountList() {
        navigateToAccountList()
    }

    /**
     * Navigate to Blotter screen
     */
    fun showBlotter(accountId: Long = -1L) {
        navigateToBlotter(accountId)
    }

    /**
     * Navigate to Transaction Form screen
     */
    fun showTransactionForm(
        transactionId: Long = -1L,
        accountId: Long = -1L,
        duplicate: Boolean = false,
        fromTemplate: Boolean = false
    ) {
        navigateToTransactionForm(transactionId, accountId, duplicate, fromTemplate)
    }

    /**
     * Replace the current fragment with a new one
     */
    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .addToBackStack(tag)
            .commit()
    }

    /**
     * Navigate back in the fragment stack
     */
    override fun onBackPressed() {
        if (!navigateBack()) {
            super.onBackPressed()
        }
    }
}
