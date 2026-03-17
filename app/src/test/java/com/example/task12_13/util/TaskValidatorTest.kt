package com.example.task12_13.util

import com.example.task12_13.model.Task
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the TaskValidator utility class
 */
class TaskValidatorTest {

    private lateinit var validTask: Task

    @Before
    fun setup() {
        validTask = Task(
            id = "valid-task-id",
            title = "Valid Task Title",
            description = "This is a valid task description",
            completed = false
        )
    }

    @Test
    fun validateTask_withValidTask_returnsValid() {
        val result = TaskValidator.validateTask(validTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    @Test
    fun validateTask_withEmptyTitle_returnsInvalid() {
        val invalidTask = validTask.copy(title = "")
        val result = TaskValidator.validateTask(invalidTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        assertThat(invalidResult.message).isEqualTo("Title cannot be empty")
    }

    @Test
    fun validateTask_withBlankTitle_returnsInvalid() {
        val invalidTask = validTask.copy(title = "   ")
        val result = TaskValidator.validateTask(invalidTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        assertThat(invalidResult.message).isEqualTo("Title cannot be empty")
    }

    @Test
    fun validateTask_withTitleTooLong_returnsInvalid() {
        val invalidTask = validTask.copy(title = "a".repeat(101))
        val result = TaskValidator.validateTask(invalidTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        assertThat(invalidResult.message).isEqualTo("Title cannot exceed 100 characters")
    }

    @Test
    fun validateTask_withTitleAtMaxLength_returnsValid() {
        val validLongTitleTask = validTask.copy(title = "a".repeat(100))
        val result = TaskValidator.validateTask(validLongTitleTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    @Test
    fun validateTask_withDescriptionTooLong_returnsInvalid() {
        val invalidTask = validTask.copy(description = "a".repeat(501))
        val result = TaskValidator.validateTask(invalidTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        assertThat(invalidResult.message).isEqualTo("Description cannot exceed 500 characters")
    }

    @Test
    fun validateTask_withDescriptionAtMaxLength_returnsValid() {
        val validLongDescTask = validTask.copy(description = "a".repeat(500))
        val result = TaskValidator.validateTask(validLongDescTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    @Test
    fun validateTask_withEmptyDescription_returnsValid() {
        val validEmptyDescTask = validTask.copy(description = "")
        val result = TaskValidator.validateTask(validEmptyDescTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    @Test
    fun validateTitle_withValidTitle_returnsValid() {
        val result = TaskValidator.validateTitle("Valid Title")

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    @Test
    fun validateTitle_withEmptyTitle_returnsInvalid() {
        val result = TaskValidator.validateTitle("")

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        assertThat(invalidResult.message).isEqualTo("Title cannot be empty")
    }

    @Test
    fun validateTitle_withBlankTitle_returnsInvalid() {
        val result = TaskValidator.validateTitle("   ")

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        assertThat(invalidResult.message).isEqualTo("Title cannot be empty")
    }

    @Test
    fun validateTitle_withTitleTooLong_returnsInvalid() {
        val result = TaskValidator.validateTitle("a".repeat(101))

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        assertThat(invalidResult.message).isEqualTo("Title cannot exceed 100 characters")
    }

    @Test
    fun validateDescription_withValidDescription_returnsValid() {
        val result = TaskValidator.validateDescription("Valid description")

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    @Test
    fun validateDescription_withEmptyDescription_returnsValid() {
        val result = TaskValidator.validateDescription("")

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    @Test
    fun validateDescription_withDescriptionTooLong_returnsInvalid() {
        val result = TaskValidator.validateDescription("a".repeat(501))

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        assertThat(invalidResult.message).isEqualTo("Description cannot exceed 500 characters")
    }

    @Test
    fun isValid_withValidTask_returnsTrue() {
        val result = TaskValidator.isValid(validTask)

        assertThat(result).isTrue()
    }

    @Test
    fun isValid_withInvalidTask_returnsFalse() {
        val invalidTask = validTask.copy(title = "")
        val result = TaskValidator.isValid(invalidTask)

        assertThat(result).isFalse()
    }

    @Test
    fun isTitleValid_withValidTitle_returnsTrue() {
        val result = TaskValidator.isTitleValid("Valid Title")

        assertThat(result).isTrue()
    }

    @Test
    fun isTitleValid_withInvalidTitle_returnsFalse() {
        val result = TaskValidator.isTitleValid("")

        assertThat(result).isFalse()
    }

    @Test
    fun validateTask_withMultipleErrors_returnsFirstError() {
        val invalidTask = validTask.copy(
            title = "",
            description = "a".repeat(600)
        )
        val result = TaskValidator.validateTask(invalidTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
        val invalidResult = result as TaskValidator.ValidationResult.Invalid
        // Should return the first validation error (empty title)
        assertThat(invalidResult.message).isEqualTo("Title cannot be empty")
    }

    @Test
    fun validateTask_withSpecialCharactersInTitle_returnsValid() {
        val taskWithSpecialChars = validTask.copy(
            title = "Task with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?"
        )
        val result = TaskValidator.validateTask(taskWithSpecialChars)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    @Test
    fun validateTask_withUnicodeInTitle_returnsValid() {
        val taskWithUnicode = validTask.copy(
            title = "Task with unicode: 你好世界 🚀"
        )
        val result = TaskValidator.validateTask(taskWithUnicode)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }
}
