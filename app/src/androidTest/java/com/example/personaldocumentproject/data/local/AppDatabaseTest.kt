package com.example.personaldocumentproject.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: DocumentDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.documentDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun upsert_and_get_returnsSameEntity() = runBlocking {
        val entity = entity("1", "Document One", createdAt = 100L)
        dao.upsert(entity)

        val result = dao.get("1")

        assertEquals(entity, result)
    }

    @Test
    fun get_unknownId_returnsNull() = runBlocking {
        assertNull(dao.get("unknown"))
    }

    @Test
    fun observeAll_returnsNewestFirst() = runBlocking {
        dao.upsert(entity("old", "Old", createdAt = 1L))
        dao.upsert(entity("new", "New", createdAt = 2L))

        val results = dao.observeAll().first()

        assertEquals(listOf("new", "old"), results.map { it.id })
    }

    @Test
    fun upsert_updatesExistingDocument() = runBlocking {
        dao.upsert(entity("1", "Original Title"))
        dao.upsert(entity("1", "Updated Title"))

        val result = dao.get("1")

        assertEquals("Updated Title", result?.title)
        assertEquals(1, dao.observeAll().first().size)
    }

    private fun entity(id: String, title: String, createdAt: Long = 0L) = DocumentEntity(
        id = id,
        imageUri = "content://$id",
        title = title,
        description = "",
        ocrText = "",
        category = "OTHER",
        tags = "",
        detectedObjects = "",
        embedding = "",
        createdAt = createdAt,
    )
}
