package ru.orangesoftware.financisto.core.common

/**
 * Core Common module for shared functionality.
 * This module contains:
 * - Feature flags for modernization (FeatureFlags)
 * - Bridge pattern implementations for gradual migration
 * - Extension functions for Java-Kotlin interop
 * - Shared utilities and constants
 */
object CoreCommon {
    const val MODULE_NAME = "core:common"
    const val VERSION = "1.0"
    
    /**
     * Initializes the core common module.
     * Called during app startup to ensure proper configuration.
     */
    fun initialize() {
        // Module initialization logic if needed
        if (FeatureFlags.ENABLE_MODERNIZATION_LOGS) {
            println("CoreCommon module initialized - Version $VERSION")
        }
    }
}
