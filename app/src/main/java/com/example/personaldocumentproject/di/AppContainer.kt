package com.example.personaldocumentproject.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personaldocumentproject.MainViewModel
import com.example.personaldocumentproject.data.local.AppDatabase
import com.example.personaldocumentproject.data.local.SettingsLocalDataSource
import com.example.personaldocumentproject.data.local.dataStore
import com.example.personaldocumentproject.data.local.storage.LocalImageStorageService
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.data.ml.embedding.MediaPipeEmbeddingService
import com.example.personaldocumentproject.data.ml.labeling.MlKitImageLabelingService
import com.example.personaldocumentproject.data.ml.llm.ModelManagerRepository
import com.example.personaldocumentproject.data.ml.metadata.SimpleMetadataService
import com.example.personaldocumentproject.data.ml.ocr.MlKitOcrService
import com.example.personaldocumentproject.data.repository.DefaultDocumentRepository
import com.example.personaldocumentproject.data.repository.SettingRepositoryImpl
import com.example.personaldocumentproject.domain.usecase.AnalyzeDocument
import com.example.personaldocumentproject.domain.usecase.GetDocument
import com.example.personaldocumentproject.domain.usecase.ObserveDocuments
import com.example.personaldocumentproject.domain.usecase.SaveDocument
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import com.example.personaldocumentproject.presentation.capture.CaptureViewModel
import com.example.personaldocumentproject.presentation.detail.DetailViewModel
import com.example.personaldocumentproject.presentation.library.LibraryViewModel
import com.example.personaldocumentproject.presentation.modelmanager.ModelManagerViewModel
import com.example.personaldocumentproject.presentation.review.ReviewViewModel
import com.example.personaldocumentproject.presentation.search.SearchViewModel
import com.example.personaldocumentproject.presentation.setting.SettingViewModel

/** Composition root. Replace adapters here without leaking infrastructure into domain or UI. */
class AppContainer(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val repository = DefaultDocumentRepository(database.documentDao(), DocumentMapper())
    private val ocrService = MlKitOcrService(context)
    private val objectDetectionService = MlKitImageLabelingService(context)
    private val metadataService = SimpleMetadataService()
    private val embeddingService = MediaPipeEmbeddingService(context)
    private val imageStorageService = LocalImageStorageService(context)

    private val settingsLocalDataSource = SettingsLocalDataSource(context.dataStore)
    private val settingsRepository = SettingRepositoryImpl(settingsLocalDataSource)

    // ── LLM / Model Manager ───────────────────────────────────────────────────

    /** Manages model files on disk and the user's active-model preference. */
    val modelManagerRepository = ModelManagerRepository(context)

    /**
     * Returns the active LiteRT-LM service if a valid model file is present on device,
     * or null if no model has been downloaded yet. Re-evaluated each time [analyzeDocument]
     * is invoked so that switching models in the UI takes effect on the next capture.
     */
    private fun activeLlmServiceOrNull() = modelManagerRepository.activeServiceOrNull()

    val observeDocuments = ObserveDocuments(repository)
    val getDocument = GetDocument(repository)

    /**
     * AnalyzeDocument uses the active LLM if one is available.
     * The lambda is called at factory time so each ReviewViewModel gets the
     * currently-active model without rebuilding AppContainer.
     */
    val analyzeDocument get() = AnalyzeDocument(
        ocrService,
        objectDetectionService,
        metadataService,
        llmService = activeLlmServiceOrNull(),
    )

    val saveDocument = SaveDocument(repository, imageStorageService, embeddingService)
    val searchDocuments = SearchDocuments(repository, embeddingService)

    // ── ViewModel factories ───────────────────────────────────────────────────

    val libraryViewModelFactory = factory { LibraryViewModel(observeDocuments) }
    val captureViewModelFactory = factory { CaptureViewModel() }
    val reviewViewModelFactory get() = factory { ReviewViewModel(analyzeDocument, saveDocument) }
    val searchViewModelFactory = factory { SearchViewModel(searchDocuments) }
    val detailViewModelFactory = factory { DetailViewModel(getDocument) }
    val mainViewModelFactory = factory { MainViewModel(settingsRepository) }
    val settingViewModelFactory = factory { SettingViewModel(repository = settingsRepository) }
    val modelManagerViewModelFactory = factory { ModelManagerViewModel(modelManagerRepository) }

    private fun factory(create: () -> ViewModel): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
        }
}
