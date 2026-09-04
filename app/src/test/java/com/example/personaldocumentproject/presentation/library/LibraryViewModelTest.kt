package com.example.personaldocumentproject.presentation.library

import com.example.personaldocumentproject.domain.model.Document
import com.example.personaldocumentproject.domain.model.DocumentCategory
import com.example.personaldocumentproject.domain.model.SearchResult
import com.example.personaldocumentproject.domain.repository.DocumentRepository
import com.example.personaldocumentproject.domain.service.EmbeddingService
import com.example.personaldocumentproject.domain.usecase.ExtractTagsFromDocuments
import com.example.personaldocumentproject.domain.usecase.FilterDocumentsByTags
import com.example.personaldocumentproject.domain.usecase.ObserveDocuments
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val docFlow = MutableStateFlow<List<Document>>(emptyList())
    private val fakeRepo = FakeDocumentRepository(docFlow)
    private val fakeEmbedding = FakeEmbeddingService()

    private val observeDocuments = ObserveDocuments(fakeRepo)
    private val searchDocuments = SearchDocuments(fakeRepo, fakeEmbedding)
    private val extractTags = ExtractTagsFromDocuments()
    private val filterTags = FilterDocumentsByTags()

    private lateinit var viewModel: LibraryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsAllDocumentsAndExtractsTags() = runTest(testDispatcher) {
        val doc1 = createDoc(id = "1", title = "Tax Return", tags = listOf("finance", "tax"))
        val doc2 = createDoc(id = "2", title = "Coffee Receipt", tags = listOf("receipt", "coffee"))
        docFlow.value = listOf(doc1, doc2)

        viewModel = LibraryViewModel(observeDocuments, searchDocuments, extractTags, filterTags)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.allDocuments.size)
        assertEquals(2, state.documents.size)
        assertEquals(listOf("coffee", "finance", "receipt", "tax"), state.availableTags)
        assertEquals(listOf("coffee", "finance", "receipt", "tax"), state.filteredTags)
        assertFalse(state.isSearchActive)
    }

    @Test
    fun searchQueryChanged_debouncesAndShowsResults() = runTest(testDispatcher) {
        val doc1 = createDoc(id = "1", title = "Tax Return", tags = listOf("tax"))
        val doc2 = createDoc(id = "2", title = "Coffee Receipt", tags = listOf("receipt"))
        docFlow.value = listOf(doc1, doc2)

        viewModel = LibraryViewModel(observeDocuments, searchDocuments, extractTags, filterTags)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Coffee")
        advanceTimeBy(350)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSearchActive)
        assertEquals(1, state.documents.size)
        assertEquals("Coffee Receipt", state.documents.first().title)
    }

    @Test
    fun onClearSearchQuery_restoresAllDocuments() = runTest(testDispatcher) {
        val doc1 = createDoc(id = "1", title = "Tax Return", tags = listOf("tax"))
        val doc2 = createDoc(id = "2", title = "Coffee Receipt", tags = listOf("receipt"))
        docFlow.value = listOf(doc1, doc2)

        viewModel = LibraryViewModel(observeDocuments, searchDocuments, extractTags, filterTags)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Coffee")
        advanceTimeBy(350)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.documents.size)

        viewModel.onClearSearchQuery()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearchActive)
        assertEquals(2, state.documents.size)
    }

    @Test
    fun tagToggle_filtersDocumentsByTag() = runTest(testDispatcher) {
        val doc1 = createDoc(id = "1", title = "Tax Return", tags = listOf("tax", "important"))
        val doc2 = createDoc(id = "2", title = "Coffee Receipt", tags = listOf("receipt"))
        val doc3 = createDoc(id = "3", title = "Work Notes", tags = listOf("important"))
        docFlow.value = listOf(doc1, doc2, doc3)

        viewModel = LibraryViewModel(observeDocuments, searchDocuments, extractTags, filterTags)
        advanceUntilIdle()

        viewModel.onTagToggle("important")
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertTrue(state.isSearchActive)
        assertEquals(setOf("important"), state.selectedTags)
        assertEquals(listOf("1", "3"), state.documents.map { it.id })

        // Toggle again to deselect
        viewModel.onTagToggle("important")
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertFalse(state.isSearchActive)
        assertEquals(emptySet<String>(), state.selectedTags)
        assertEquals(3, state.documents.size)
    }

    @Test
    fun tagQueryChanged_filtersAvailableTags() = runTest(testDispatcher) {
        val doc1 = createDoc(id = "1", tags = listOf("receipt", "finance", "tax"))
        docFlow.value = listOf(doc1)

        viewModel = LibraryViewModel(observeDocuments, searchDocuments, extractTags, filterTags)
        advanceUntilIdle()

        viewModel.onTagQueryChanged("rec")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("rec", state.tagQuery)
        assertEquals(listOf("receipt"), state.filteredTags)

        viewModel.onClearTagQuery()
        advanceUntilIdle()
        assertEquals(listOf("finance", "receipt", "tax"), viewModel.uiState.value.filteredTags)
    }

    @Test
    fun onToggleTagDropdown_togglesExpanded() = runTest(testDispatcher) {
        viewModel = LibraryViewModel(observeDocuments, searchDocuments, extractTags, filterTags)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isTagDropdownExpanded)

        viewModel.onToggleTagDropdown()
        assertTrue(viewModel.uiState.value.isTagDropdownExpanded)

        viewModel.onToggleTagDropdown()
        assertFalse(viewModel.uiState.value.isTagDropdownExpanded)
    }

    private fun createDoc(id: String, title: String = "Doc $id", tags: List<String>): Document {
        return Document(
            id = id,
            imageUri = "content://$id",
            title = title,
            description = "Desc",
            ocrText = "OCR",
            category = DocumentCategory.OTHER,
            tags = tags,
            detectedObjects = emptyList(),
            embedding = emptyList(),
            createdAt = 1000L
        )
    }
}

private class FakeDocumentRepository(
    private val docs: MutableStateFlow<List<Document>>
) : DocumentRepository {
    override fun observeAll(): Flow<List<Document>> = docs
    override suspend fun get(id: String): Document? = docs.value.find { it.id == id }
    override fun search(query: String, queryEmbedding: List<Float>): Flow<List<SearchResult>> {
        return docs.map { list ->
            list.filter { it.title.contains(query, ignoreCase = true) || it.tags.any { t -> t.contains(query, ignoreCase = true) } }
                .map { SearchResult(it, 1f) }
        }
    }
    override suspend fun save(document: Document) {}
}

private class FakeEmbeddingService : EmbeddingService {
    override suspend fun embed(text: String): List<Float> = emptyList()
}
