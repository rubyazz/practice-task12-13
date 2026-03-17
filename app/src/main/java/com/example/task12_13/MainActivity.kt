package com.example.task12_13

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.task12_13.adapter.TaskAdapter
import com.example.task12_13.databinding.ActivityMainBinding
import com.example.task12_13.databinding.DialogAddTaskBinding
import com.example.task12_13.model.Task
import com.example.task12_13.repository.TaskRepository
import com.example.task12_13.util.Logger
import com.example.task12_13.util.DebugConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Main Activity for the Task Management application.
 * Displays a list of tasks and handles add/delete/update operations.
 *
 * Debug Tips:
 * - Set breakpoints in onCreate() to trace activity initialization
 * - Set breakpoints in user action handlers to track interactions
 * - Use Logcat filter: "tag:MainActivity" to see all activity logs
 * - Use Layout Inspector to view view hierarchy: Tools -> Layout Inspector
 *
 * Lifecycle Debugging:
 * - The lifecycle events (onCreate, onResume, onPause) are logged
 * - Use these logs to understand when the activity is active/inactive
 * - Check for memory leaks by monitoring onDestroy
 */
class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * Tag for logging.
         * Use this tag in Logcat to filter activity logs: tag:MainActivity
         */
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var repository: TaskRepository

    /**
     * Called when the activity is first created.
     * This is where initialization happens.
     *
     * Debug: Set breakpoint here to trace activity creation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "onCreate: Activity starting")
        DebugConfig.logDebug(TAG, "savedInstanceState: ${savedInstanceState != null}")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Logger.d(TAG, "View binding initialized")

        setupRepository()
        setupRecyclerView()
        setupFab()
        observeTasks()

        Logger.i(TAG, "onCreate: Setup completed")
    }

    /**
     * Called when the activity is visible and ready to interact with the user.
     *
     * Debug: Use this to track when the activity becomes active
     */
    override fun onResume() {
        super.onResume()
        Logger.i(TAG, "onResume: Activity is now active and interacting with user")
        DebugConfig.logDebug(TAG, "Task list size: ${adapter.itemCount}")
    }

    /**
     * Called when the activity is no longer in the foreground.
     *
     * Debug: Use this to track when the activity loses focus
     */
    override fun onPause() {
        super.onPause()
        Logger.i(TAG, "onPause: Activity is no longer in the foreground")
    }

    /**
     * Initialize TaskRepository.
     *
     * Debug: Set breakpoint to trace repository initialization
     */
    private fun setupRepository() {
        Logger.d(TAG, "Initializing TaskRepository")
        repository = TaskRepository.getInstance()
        Logger.d(TAG, "TaskRepository initialized successfully")
    }

    /**
     * Setup RecyclerView with adapter and layout manager.
     *
     * Debug: Set breakpoint to trace RecyclerView setup
     */
    private fun setupRecyclerView() {
        Logger.d(TAG, "Setting up RecyclerView")

        adapter = TaskAdapter(
            onDeleteClick = { task ->
                Logger.d(TAG, "Delete clicked for task: ${task.title}")
                showDeleteConfirmationDialog(task)
            },
            onCheckChanged = { task, isChecked ->
                Logger.d(TAG, "Checkbox changed for task: ${task.title}, checked: $isChecked")
                updateTaskStatus(task, isChecked)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }

        Logger.d(TAG, "RecyclerView setup completed")
    }

    /**
     * Setup FAB click listener to show add task dialog.
     *
     * Debug: Set breakpoint in onClick to trace FAB clicks
     */
    private fun setupFab() {
        Logger.d(TAG, "Setting up FAB")
        binding.fabAdd.setOnClickListener {
            Logger.i(TAG, "FAB clicked - showing add task dialog")
            showAddTaskDialog()
        }
    }

    /**
     * Observe tasks from Firestore using Flow.
     *
     * Debug:
     * - Set breakpoint in collect block to trace task updates
     * - Check the "tasks" variable to see received data
     * - Monitor loading state changes
     */
    private fun observeTasks() {
        Logger.i(TAG, "Starting to observe tasks from Firestore")
        showLoading(true)

        lifecycleScope.launch {
            try {
                repository.getTasksAsFlow().collect { result ->
                    result.onSuccess { tasks ->
                        Logger.i(TAG, "Tasks updated successfully: ${tasks.size} items")
                        DebugConfig.logDebug(TAG, "Task list: ${tasks.map { it.title }}")
                        adapter.submitList(tasks)
                        updateEmptyState(tasks.isEmpty())
                        showLoading(false)
                    }.onFailure { exception ->
                        Logger.e(TAG, "Error loading tasks from Firestore", exception)
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.error_loading_tasks),
                            Toast.LENGTH_SHORT
                        ).show()
                        showLoading(false)
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Exception in observeTasks", e)
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.error_loading_tasks),
                    Toast.LENGTH_SHORT
                ).show()
                showLoading(false)
            }
        }
    }

    /**
     * Show dialog to add a new task.
     *
     * Debug:
     * - Set breakpoint in positive button click to trace save attempts
     * - Check validation logic
     */
    private fun showAddTaskDialog() {
        Logger.i(TAG, "Showing add task dialog")
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_task_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.etTitle.text?.toString()?.trim() ?: ""
                val description = dialogBinding.etDescription.text?.toString()?.trim() ?: ""

                Logger.d(TAG, "Add task dialog - Save clicked, title: '$title'")

                if (title.isEmpty()) {
                    Logger.w(TAG, "Add task validation failed: title is empty")
                    Toast.makeText(this, R.string.title_required, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addTask(title, description)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                Logger.d(TAG, "Add task dialog cancelled")
            }
            .create()

        dialog.show()
    }

    /**
     * Add a new task to Firestore.
     *
     * Debug:
     * - Set breakpoint to trace task creation and save
     * - Check the task object before saving
     * - Monitor the result success/failure
     */
    private fun addTask(title: String, description: String) {
        Logger.i(TAG, "Adding new task - title: '$title', description: '$description'")
        showLoading(true)

        lifecycleScope.launch {
            try {
                val task = Task(
                    id = "",
                    title = title,
                    description = description,
                    completed = false,
                    createdAt = System.currentTimeMillis()
                )

                Logger.d(TAG, "Created task object: $task")

                repository.addTask(task)
                    .onSuccess { taskId ->
                        Logger.i(TAG, "Task added successfully with ID: $taskId")
                        Toast.makeText(
                            this@MainActivity,
                            R.string.task_added,
                            Toast.LENGTH_SHORT
                        ).show()
                        showLoading(false)
                    }
                    .onFailure { exception ->
                        Logger.e(TAG, "Error adding task to Firestore", exception)
                        Toast.makeText(
                            this@MainActivity,
                            R.string.error_adding_task,
                            Toast.LENGTH_SHORT
                        ).show()
                        showLoading(false)
                    }
            } catch (e: Exception) {
                Logger.e(TAG, "Exception in addTask", e)
                Toast.makeText(
                    this@MainActivity,
                    R.string.error_adding_task,
                    Toast.LENGTH_SHORT
                ).show()
                showLoading(false)
            }
        }
    }

    /**
     * Show confirmation dialog before deleting a task.
     *
     * Debug: Set breakpoint to trace delete confirmations
     */
    private fun showDeleteConfirmationDialog(task: Task) {
        Logger.i(TAG, "Showing delete confirmation for task: ${task.title}")
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_task)
            .setMessage("Are you sure you want to delete \"${task.title}\"?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                Logger.d(TAG, "Delete confirmed for task: ${task.title}")
                deleteTask(task)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                Logger.d(TAG, "Delete cancelled for task: ${task.title}")
            }
            .show()
    }

    /**
     * Delete a task from Firestore.
     *
     * Debug:
     * - Set breakpoint to trace delete operations
     * - Check the task ID before deletion
     * - Monitor the result success/failure
     */
    private fun deleteTask(task: Task) {
        Logger.i(TAG, "Deleting task: ${task.title} (ID: ${task.id})")
        showLoading(true)

        lifecycleScope.launch {
            try {
                repository.deleteTask(task.id)
                    .onSuccess {
                        Logger.i(TAG, "Task deleted successfully: ${task.title}")
                        Toast.makeText(
                            this@MainActivity,
                            R.string.task_deleted,
                            Toast.LENGTH_SHORT
                        ).show()
                        showLoading(false)
                    }
                    .onFailure { exception ->
                        Logger.e(TAG, "Error deleting task from Firestore", exception)
                        Toast.makeText(
                            this@MainActivity,
                            R.string.error_deleting_task,
                            Toast.LENGTH_SHORT
                        ).show()
                        showLoading(false)
                    }
            } catch (e: Exception) {
                Logger.e(TAG, "Exception in deleteTask", e)
                Toast.makeText(
                    this@MainActivity,
                    R.string.error_deleting_task,
                    Toast.LENGTH_SHORT
                ).show()
                showLoading(false)
            }
        }
    }

    /**
     * Update task completion status in Firestore.
     *
     * Debug:
     * - Set breakpoint to trace status updates
     * - Check the updatedTask object before saving
     * - Monitor for update failures
     */
    private fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        Logger.i(TAG, "Updating task status: ${task.title} -> completed: $isCompleted")

        lifecycleScope.launch {
            try {
                val updatedTask = task.copy(completed = isCompleted)
                Logger.d(TAG, "Updating task with new status: $updatedTask")

                repository.updateTask(updatedTask)
                    .onFailure { exception ->
                        Logger.e(TAG, "Error updating task status in Firestore", exception)
                        Toast.makeText(
                            this@MainActivity,
                            R.string.error_updating_task,
                            Toast.LENGTH_SHORT
                        ).show()
                        // Revert the checkbox state
                        adapter.notifyDataSetChanged()
                    }
                Logger.d(TAG, "Task status updated successfully: ${task.title}, completed: $isCompleted")
            } catch (e: Exception) {
                Logger.e(TAG, "Exception in updateTaskStatus", e)
                Toast.makeText(
                    this@MainActivity,
                    R.string.error_updating_task,
                    Toast.LENGTH_SHORT
                ).show()
                // Revert the checkbox state
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Show or hide the loading progress bar.
     *
     * Debug: Set breakpoint to trace loading state changes
     */
    private fun showLoading(show: Boolean) {
        DebugConfig.logDebug(TAG, "Loading state: $show")
        binding.progressBar.visibility = if (show) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }

    /**
     * Update empty state visibility.
     *
     * Debug: Set breakpoint to trace empty state changes
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        DebugConfig.logDebug(TAG, "Empty state: $isEmpty")
        binding.tvEmptyState.visibility = if (isEmpty) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }
}

/**
 * Additional Debugging Tips for MainActivity:
 *
 * Logcat Filters:
 * - "tag:MainActivity" - See all activity logs
 * - "tag:MainActivity level:error" - See only errors
 * - "tag:TaskApp_*" - See all app logs with the standard prefix
 *
 * Common Issues and Debugging:
 *
 * 1. Tasks not showing in UI:
 *    - Check Logcat for "observeTasks" logs
 *    - Verify adapter.submitList() is called with data
 *    - Set breakpoint in observeTasks onSuccess block
 *
 * 2. Add/Delete/Update not working:
 *    - Check Logcat for error logs with stack traces
 *    - Verify Firestore is initialized (check TaskApplication logs)
 *    - Check Network Inspector for failed requests
 *
 * 3. UI not responding:
 *    - Check if showLoading(false) is called after operations
 *    - Look for exceptions in coroutines
 *    - Verify lifecycleScope is active
 *
 * Using Android Studio Debugger:
 * - Set breakpoint in onCreate() to trace initialization
 * - Set breakpoints in user action handlers (addTask, deleteTask, etc.)
 * - Add watches on: task, adapter.itemCount, binding.progressBar.visibility
 * - Use conditional breakpoints: task.title == "specific title"
 *
 * Layout Inspection:
 * - Open Layout Inspector: Tools -> Layout Inspector
 * - Select your app process
 * - View the view hierarchy and attributes
 * - Check binding references are correct
 *
 * Breakpoint Strategy:
 * - onCreate: Trace activity initialization
 * - observeTasks collect: Trace data updates
 * - addTask/deleteTask/updateTask: Trace user actions
 * - showLoading: Trace UI state changes
 *
 * Variable Inspection:
 * - task: Check task properties before operations
 * - adapter.itemCount: Verify list size
 * - binding.*.visibility: Check UI state
 * - exception: Read error messages and stack traces
 */
