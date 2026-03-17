package com.example.task12_13.repository

import com.example.task12_13.model.Task
import com.example.task12_13.util.Logger
import com.example.task12_13.util.DebugConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository class for managing Task data operations with Firestore.
 * Implements singleton pattern for easy access throughout the application.
 *
 * Debug Tips:
 * - Set breakpoints in each method to track data flow
 * - Use Logger output in Logcat to trace operations
 * - Filter Logcat by "TaskRepository" tag to see all repository logs
 * - Use the debugger's "Evaluate Expression" to test Firestore queries
 * - Check Network Inspector in Android Studio to see Firestore requests
 *
 * @property firestore The FirebaseFirestore instance for database operations
 */
class TaskRepository private constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        /**
         * Firestore collection name for tasks.
         * Change this to use a different collection in Firestore.
         */
        private const val COLLECTION_NAME = "tasks"

        /**
         * Tag for logging.
         * Use this tag in Logcat to filter repository logs: tag:TaskRepository
         */
        private const val TAG = "TaskRepository"

        @Volatile
        private var INSTANCE: TaskRepository? = null

        /**
         * Gets the singleton instance of TaskRepository
         * @param firestore FirebaseFirestore instance (optional, uses default if not provided)
         * @return TaskRepository singleton instance
         */
        fun getInstance(firestore: FirebaseFirestore = FirebaseFirestore.getInstance()): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(firestore).also {
                    INSTANCE = it
                    Logger.i(TAG, "TaskRepository singleton instance created")
                }
            }
        }
    }

    private val tasksCollection = firestore.collection(COLLECTION_NAME)

    init {
        Logger.i(TAG, "TaskRepository initialized")
        DebugConfig.logDebug(TAG, "Firestore instance: ${firestore.app.name}")
    }

    /**
     * Adds a new task to Firestore.
     *
     * Debug:
     * - Set breakpoint at try block to trace task creation
     * - Check Network Inspector to see the Firestore write operation
     * - Verify the new document appears in Firestore Console
     *
     * @param task The Task object to add
     * @return Result<String> containing the new task ID or error
     */
    suspend fun addTask(task: Task): Result<String> = suspendCancellableCoroutine { continuation ->
        Logger.i(TAG, "Starting addTask operation for task: ${task.title}")
        DebugConfig.logDebug(TAG, "Task details: title='${task.title}', description='${task.description}'")

        if (!task.isValid()) {
            val error = IllegalStateException("Cannot add invalid task: title is blank")
            Logger.e(TAG, error.message, error)
            continuation.resume(Result.failure(error))
            return@suspendCancellableCoroutine
        }

        val newTaskRef = tasksCollection.document()
        val taskWithId = task.copy(id = newTaskRef.id)
        Logger.d(TAG, "Creating new document with ID: ${newTaskRef.id}")

        newTaskRef.set(taskWithId.toMap())
            .addOnSuccessListener {
                Logger.i(TAG, "Task created successfully with ID: ${taskWithId.id}")
                DebugConfig.logDebug(TAG, "Created task: $taskWithId")
                continuation.resume(Result.success(taskWithId.id))
            }
            .addOnFailureListener { exception ->
                Logger.e(TAG, "Failed to add task: ${task.title}", exception)
                Logger.e(TAG, "Error type: ${exception.javaClass.simpleName}, message: ${exception.message}")
                continuation.resume(Result.failure(exception))
            }
            .addOnCanceledListener {
                Logger.w(TAG, "addTask operation was cancelled")
                continuation.cancel()
            }
    }

    /**
     * Retrieves all tasks from Firestore.
     *
     * Debug:
     * - Set breakpoint to trace task loading
     * - Check querySnapshot size in debugger
     * - Use Logcat to verify data received
     *
     * @return Result<List<Task>> containing list of tasks or error
     */
    suspend fun getAllTasks(): Result<List<Task>> = suspendCancellableCoroutine { continuation ->
        Logger.d(TAG, "Fetching all tasks from Firestore")

        tasksCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                Logger.d(TAG, "Received ${querySnapshot.documents.size} documents from Firestore")
                val tasks = querySnapshot.documents.mapNotNull { document ->
                    try {
                        val task = Task.fromMap(document.data)
                        task.id = document.id
                        DebugConfig.logDebug(TAG, "Parsed task: ${task.title} (id: ${task.id})")
                        task
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error parsing task document ${document.id}", e)
                        null
                    }
                }
                Logger.i(TAG, "Successfully fetched ${tasks.size} tasks")
                continuation.resume(Result.success(tasks))
            }
            .addOnFailureListener { exception ->
                Logger.e(TAG, "Failed to fetch tasks from Firestore", exception)
                continuation.resume(Result.failure(exception))
            }
            .addOnCanceledListener {
                Logger.w(TAG, "getAllTasks operation was cancelled")
                continuation.cancel()
            }
    }

    /**
     * Deletes a task from Firestore.
     *
     * Debug:
     * - Set breakpoint to trace deletion operations
     * - Verify document is removed in Firestore Console
     * - Check Network Inspector for the delete request
     *
     * @param taskId The ID of the task to delete
     * @return Result<Unit> indicating success or failure
     */
    suspend fun deleteTask(taskId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        Logger.i(TAG, "Starting deleteTask operation for task ID: $taskId")
        DebugConfig.logDebug(TAG, "Deleting task from Firestore")

        if (taskId.isEmpty()) {
            val error = IllegalArgumentException("Task ID cannot be empty")
            Logger.e(TAG, error.message, error)
            continuation.resume(Result.failure(error))
            return@suspendCancellableCoroutine
        }

        tasksCollection.document(taskId)
            .delete()
            .addOnSuccessListener {
                Logger.i(TAG, "Task deleted successfully: $taskId")
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Logger.e(TAG, "Failed to delete task: $taskId", exception)
                Logger.e(TAG, "Error type: ${exception.javaClass.simpleName}, message: ${exception.message}")
                continuation.resume(Result.failure(exception))
            }
            .addOnCanceledListener {
                Logger.w(TAG, "deleteTask operation was cancelled")
                continuation.cancel()
            }
    }

    /**
     * Updates an existing task in Firestore.
     *
     * Debug:
     * - Set breakpoint at the update operation to trace changes
     * - Check Network Inspector for the Firestore update request
     * - Verify changes in Firestore Console
     *
     * @param task The Task object with updated fields
     * @return Result<Unit> indicating success or failure
     */
    suspend fun updateTask(task: Task): Result<Unit> = suspendCancellableCoroutine { continuation ->
        Logger.i(TAG, "Starting updateTask operation for task ID: ${task.id}")
        DebugConfig.logDebug(TAG, "Update details: title='${task.title}', completed=${task.completed}")

        if (task.id.isEmpty()) {
            val error = IllegalArgumentException("Task ID cannot be empty")
            Logger.e(TAG, error.message, error)
            continuation.resume(Result.failure(error))
            return@suspendCancellableCoroutine
        }

        if (!task.isValid()) {
            val error = IllegalStateException("Cannot update invalid task: title is blank")
            Logger.e(TAG, error.message, error)
            continuation.resume(Result.failure(error))
            return@suspendCancellableCoroutine
        }

        Logger.d(TAG, "Updating document ${task.id} in Firestore")
        tasksCollection.document(task.id)
            .set(task.toMap())
            .addOnSuccessListener {
                Logger.i(TAG, "Task updated successfully: ${task.id}")
                DebugConfig.logDebug(TAG, "Updated task: $task")
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Logger.e(TAG, "Failed to update task: ${task.id}", exception)
                Logger.e(TAG, "Error type: ${exception.javaClass.simpleName}, message: ${exception.message}")
                continuation.resume(Result.failure(exception))
            }
            .addOnCanceledListener {
                Logger.w(TAG, "updateTask operation was cancelled")
                continuation.cancel()
            }
    }

    /**
     * Gets a single task by ID from Firestore.
     *
     * Debug:
     * - Set breakpoint to trace individual task fetches
     * - Use "Evaluate Expression" to test with different IDs
     *
     * @param taskId The ID of the task to retrieve
     * @return Result<Task> containing the task or error
     */
    suspend fun getTaskById(taskId: String): Result<Task> = suspendCancellableCoroutine { continuation ->
        Logger.d(TAG, "Fetching task by ID: $taskId")

        if (taskId.isEmpty()) {
            val error = IllegalArgumentException("Task ID cannot be empty")
            Logger.e(TAG, error.message, error)
            continuation.resume(Result.failure(error))
            return@suspendCancellableCoroutine
        }

        tasksCollection.document(taskId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val task = Task.fromMap(documentSnapshot.data)
                    task.id = documentSnapshot.id
                    Logger.i(TAG, "Task fetched successfully: ${task.title}")
                    DebugConfig.logDebug(TAG, "Fetched task: $task")
                    continuation.resume(Result.success(task))
                } else {
                    val error = NoSuchElementException("Task not found with ID: $taskId")
                    Logger.w(TAG, error.message)
                    continuation.resume(Result.failure(error))
                }
            }
            .addOnFailureListener { exception ->
                Logger.e(TAG, "Failed to fetch task: $taskId", exception)
                continuation.resume(Result.failure(exception))
            }
            .addOnCanceledListener {
                Logger.w(TAG, "getTaskById operation was cancelled")
                continuation.cancel()
            }
    }

    /**
     * Adds a snapshot listener to observe real-time changes to the tasks collection.
     *
     * Debug:
     * - Set breakpoint in the listener to trace real-time updates
     * - Check snapshot metadata in debugger (fromCache, hasPendingWrites)
     * - Use Logcat to track when updates arrive
     *
     * @param onResult Callback function with Result<List<Task>>
     * @return ListenerRegistration that can be used to remove the listener
     */
    fun addTasksSnapshotListener(
        onResult: (Result<List<Task>>) -> Unit
    ): ListenerRegistration {
        Logger.d(TAG, "Starting real-time task listener")

        return tasksCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Logger.e(TAG, "Error in snapshot listener", exception)
                onResult(Result.failure(exception))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Logger.d(TAG, "Received snapshot with ${snapshot.documents.size} documents")
                DebugConfig.logDebug(TAG, "Snapshot metadata: fromCache=${snapshot.metadata.isFromCache}")

                val tasks = snapshot.documents.mapNotNull { document ->
                    try {
                        val task = Task.fromMap(document.data)
                        task.id = document.id
                        DebugConfig.logDebug(TAG, "Parsed task: ${task.title} (id: ${task.id})")
                        task
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error parsing task document ${document.id}", e)
                        null
                    }
                }
                Logger.d(TAG, "Emitting ${tasks.size} tasks to callback")
                onResult(Result.success(tasks))
            }
        }
    }

    /**
     * Returns a Flow that emits task list updates in real-time.
     *
     * Debug:
     * - Set breakpoint at callbackFlow start to trace snapshot creation
     * - Check "snapshot" variable to see Firestore query results
     * - Use Logcat filter: "tag:TaskRepository" to see loading/update logs
     *
     * @return Flow<Result<List<Task>>> emitting task list updates
     */
    fun getTasksAsFlow(): Flow<Result<List<Task>>> = callbackFlow {
        Logger.d(TAG, "Starting real-time task flow")

        val listenerRegistration = tasksCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Logger.e(TAG, "Error in flow snapshot listener", exception)
                trySend(Result.failure(exception))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Logger.d(TAG, "Flow received snapshot with ${snapshot.documents.size} documents")

                val tasks = snapshot.documents.mapNotNull { document ->
                    try {
                        val task = Task.fromMap(document.data)
                        task.id = document.id
                        DebugConfig.logDebug(TAG, "Flow parsed task: ${task.title} (id: ${task.id})")
                        task
                    } catch (e: Exception) {
                        Logger.e(TAG, "Flow error parsing task document ${document.id}", e)
                        null
                    }
                }
                Logger.d(TAG, "Flow emitting ${tasks.size} tasks")
                trySend(Result.success(tasks))
            }
        }

        awaitClose {
            Logger.d(TAG, "Closing flow snapshot listener")
            listenerRegistration.remove()
        }
    }

    /**
     * Toggle the completed status of a task.
     * Convenience method for common UI operation.
     *
     * Debug:
     * - Set breakpoint to track toggle operations
     * - Check the before/after values in the debugger
     *
     * @param task The task to toggle
     * @return Result<Unit> indicating success or failure
     */
    suspend fun toggleTaskCompleted(task: Task): Result<Unit> {
        Logger.i(TAG, "Toggling completed status for task: ${task.id}")
        DebugConfig.logDebug(TAG, "Current completed status: ${task.completed}")

        val updatedTask = task.copy(completed = !task.completed)
        Logger.d(TAG, "New completed status will be: ${updatedTask.completed}")

        return updateTask(updatedTask)
    }
}

