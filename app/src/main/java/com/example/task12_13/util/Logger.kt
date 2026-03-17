package com.example.task12_13.util

import timber.log.Timber

/**
 * Wrapper class for logging using Timber.
 * Provides a consistent logging interface throughout the application.
 *
 * Usage:
 * ```kotlin
 * Logger.d("MyTag", "Debug message")
 * Logger.i("MyTag", "Info message")
 * Logger.w("MyTag", "Warning message")
 * Logger.e("MyTag", "Error message", exception)
 * ```
 *
 * To use Timber, initialize it in your Application class:
 * ```kotlin
 * if (BuildConfig.DEBUG) {
 *     Timber.plant(Timber.DebugTree())
 * }
 * ```
 */
object Logger {

    /**
     * Log a debug message.
     * Use for detailed debugging information that is useful during development.
     *
     * @param tag Optional tag to identify the log source. If null, uses Timber's default.
     * @param message The message to log.
     */
    fun d(tag: String? = null, message: String) {
        if (tag != null) {
            Timber.tag(tag).d(message)
        } else {
            Timber.d(message)
        }
    }

    /**
     * Log an info message.
     * Use for general informational messages about normal application flow.
     *
     * @param tag Optional tag to identify the log source. If null, uses Timber's default.
     * @param message The message to log.
     */
    fun i(tag: String? = null, message: String) {
        if (tag != null) {
            Timber.tag(tag).i(message)
        } else {
            Timber.i(message)
        }
    }

    /**
     * Log a warning message.
     * Use for potentially harmful situations that don't prevent the app from running.
     *
     * @param tag Optional tag to identify the log source. If null, uses Timber's default.
     * @param message The message to log.
     */
    fun w(tag: String? = null, message: String) {
        if (tag != null) {
            Timber.tag(tag).w(message)
        } else {
            Timber.w(message)
        }
    }

    /**
     * Log a warning message with a throwable.
     *
     * @param tag Optional tag to identify the log source. If null, uses Timber's default.
     * @param message The message to log.
     * @param throwable The exception or error to log.
     */
    fun w(tag: String? = null, message: String, throwable: Throwable) {
        if (tag != null) {
            Timber.tag(tag).w(throwable, message)
        } else {
            Timber.w(throwable, message)
        }
    }

    /**
     * Log an error message.
     * Use for error events that might still allow the application to continue running.
     *
     * @param tag Optional tag to identify the log source. If null, uses Timber's default.
     * @param message The message to log.
     */
    fun e(tag: String? = null, message: String) {
        if (tag != null) {
            Timber.tag(tag).e(message)
        } else {
            Timber.e(message)
        }
    }

    /**
     * Log an error message with a throwable and stack trace.
     * Use for critical error events that should be investigated.
     *
     * @param tag Optional tag to identify the log source. If null, uses Timber's default.
     * @param message The message to log.
     * @param throwable The exception or error to log with full stack trace.
     */
    fun e(tag: String? = null, message: String, throwable: Throwable) {
        if (tag != null) {
            Timber.tag(tag).e(throwable, message)
        } else {
            Timber.e(throwable, message)
        }
    }

    /**
     * Log an exception with full stack trace.
     *
     * @param tag Optional tag to identify the log source. If null, uses Timber's default.
     * @param throwable The exception or error to log.
     */
    fun e(tag: String? = null, throwable: Throwable) {
        if (tag != null) {
            Timber.tag(tag).e(throwable, "Exception occurred")
        } else {
            Timber.e(throwable, "Exception occurred")
        }
    }
}

/**
 * Debug Tips:
 *
 * Using Logcat in Android Studio:
 * 1. Open Android Studio
 * 2. Go to View -> Tool Windows -> Logcat (or press Alt+6 on Windows/Linux, Opt+6 on Mac)
 * 3. Filter logs by tag using the search bar (e.g., "tag:MainActivity")
 * 4. Filter by log level (Debug, Info, Warning, Error) using the dropdown
 * 5. Right-click on a log entry to create a filter for that tag
 *
 * Common Logcat filters:
 * - "tag:TaskRepository" - Show only logs from TaskRepository
 * - "package:com.example.task12_13" - Show only logs from this app
 * - "level:error" - Show only error level logs
 *
 * Using the Android Studio Debugger:
 * 1. Click in the gutter next to a line number to set a breakpoint (red dot appears)
 * 2. Click the Debug icon (bug icon) or press Shift+F9 to start debugging
 * 3. Use the Debug window to:
 *    - Step over (F8) - Execute current line and move to next
 *    - Step into (F7) - Enter function calls
 *    - Step out (Shift+F8) - Exit current function
 *    - Resume (F9) - Continue execution
 * 4. Inspect variables in the Variables panel
 * 5. Add watches to monitor specific expressions
 */
