package com.example.task12_13.model

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the Task model
 */
class TaskTest {

    private lateinit var testTask: Task

    @Before
    fun setup() {
        testTask = Task(
            id = "test-id-123",
            title = "Test Task",
            description = "This is a test task description",
            completed = false,
            createdAt = 1234567890L
        )
    }

    @Test
    fun taskCreation_withAllParameters_createsTaskCorrectly() {
        assertThat(testTask.id).isEqualTo("test-id-123")
        assertThat(testTask.title).isEqualTo("Test Task")
        assertThat(testTask.description).isEqualTo("This is a test task description")
        assertThat(testTask.completed).isFalse()
        assertThat(testTask.createdAt).isEqualTo(1234567890L)
    }

    @Test
    fun taskCreation_withDefaultValues_usesCorrectDefaults() {
        val task = Task()

        assertThat(task.id).isEmpty()
        assertThat(task.title).isEmpty()
        assertThat(task.description).isEmpty()
        assertThat(task.completed).isFalse()
        assertThat(task.createdAt).isAtLeast(System.currentTimeMillis() - 1000)
    }

    @Test
    fun taskCreation_withPartialParameters_usesDefaultsForUnspecified() {
        val task = Task(
            id = "partial-id",
            title = "Partial Task"
        )

        assertThat(task.id).isEqualTo("partial-id")
        assertThat(task.title).isEqualTo("Partial Task")
        assertThat(task.description).isEmpty()
        assertThat(task.completed).isFalse()
        assertThat(task.createdAt).isAtLeast(System.currentTimeMillis() - 1000)
    }

    @Test
    fun toMap_containsAllFields() {
        val map = testTask.toMap()

        assertThat(map).hasSize(5)
        assertThat(map["id"]).isEqualTo("test-id-123")
        assertThat(map["title"]).isEqualTo("Test Task")
        assertThat(map["description"]).isEqualTo("This is a test task description")
        assertThat(map["completed"]).isEqualTo(false)
        assertThat(map["createdAt"]).isEqualTo(1234567890L)
    }

    @Test
    fun toMap_withEmptyTask_returnsMapWithEmptyValues() {
        val task = Task()
        val map = task.toMap()

        assertThat(map["id"]).isEqualTo("")
        assertThat(map["title"]).isEqualTo("")
        assertThat(map["description"]).isEqualTo("")
        assertThat(map["completed"]).isEqualTo(false)
        assertThat(map["createdAt"]).isInstanceOf(Long::class.java)
    }

    @Test
    fun fromMap_withValidMap_createsTaskCorrectly() {
        val map = mapOf(
            "id" to "map-id-456",
            "title" to "Task From Map",
            "description" to "Created from map",
            "completed" to true,
            "createdAt" to 9876543210L
        )

        val task = Task.fromMap(map)

        assertThat(task.id).isEqualTo("map-id-456")
        assertThat(task.title).isEqualTo("Task From Map")
        assertThat(task.description).isEqualTo("Created from map")
        assertThat(task.completed).isTrue()
        assertThat(task.createdAt).isEqualTo(9876543210L)
    }

    @Test
    fun fromMap_withNullMap_returnsTaskWithDefaults() {
        val task = Task.fromMap(null)

        assertThat(task.id).isEmpty()
        assertThat(task.title).isEmpty()
        assertThat(task.description).isEmpty()
        assertThat(task.completed).isFalse()
        assertThat(task.createdAt).isAtLeast(System.currentTimeMillis() - 1000)
    }

    @Test
    fun fromMap_withPartialMap_usesDefaultsForMissingFields() {
        val map = mapOf(
            "id" to "partial-map-id",
            "title" to "Partial Map Task"
        )

        val task = Task.fromMap(map)

        assertThat(task.id).isEqualTo("partial-map-id")
        assertThat(task.title).isEqualTo("Partial Map Task")
        assertThat(task.description).isEmpty()
        assertThat(task.completed).isFalse()
        assertThat(task.createdAt).isAtLeast(System.currentTimeMillis() - 1000)
    }

    @Test
    fun fromMap_withInvalidTypes_usesDefaults() {
        val map = mapOf(
            "id" to 123, // Wrong type
            "title" to true, // Wrong type
            "description" to listOf("items"), // Wrong type
            "completed" to "not-a-boolean", // Wrong type
            "createdAt" to "not-a-long" // Wrong type
        )

        val task = Task.fromMap(map)

        assertThat(task.id).isEmpty() // Default due to wrong type
        assertThat(task.title).isEmpty() // Default due to wrong type
        assertThat(task.description).isEmpty() // Default due to wrong type
        assertThat(task.completed).isFalse() // Default due to wrong type
        assertThat(task.createdAt).isAtLeast(System.currentTimeMillis() - 1000) // Default due to wrong type
    }

    @Test
    fun dataClass_copy_worksCorrectly() {
        val copiedTask = testTask.copy(title = "Copied Task")

        assertThat(copiedTask.id).isEqualTo(testTask.id)
        assertThat(copiedTask.title).isEqualTo("Copied Task")
        assertThat(copiedTask.description).isEqualTo(testTask.description)
        assertThat(copiedTask.completed).isEqualTo(testTask.completed)
        assertThat(copiedTask.createdAt).isEqualTo(testTask.createdAt)
    }

    @Test
    fun dataClass_equals_returnsTrueForSameValues() {
        val task1 = Task(id = "same-id", title = "Same Title")
        val task2 = Task(id = "same-id", title = "Same Title")

        assertThat(task1).isEqualTo(task2)
    }

    @Test
    fun dataClass_equals_returnsFalseForDifferentValues() {
        val task1 = Task(id = "id-1", title = "Title 1")
        val task2 = Task(id = "id-2", title = "Title 2")

        assertThat(task1).isNotEqualTo(task2)
    }

    @Test
    fun task_toggleCompleted_togglesStatus() {
        val incompleteTask = testTask.copy(completed = false)
        val completedTask = incompleteTask.copy(completed = true)

        assertThat(incompleteTask.completed).isFalse()
        assertThat(completedTask.completed).isTrue()
    }
}
