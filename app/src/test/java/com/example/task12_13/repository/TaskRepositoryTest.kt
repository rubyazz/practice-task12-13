package com.example.task12_13.repository

import com.example.task12_13.model.Task
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task as GoogleTask
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever

/**
 * Unit tests for the TaskRepository class
 */
class TaskRepositoryTest {

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockCollection: CollectionReference

    @Mock
    private lateinit var mockDocumentReference: DocumentReference

    @Mock
    private lateinit var mockQuerySnapshot: QuerySnapshot

    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    @Mock
    private lateinit var mockListenerRegistration: ListenerRegistration

    @Mock
    private lateinit var mockGoogleTask: GoogleTask<Void>

    private lateinit var taskRepository: TaskRepository

    private val testTask = Task(
        id = "test-task-id",
        title = "Test Task",
        description = "Test Description",
        completed = false,
        createdAt = 1234567890L
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Setup repository with singleton pattern bypass for testing
        taskRepository = TaskRepository(mockFirestore)

        // Setup common mock behaviors
        whenever(mockFirestore.collection("tasks")).thenReturn(mockCollection)
        whenever(mockCollection.document()).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.id).thenReturn("generated-task-id")
    }

    @Test
    fun addTask_success_returnsSuccessWithTaskId() = runTest {
        // Arrange
        val expectedTaskId = "test-task-id"
        whenever(mockDocumentReference.id).thenReturn(expectedTaskId)

        // Mock the set operation to succeed immediately
        val captor = ArgumentCaptor.forClass(OnSuccessListener::class.java)
        doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
            listener.onSuccess(null)
            mockGoogleTask
        }.`when`(mockDocumentReference).set(any Map::class.java)
        whenever(mockGoogleTask.addOnSuccessListener(any())).thenReturn(mockGoogleTask)
        whenever(mockGoogleTask.addOnFailureListener(any())).thenReturn(mockGoogleTask)
        whenever(mockGoogleTask.addOnCanceledListener(any())).thenReturn(mockGoogleTask)

        // Act
        val result = taskRepository.addTask(testTask.copy(id = ""))

        // Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedTaskId)
    }

    @Test
    fun addTask_failure_returnsFailure() = runTest {
        // Arrange
        val expectedException = RuntimeException("Firestore error")
        doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
            // Don't call onSuccess, we'll verify failure instead
            mockGoogleTask
        }.`when`(mockDocumentReference).set(any Map::class.java)

        // Mock to call failure listener
        whenever(mockGoogleTask.addOnSuccessListener(any())).thenAnswer {
            val task = mock<GoogleTask<Void>>()
            whenever(task.addOnFailureListener(any())).thenAnswer {
                val failureListener = it.getArgument<com.google.android.gms.tasks.OnFailureListener>(0)
                failureListener.onFailure(expectedException)
                task
            }
            whenever(task.addOnCanceledListener(any())).thenReturn(task)
            task
        }
        whenever(mockDocumentReference.set(any Map::class.java)).thenReturn(mockGoogleTask)

        // Act
        val result = taskRepository.addTask(testTask.copy(id = ""))

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Firestore error")
    }

    @Test
    fun getAllTasks_success_returnsListOfTasks() = runTest {
        // Arrange
        val expectedTasks = listOf(
            testTask,
            testTask.copy(id = "task-2", title = "Second Task")
        )

        // Mock document snapshots
        val mockDoc1 = mock<DocumentSnapshot>()
        whenever(mockDoc1.data).thenReturn(mapOf(
            "id" to "test-task-id",
            "title" to "Test Task",
            "description" to "Test Description",
            "completed" to false,
            "createdAt" to 1234567890L
        ))

        val mockDoc2 = mock<DocumentSnapshot>()
        whenever(mockDoc2.data).thenReturn(mapOf(
            "id" to "task-2",
            "title" to "Second Task",
            "description" to "Test Description",
            "completed" to false,
            "createdAt" to 1234567890L
        ))

        whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

        // Mock the get operation
        val mockGetTask = mock<GoogleTask<QuerySnapshot>>()
        whenever(mockCollection.get()).thenReturn(mockGetTask)

        whenever(mockGetTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<QuerySnapshot>>(0)
            listener.onSuccess(mockQuerySnapshot)
            mockGetTask
        }
        whenever(mockGetTask.addOnFailureListener(any())).thenReturn(mockGetTask)
        whenever(mockGetTask.addOnCanceledListener(any())).thenReturn(mockGetTask)

        // Act
        val result = taskRepository.getAllTasks()

        // Assert
        assertThat(result.isSuccess).isTrue()
        val tasks = result.getOrNull()
        assertThat(tasks).hasSize(2)
        assertThat(tasks?.get(0)?.title).isEqualTo("Test Task")
        assertThat(tasks?.get(1)?.title).isEqualTo("Second Task")
    }

    @Test
    fun getAllTasks_emptyCollection_returnsEmptyList() = runTest {
        // Arrange
        whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

        val mockGetTask = mock<GoogleTask<QuerySnapshot>>()
        whenever(mockCollection.get()).thenReturn(mockGetTask)

        whenever(mockGetTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<QuerySnapshot>>(0)
            listener.onSuccess(mockQuerySnapshot)
            mockGetTask
        }
        whenever(mockGetTask.addOnFailureListener(any())).thenReturn(mockGetTask)
        whenever(mockGetTask.addOnCanceledListener(any())).thenReturn(mockGetTask)

        // Act
        val result = taskRepository.getAllTasks()

        // Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun getAllTasks_failure_returnsFailure() = runTest {
        // Arrange
        val expectedException = RuntimeException("Network error")

        val mockGetTask = mock<GoogleTask<QuerySnapshot>>()
        whenever(mockCollection.get()).thenReturn(mockGetTask)

        whenever(mockGetTask.addOnSuccessListener(any())).thenAnswer {
            val task = mock<GoogleTask<QuerySnapshot>>()
            whenever(task.addOnFailureListener(any())).thenAnswer {
                val failureListener = it.getArgument<com.google.android.gms.tasks.OnFailureListener>(0)
                failureListener.onFailure(expectedException)
                task
            }
            whenever(task.addOnCanceledListener(any())).thenReturn(task)
            task
        }

        // Act
        val result = taskRepository.getAllTasks()

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Network error")
    }

    @Test
    fun deleteTask_success_returnsSuccess() = runTest {
        // Arrange
        val taskId = "task-to-delete"
        whenever(mockCollection.document(taskId)).thenReturn(mockDocumentReference)

        val mockDeleteTask = mock<GoogleTask<Void>>()
        whenever(mockDocumentReference.delete()).thenReturn(mockDeleteTask)

        whenever(mockDeleteTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<Void>>(0)
            listener.onSuccess(null)
            mockDeleteTask
        }
        whenever(mockDeleteTask.addOnFailureListener(any())).thenReturn(mockDeleteTask)
        whenever(mockDeleteTask.addOnCanceledListener(any())).thenReturn(mockDeleteTask)

        // Act
        val result = taskRepository.deleteTask(taskId)

        // Assert
        assertThat(result.isSuccess).isTrue()
        verify(mockDocumentReference).delete()
    }

    @Test
    fun deleteTask_failure_returnsFailure() = runTest {
        // Arrange
        val taskId = "task-to-delete"
        val expectedException = RuntimeException("Delete failed")
        whenever(mockCollection.document(taskId)).thenReturn(mockDocumentReference)

        val mockDeleteTask = mock<GoogleTask<Void>>()
        whenever(mockDocumentReference.delete()).thenReturn(mockDeleteTask)

        whenever(mockDeleteTask.addOnSuccessListener(any())).thenAnswer {
            val task = mock<GoogleTask<Void>>()
            whenever(task.addOnFailureListener(any())).thenAnswer {
                val failureListener = it.getArgument<com.google.android.gms.tasks.OnFailureListener>(0)
                failureListener.onFailure(expectedException)
                task
            }
            whenever(task.addOnCanceledListener(any())).thenReturn(task)
            task
        }

        // Act
        val result = taskRepository.deleteTask(taskId)

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Delete failed")
    }

    @Test
    fun updateTask_success_returnsSuccess() = runTest {
        // Arrange
        val taskToUpdate = testTask
        whenever(mockCollection.document(taskToUpdate.id)).thenReturn(mockDocumentReference)

        val mockSetTask = mock<GoogleTask<Void>>()
        whenever(mockDocumentReference.set(any Map::class.java)).thenReturn(mockSetTask)

        whenever(mockSetTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<Void>>(0)
            listener.onSuccess(null)
            mockSetTask
        }
        whenever(mockSetTask.addOnFailureListener(any())).thenReturn(mockSetTask)
        whenever(mockSetTask.addOnCanceledListener(any())).thenReturn(mockSetTask)

        // Act
        val result = taskRepository.updateTask(taskToUpdate)

        // Assert
        assertThat(result.isSuccess).isTrue()
        verify(mockDocumentReference).set(any Map::class.java)
    }

    @Test
    fun updateTask_withEmptyId_returnsFailure() = runTest {
        // Arrange
        val invalidTask = testTask.copy(id = "")

        // Act
        val result = taskRepository.updateTask(invalidTask)

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Task ID cannot be empty")
    }

    @Test
    fun updateTask_firestoreFailure_returnsFailure() = runTest {
        // Arrange
        val taskToUpdate = testTask
        val expectedException = RuntimeException("Update failed")
        whenever(mockCollection.document(taskToUpdate.id)).thenReturn(mockDocumentReference)

        val mockSetTask = mock<GoogleTask<Void>>()
        whenever(mockDocumentReference.set(any Map::class.java)).thenReturn(mockSetTask)

        whenever(mockSetTask.addOnSuccessListener(any())).thenAnswer {
            val task = mock<GoogleTask<Void>>()
            whenever(task.addOnFailureListener(any())).thenAnswer {
                val failureListener = it.getArgument<com.google.android.gms.tasks.OnFailureListener>(0)
                failureListener.onFailure(expectedException)
                task
            }
            whenever(task.addOnCanceledListener(any())).thenReturn(task)
            task
        }

        // Act
        val result = taskRepository.updateTask(taskToUpdate)

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Update failed")
    }

    @Test
    fun getTaskById_success_returnsTask() = runTest {
        // Arrange
        val taskId = "existing-task-id"
        whenever(mockCollection.document(taskId)).thenReturn(mockDocumentReference)

        whenever(mockDocumentSnapshot.exists()).thenReturn(true)
        whenever(mockDocumentSnapshot.data).thenReturn(mapOf(
            "id" to taskId,
            "title" to "Existing Task",
            "description" to "Description",
            "completed" to false,
            "createdAt" to 1234567890L
        ))

        val mockGetTask = mock<GoogleTask<DocumentSnapshot>>()
        whenever(mockDocumentReference.get()).thenReturn(mockGetTask)

        whenever(mockGetTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<DocumentSnapshot>>(0)
            listener.onSuccess(mockDocumentSnapshot)
            mockGetTask
        }
        whenever(mockGetTask.addOnFailureListener(any())).thenReturn(mockGetTask)
        whenever(mockGetTask.addOnCanceledListener(any())).thenReturn(mockGetTask)

        // Act
        val result = taskRepository.getTaskById(taskId)

        // Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.title).isEqualTo("Existing Task")
    }

    @Test
    fun getTaskById_notFound_returnsFailure() = runTest {
        // Arrange
        val taskId = "non-existing-task-id"
        whenever(mockCollection.document(taskId)).thenReturn(mockDocumentReference)

        whenever(mockDocumentSnapshot.exists()).thenReturn(false)

        val mockGetTask = mock<GoogleTask<DocumentSnapshot>>()
        whenever(mockDocumentReference.get()).thenReturn(mockGetTask)

        whenever(mockGetTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<DocumentSnapshot>>(0)
            listener.onSuccess(mockDocumentSnapshot)
            mockGetTask
        }
        whenever(mockGetTask.addOnFailureListener(any())).thenReturn(mockGetTask)
        whenever(mockGetTask.addOnCanceledListener(any())).thenReturn(mockGetTask)

        // Act
        val result = taskRepository.getTaskById(taskId)

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(NoSuchElementException::class.java)
    }
}
