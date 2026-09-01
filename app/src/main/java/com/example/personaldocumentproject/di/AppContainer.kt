package com.example.personaldocumentproject.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personaldocumentproject.data.local.AppDatabase
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.data.ml.embedding.LocalEmbeddingService
import com.example.personaldocumentproject.data.ml.ocr.MlKitOcrService
import com.example.personaldocumentproject.data.repository.DefaultDocumentRepository
import com.example.personaldocumentproject.domain.usecase.AnalyzeDocument
import com.example.personaldocumentproject.domain.usecase.GetDocument
import com.example.personaldocumentproject.domain.usecase.ObserveDocuments
import com.example.personaldocumentproject.domain.usecase.SaveDocument
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import com.example.personaldocumentproject.presentation.capture.CaptureViewModel
import com.example.personaldocumentproject.presentation.library.LibraryViewModel
import com.example.personaldocumentproject.presentation.review.ReviewViewModel
import com.example.personaldocumentproject.presentation.search.SearchViewModel

/** Composition root. Replace adapters here without leaking infrastructure into domain or UI. */
class AppContainer {
    private val database = AppDatabase()
    private val repository = DefaultDocumentRepository(database.documentDao, DocumentMapper())
    private val ocrService = MlKitOcrService()
    private val embeddingService = LocalEmbeddingService()

    val observeDocuments = ObserveDocuments(repository)
    val getDocument = GetDocument(repository)
    val analyzeDocument = AnalyzeDocument(ocrService, embeddingService)
    val saveDocument = SaveDocument(repository)
    val searchDocuments = SearchDocuments(repository, embeddingService)

    val libraryViewModelFactory = factory { LibraryViewModel(observeDocuments) }
    val captureViewModelFactory = factory { CaptureViewModel() }
    val reviewViewModelFactory = factory { ReviewViewModel(analyzeDocument, saveDocument) }
    val searchViewModelFactory = factory { SearchViewModel(searchDocuments) }

    private fun factory(create: () -> ViewModel): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
        }
}
