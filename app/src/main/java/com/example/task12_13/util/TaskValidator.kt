package com.example.task12_13.util

import com.example.task12_13.model.Task

/**
 * Utility class for validating Task data
 */
object TaskValidator {

    /**
     * Result of task validation
     */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }

    private const val MIN_TITLE_LENGTH = 1
    private const val MAX_TITLE_LENGTH = 100
    private const val MAX_DESCRIPTION_LENGTH = 500

    /**
     * Validates a task object
     * @param task The task to validate
     * @return ValidationResult indicating if the task is valid or not
     */
    fun validateTask(task: Task): ValidationResult {
        return when {
            task.title.isBlank() -> ValidationResult.Invalid("Title cannot be empty")
            task.title.length < MIN_TITLE_LENGTH -> ValidationResult.Invalid("Title must be at least $MIN_TITLE_LENGTH character")
            task.title.length > MAX_TITLE_LENGTH -> ValidationResult.Invalid("Title cannot exceed $MAX_TITLE_LENGTH characters")
            task.description.length > MAX_DESCRIPTION_LENGTH -> ValidationResult.Invalid("Description cannot exceed $MAX_DESCRIPTION_LENGTH characters")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates a task title
     * @param title The title to validate
     * @return ValidationResult indicating if the title is valid or not
     */
    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.Invalid("Title cannot be empty")
            title.length < MIN_TITLE_LENGTH -> ValidationResult.Invalid("Title must be at least $MIN_TITLE_LENGTH character")
            title.length > MAX_TITLE_LENGTH -> ValidationResult.Invalid("Title cannot exceed $MAX_TITLE_LENGTH characters")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates a task description
     * @param description The description to validate
     * @return ValidationResult indicating if the description is valid or not
     */
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.length > MAX_DESCRIPTION_LENGTH -> ValidationResult.Invalid("Description cannot exceed $MAX_DESCRIPTION_LENGTH characters")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Checks if a task is valid
     * @param task The task to check
     * @return true if valid, false otherwise
     */
    fun isValid(task: Task): Boolean {
        return validateTask(task) == ValidationResult.Valid
    }

    /**
     * Checks if a title is valid
     * @param title The title to check
     * @return true if valid, false otherwise
     */
    fun isTitleValid(title: String): Boolean {
        return validateTitle(title) == ValidationResult.Valid
    }
}
