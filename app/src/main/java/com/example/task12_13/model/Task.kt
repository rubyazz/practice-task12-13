package com.example.task12_13.model

import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a Task in the application.
 *
 * This model class represents a task that can be stored in Firestore
 * and displayed in the UI.
 *
 * Debug Tips:
 * - Use data classes' built-in toString() for logging: Logger.d("Task", task.toString())
 * - Set breakpoints on property access to track when values are read
 * - Use "Evaluate Expression" debugger feature to create test tasks:
 *   Task(id="test", title="Test Task", description="Debug task", completed=false)
 *
 * @property id Unique identifier for the task (empty string for new tasks before saving)
 * @property title The title/name of the task
 * @property description Detailed description of the task
 * @property completed Whether the task is completed (true) or not (false)
 * @property createdAt Timestamp when the task was created (milliseconds since epoch)
 */
data class Task(
    @PropertyName("id") var id: String = "",
    @PropertyName("title") var title: String = "",
    @PropertyName("description") var description: String = "",
    @PropertyName("completed") var completed: Boolean = false,
    @PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Empty constructor for Firestore deserialization
     */
    constructor() : this("", "", "", false, System.currentTimeMillis())

    /**
     * Converts the Task object to a Map for Firestore operations
     * @return Map<String, Any> representation of the Task
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "completed" to completed,
            "createdAt" to createdAt
        )
    }

    /**
     * Helper function to check if this is a new task (not yet saved to Firestore).
     * A task is considered new if it has an empty ID.
     *
     * Debug: Watch this property in the debugger to track task state changes
     */
    fun isNew(): Boolean = id.isEmpty()

    /**
     * Returns a display-friendly status string.
     * Useful for debugging and UI display.
     *
     * Debug: Add this to your watches in the debugger to see task status at a glance
     */
    fun getStatusText(): String = if (completed) "Completed" else "Active"

    /**
     * Validation helper to check if the task has valid data.
     * A task is valid if it has a non-empty title.
     *
     * Use this before saving to Firestore to ensure data integrity.
     * Debug: Set conditional breakpoint on this returning false to catch invalid saves
     */
    fun isValid(): Boolean = title.isNotBlank()

    /**
     * Creates a Task object from a Firestore document snapshot map
     * @param map The map from Firestore document
     * @return Task object
     */
    companion object {
        fun fromMap(map: Map<String, Any>?): Task {
            return Task(
                id = map?.get("id") as? String ?: "",
                title = map?.get("title") as? String ?: "",
                description = map?.get("description") as? String ?: "",
                completed = map?.get("completed") as? Boolean ?: false,
                createdAt = (map?.get("createdAt") as? Long) ?: System.currentTimeMillis()
            )
        }
    }
}
