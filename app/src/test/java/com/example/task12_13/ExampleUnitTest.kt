package com.example.task12_13

import com.example.task12_13.model.Task
import com.example.task12_13.util.TaskValidator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * These tests demonstrate basic testing patterns for the Task management app.
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    /**
     * Demonstrates basic arithmetic testing
     */
    @Test
    fun addition_isCorrect() {
        assertThat(2 + 2).isEqualTo(4)
    }

    /**
     * Tests Task model creation with default values
     */
    @Test
    fun task_withDefaultValues_hasCorrectDefaults() {
        val task = Task()
        assertThat(task.title).isEmpty()
        assertThat(task.description).isEmpty()
        assertThat(task.completed).isFalse()
    }

    /**
     * Tests Task toMap conversion
     */
    @Test
    fun task_toMap_containsExpectedFields() {
        val task = Task(
            id = "test-1",
            title = "Sample Task",
            description = "Sample Description"
        )
        val map = task.toMap()

        assertThat(map).containsKey("id")
        assertThat(map).containsKey("title")
        assertThat(map).containsKey("description")
        assertThat(map["title"]).isEqualTo("Sample Task")
    }

    /**
     * Tests TaskValidator with valid task
     */
    @Test
    fun taskValidator_withValidTask_returnsValid() {
        val validTask = Task(
            id = "valid-id",
            title = "Valid Task Title",
            description = "Valid description"
        )
        val result = TaskValidator.validateTask(validTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Valid::class.java)
    }

    /**
     * Tests TaskValidator with invalid (empty title) task
     */
    @Test
    fun taskValidator_withEmptyTitle_returnsInvalid() {
        val invalidTask = Task(title = "")
        val result = TaskValidator.validateTask(invalidTask)

        assertThat(result).isInstanceOf(TaskValidator.ValidationResult.Invalid::class.java)
    }

    /**
     * Tests timestamp generation
     */
    @Test
    fun task_createdAt_hasRecentTimestamp() {
        val beforeCreation = System.currentTimeMillis()
        val task = Task()
        val afterCreation = System.currentTimeMillis()

        assertThat(task.createdAt).isAtLeast(beforeCreation)
        assertThat(task.createdAt).isAtMost(afterCreation)
    }

    /**
     * Tests Task data class equality
     */
    @Test
    fun task_withSameProperties_areEqual() {
        val task1 = Task(id = "same-id", title = "Same Title")
        val task2 = Task(id = "same-id", title = "Same Title")

        assertThat(task1).isEqualTo(task2)
    }

    /**
     * Tests Task data class copy
     */
    @Test
    fun task_copy_createsNewInstanceWithSameValues() {
        val original = Task(id = "original", title = "Original")
        val copy = original.copy()

        assertThat(copy).isEqualTo(original)
        assertThat(copy).isNotSameInstanceAs(original)
    }
}