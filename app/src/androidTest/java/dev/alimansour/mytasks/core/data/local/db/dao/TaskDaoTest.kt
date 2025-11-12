package dev.alimansour.mytasks.core.data.local.db.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.alimansour.mytasks.core.data.local.db.TasksDatabase
import dev.alimansour.mytasks.core.data.local.db.entity.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var db: TasksDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db =
            Room
                .inMemoryDatabaseBuilder(context, TasksDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.taskDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        id: Long = 0,
        title: String = "Title",
        description: String = "Desc",
        dueDate: Long = 1000L,
        isCompleted: Boolean = false,
    ) = TaskEntity(id = id, title = title, description = description, dueDate = dueDate, isCompleted = isCompleted)

    @Test
    fun insertTask_shouldReturnRowId_and_row_is_retrievable_in_order_by_dueDate() =
        runTest {
            val id1 = dao.insertTask(entity(title = "A", dueDate = 20))
            val id2 = dao.insertTask(entity(title = "B", dueDate = 10))
            assertTrue(id1 > 0)
            assertTrue(id2 > 0)

            val list = dao.getTasks().first()
            assertEquals(list.map { it.title }, listOf("B", "A"))
        }

    @Test
    fun updateTask_shouldAffectExactlyOneRow_and_persist_changes() =
        runTest {
            val id = dao.insertTask(entity(title = "Old", dueDate = 5))
            val updated = entity(id = id, title = "New", description = "Changed", dueDate = 15, isCompleted = true)

            val affected = dao.updateTask(updated)
            assertEquals(1, affected)

            val list = dao.getTasks().first()
            assertEquals(1, list.size)
            val row = list.first()
            assertEquals(id, row.id)
            assertEquals("New", row.title)
            assertEquals("Changed", row.description)
            assertEquals(15, row.dueDate)
            assertTrue(row.isCompleted)
        }

    @Test
    fun updateTask_nonExisting_shouldReturnZero() =
        runTest {
            val affected = dao.updateTask(entity(id = 999, title = "X"))
            assertEquals(0, affected)
        }

    @Test
    fun deleteTask_shouldAffectExactlyOneRow_and_remove_row() =
        runTest {
            val id = dao.insertTask(entity(title = "ToDelete"))
            val affected = dao.deleteTask(entity(id = id, title = "ToDelete"))
            assertEquals(1, affected)

            val list = dao.getTasks().first()
            assertTrue(list.isEmpty())
        }

    @Test
    fun deleteTask_nonExisting_shouldReturnZero() =
        runTest {
            val affected = dao.deleteTask(entity(id = 12345, title = "Nope"))
            assertEquals(0, affected)
        }

    @Test
    fun insert_withExistingPrimaryKey_shouldReplace_row_due_to_REPLACE_conflict_strategy() =
        runTest {
            val id = dao.insertTask(entity(title = "First", description = "D1", dueDate = 1))
            dao.insertTask(entity(id = id, title = "Second", description = "D2", dueDate = 2))

            val list = dao.getTasks().first()
            assertEquals(1, list.size)
            val row = list.first()
            assertEquals(id, row.id)
            assertEquals("Second", row.title)
            assertEquals("D2", row.description)
            assertEquals(2, row.dueDate)
        }
}
