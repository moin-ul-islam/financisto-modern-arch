package ru.orangesoftware.financisto.core.ui.fragment

import androidx.fragment.app.Fragment
import ru.orangesoftware.financisto.core.ui.navigation.FragmentNavigator

/**
 * Base Fragment class that provides access to navigation functionality.
 * All modern Fragments should extend this class.
 */
abstract class BaseFragment : Fragment() {

    /**
     * Get the FragmentNavigator from the host Activity.
     * Returns null if the host Activity doesn't implement FragmentNavigator.
     */
    protected val navigator: FragmentNavigator?
        get() = activity as? FragmentNavigator

    /**
     * Navigate back in the navigation stack.
     * Returns true if navigation was handled, false otherwise.
     */
    protected fun navigateBack(): Boolean {
        return navigator?.navigateBack() ?: false
    }

    /**
     * Close the current screen.
     * This will navigate back or finish the Activity if this is the last screen.
     */
    protected fun closeCurrentScreen() {
        navigator?.closeCurrentScreen() ?: activity?.finish()
    }
}
