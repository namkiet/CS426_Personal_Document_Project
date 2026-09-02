package com.example.personaldocumentproject.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personaldocumentproject.data.local.AppDatabase
import com.example.personaldocumentproject.data.local.storage.LocalImageStorageService
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.data.ml.embedding.MediaPipeEmbeddingService
import com.example.personaldocumentproject.data.ml.ocr.MlKitOcrService
import com.example.personaldocumentproject.data.ml.objectdetection.LocalObjectDetectionService
import com.example.personaldocumentproject.data.ml.metadata.SimpleMetadataService
import com.example.personaldocumentproject.data.repository.DefaultDocumentRepository
import com.example.personaldocumentproject.domain.usecase.AnalyzeDocument
import com.example.personaldocumentproject.domain.usecase.GetDocument
import com.example.personaldocumentproject.domain.usecase.ObserveDocuments
import com.example.personaldocumentproject.domain.usecase.SaveDocument
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import com.example.personaldocumentproject.presentation.capture.CaptureViewModel
import com.example.personaldocumentproject.presentation.detail.DetailViewModel
import com.example.personaldocumentproject.presentation.library.LibraryViewModel
import com.example.personaldocumentproject.presentation.review.ReviewViewModel
import com.example.personaldocumentproject.presentation.search.SearchViewModel

/** Composition root. Replace adapters here without leaking infrastructure into domain or UI. */
class AppContainer(private val context: Context) {
    private val database = AppDatabase()
    private val repository = DefaultDocumentRepository(database.documentDao, DocumentMapper())
    private val ocrService = MlKitOcrService(context)
    private val objectDetectionService = LocalObjectDetectionService()
    private val metadataService = SimpleMetadataService()
    private val embeddingService = MediaPipeEmbeddingService(context)
    private val imageStorageService = LocalImageStorageService(context)

    val observeDocuments = ObserveDocuments(repository)
    val getDocument = GetDocument(repository)
    val analyzeDocument = AnalyzeDocument(
        ocrService,
        objectDetectionService,
        metadataService,
    )
    val saveDocument = SaveDocument(repository, imageStorageService, embeddingService)
    val searchDocuments = SearchDocuments(repository, embeddingService)

    val libraryViewModelFactory = factory { LibraryViewModel(observeDocuments) }
    val captureViewModelFactory = factory { CaptureViewModel() }
    val reviewViewModelFactory = factory { ReviewViewModel(analyzeDocument, saveDocument) }
    val searchViewModelFactory = factory { SearchViewModel(searchDocuments) }
    val detailViewModelFactory = factory { DetailViewModel(getDocument) }

    private fun factory(create: () -> ViewModel): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
        }
}
