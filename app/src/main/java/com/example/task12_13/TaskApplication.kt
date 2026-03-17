package com.example.task12_13

import android.app.Application
import com.example.task12_13.repository.TaskRepository
import com.example.task12_13.util.Logger
import com.example.task12_13.util.DebugConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import timber.log.Timber

/**
 * Custom Application class for initialization tasks.
 *
 * This class is responsible for initializing app-wide components:
 * - Timber logging library
 * - Firebase Firestore with optimized settings
 * - TaskRepository singleton
 *
 * Debug Tips:
 * - Set breakpoint in onCreate() to trace app initialization
 * - Check Timber is planted before logging
 * - Verify Firestore settings are applied
 *
 * Remember to register this in AndroidManifest.xml:
 * <application
 *     android:name=".TaskApplication"
 *     ... >
 */
class TaskApplication : Application() {

    companion object {
        /**
         * Tag for logging.
         * Use this tag in Logcat to filter application logs: tag:TaskApplication
         */
        private const val TAG = "TaskApplication"

        lateinit var instance: TaskApplication
            private set

        val taskRepository: TaskRepository by lazy {
            TaskRepository.getInstance()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Logger.i(TAG, "Application onCreate started")
        DebugConfig.logDebug(TAG, "Process: ${android.os.Process.myPid()}")

        // Initialize Firebase Firestore with settings
        initializeFirestore()

        // Initialize Timber for logging
        initializeTimber()

        Logger.i(TAG, "Application initialization completed")
    }

    /**
     * Configure Firestore settings for better performance and offline support.
     *
     * Debug:
     * - Set breakpoint to trace Firestore initialization
     * - Check settings are applied correctly
     * - Verify persistence is enabled
     */
    private fun initializeFirestore() {
        Logger.d(TAG, "Initializing Firestore with custom settings")

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings

        Logger.i(TAG, "Firestore initialized with persistence enabled")
        DebugConfig.logDebug(TAG, "Cache size: UNLIMITED")
    }

    /**
     * Initialize Timber logging library.
     * Plant debug tree in debug builds only.
     *
     * Debug:
     * - After this runs, Timber will log to Logcat
     * - Check for "Timber logging initialized" message
     */
    private fun initializeTimber() {
        Logger.d(TAG, "Initializing Timber logging")

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Logger.i(TAG, "Timber logging initialized")
            DebugConfig.logDebug(TAG, "Debug tree planted - logs will appear in Logcat")
        } else {
            Logger.w(TAG, "Release build - Timber debug tree not planted")
        }
    }

    /**
     * Get the singleton instance of TaskRepository.
     *
     * @return TaskRepository instance
     */
    fun getTaskRepository(): TaskRepository {
        return taskRepository
    }
}

/**
 * Debugging Tips for TaskApplication:
 *
 * Logcat Filters:
 * - "tag:TaskApplication" - See all application logs
 * - "Timber" - See Timber-specific logs
 * - "Firestore" - See Firestore initialization logs
 *
 * Common Issues and Debugging:
 *
 * 1. Timber not logging:
 *    - Check BuildConfig.DEBUG is true
 *    - Verify initializeTimber() is called
 *    - Look for "Timber logging initialized" in Logcat
 *
 * 2. Firestore not working:
 *    - Check google-services.json exists
 *    - Verify Firestore initialization logs
 *    - Check Network Inspector for connection issues
 *
 * 3. App crashes on start:
 *    - Set breakpoint in onCreate()
 *    - Step through initialization
 *    - Check exception stack trace
 *
 * Using Android Studio Debugger:
 * - Set breakpoint in onCreate() at super.onCreate()
 * - Step through each initialization method
 * - Add watches on: settings, BuildConfig.DEBUG
 *
 * Application Lifecycle:
 * - onCreate: Called when app is first launched
 * - onTerminate: Called when app is ending (never on production devices)
 * - onLowMemory: Called when system is low on memory
 * - onConfigurationChanged: Called when configuration changes
 */