/**
 * Additional Debugging Tips for TaskRepository:
 *
 * Logcat Filters:
 * - "tag:TaskRepository" - See all repository logs
 * - "tag:TaskRepository level:error" - See only errors
 * - "tag:Performance" - See operation timing measurements
 *
 * Common Issues and Debugging:
 *
 * 1. Tasks not appearing in UI:
 *    - Check Logcat for "getTasks" or "getAllTasks" logs
 *    - Verify Firestore has documents in the "tasks" collection
 *    - Set breakpoint in snapshot listener to check if snapshots are received
 *
 * 2. Add/Update/Delete not working:
 *    - Check Logcat for error logs with stack traces
 *    - Verify Firestore security rules allow writes
 *    - Check Network Inspector for failed requests
 *
 * 3. Real-time updates not working:
 *    - Verify listener registration is active
 *    - Check that the Flow is being collected
 *    - Look for "Closing" logs in Logcat
 *
 * Using Android Studio Debugger:
 * - Step through repository operations to understand data flow
 * - Add watches on: task, task.id, snapshot.documents
 * - Use conditional breakpoints: task.id == "specific-id"
 * - Use "Resume Program" to skip to the next important operation
 *
 * Network Inspection:
 * - Open Android Studio -> View -> Tool Windows -> Profiler
 * - Select your app process
 * - Click on the "Network" section
 * - Look for Firestore requests and their status codes
 */
