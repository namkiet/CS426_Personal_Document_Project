package com.example.personaldocumentproject.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Database boundary; replace [InMemoryDocumentDao] with the Room-generated database. */
class AppDatabase {
    val documentDao: DocumentDao = InMemoryDocumentDao()
}

private class InMemoryDocumentDao : DocumentDao {
    private val documents = MutableStateFlow<List<DocumentEntity>>(emptyList())

    override fun observeAll() = documents.asStateFlow()
    override suspend fun get(id: String) = documents.value.firstOrNull { it.id == id }
    override suspend fun upsert(document: DocumentEntity) {
        documents.update { current ->
            (current.filterNot { it.id == document.id } + document)
                .sortedByDescending(DocumentEntity::createdAt)
        }
    }
}
