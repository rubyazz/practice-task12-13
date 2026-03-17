package com.example.task12_13.util

/**
 * Debug configuration object containing constants and helper functions
 * for debug-specific code and features.
 *
 * This object provides a centralized place to manage debug settings
 * and debug-only code execution.
 */
object DebugConfig {

    /**
     * Debug mode flag.
     * Set to true to enable debug features and verbose logging.
     * Set to false in production releases.
     *
     * Note: This is a manual flag. For automatic configuration,
     * consider using BuildConfig.DEBUG which is automatically
     * set by the Android build system.
     */
    const val DEBUG_MODE = true

    /**
     * Tag prefix for application logs.
     * Used to filter logs in Logcat.
     */
    const val LOG_TAG_PREFIX = "TaskApp_"

    /**
     * Execute code only in debug mode.
     * This function is useful for code that should not run in production,
     * such as verbose logging, test data, or debug UI elements.
     *
     * Usage:
     * ```kotlin
     * DebugConfig.debugOnly {
     *     Logger.d("Debug", "This only runs in debug mode")
     * }
     * ```
     *
     * @param block The code to execute in debug mode.
     */
    inline fun debugOnly(block: () -> Unit) {
        if (DEBUG_MODE) {
            block()
        }
    }

    /**
     * Execute code only in release mode.
     * This function is useful for production-specific behavior.
     *
     * Usage:
     * ```kotlin
     * DebugConfig.releaseOnly {
     *     // Production-only code
     * }
     * ```
     *
     * @param block The code to execute in release mode.
     */
    inline fun releaseOnly(block: () -> Unit) {
        if (!DEBUG_MODE) {
            block()
        }
    }

    /**
     * Get a debug tag with the standard prefix.
     * Helps maintain consistent log tags throughout the app.
     *
     * Usage:
     * ```kotlin
     * val tag = DebugConfig.tag("MainActivity")
     * // Result: "TaskApp_MainActivity"
     * ```
     *
     * @param className The class name to create a tag for.
     * @return A formatted tag string with prefix.
     */
    fun tag(className: String): String = "$LOG_TAG_PREFIX$className"

    /**
     * Check if we're currently in debug mode.
     *
     * @return true if debug mode is enabled, false otherwise.
     */
    fun isDebug(): Boolean = DEBUG_MODE

    /**
     * Log debug information only in debug mode.
     * Combines mode checking with logging.
     *
     * Usage:
     * ```kotlin
     * DebugConfig.logDebug("MainActivity", "Activity created")
     * ```
     *
     * @param tag The log tag.
     * @param message The message to log.
     */
    fun logDebug(tag: String, message: String) {
        debugOnly {
            Logger.d(tag, message)
        }
    }

    /**
     * Measure execution time of a code block.
     * Useful for performance profiling in debug builds.
     *
     * Usage:
     * ```kotlin
     * DebugConfig.measureTime("databaseOperation") {
     *     // Code to measure
     *     database.saveData(data)
     * }
     * ```
     *
     * @param operationName Name of the operation for logging.
     * @param block The code to measure.
     * @return The result of the code block execution.
     */
    inline fun <T> measureTime(operationName: String, block: () -> T): T {
        if (DEBUG_MODE) {
            val startTime = System.nanoTime()
            val result = block()
            val endTime = System.nanoTime()
            val durationMs = (endTime - startTime) / 1_000_000.0
            Logger.d("Performance", "$operationName took ${durationMs}ms")
            return result
        }
        return block()
    }
}

/**
 * Debugging Tips and Best Practices:
 *
 * 1. Using BuildConfig.DEBUG (Recommended for production):
 *    - Replace `DebugConfig.DEBUG_MODE` with `BuildConfig.DEBUG` in production code
 *    - BuildConfig.DEBUG is automatically set to false in release builds
 *    - Example: `if (BuildConfig.DEBUG) { ... }`
 *
 * 2. Conditional Compilation:
 *    - Use `@DebugOnly` annotation to mark debug-only functions
 *    - The compiler will strip these from release builds
 *    - Example:
 *      ```kotlin
 *      @DebugOnly
 *      fun debugFunction() { ... }
 *      ```
 *
 * 3. Logcat Filtering:
 *    - Filter by tag prefix: `tag:TaskApp_`
 *    - This shows all app logs with the standard prefix
 *    - Add to Logcat search: `package:com.example.task12_13`
 *
 * 4. Breakpoint Tips:
 *    - Conditional breakpoints: Right-click breakpoint -> Edit Breakpoint -> Condition
 *    - Example condition: `task.id == "123"` (only breaks when true)
 *    - Log-only breakpoints: Check "Log evaluated expression" instead of "Suspend"
 *
 * 5. Variable Inspection:
 *    - Hover over variables to see their current value
 *    - Use "Evaluate Expression" (Alt+F8) to run code snippets
 *    - Add custom renderers for complex objects
 *
 * 6. Network Debugging:
 *    - Use Charles Proxy or Fiddler to inspect network traffic
 *    - Enable "Network Security Config" for HTTPS inspection in debug builds
 */
