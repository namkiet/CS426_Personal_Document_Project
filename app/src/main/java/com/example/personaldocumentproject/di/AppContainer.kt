package com.example.personaldocumentproject.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personaldocumentproject.BuildConfig
import com.example.personaldocumentproject.MainViewModel
import com.example.personaldocumentproject.data.local.AppDatabase
import com.example.personaldocumentproject.data.local.SettingsLocalDataSource
import com.example.personaldocumentproject.data.local.dataStore
import com.example.personaldocumentproject.data.mapper.DocumentMapper
import com.example.personaldocumentproject.data.ml.embedding.MediaPipeEmbeddingService
import com.example.personaldocumentproject.data.ml.labeling.MlKitImageLabelingService
import com.example.personaldocumentproject.data.ml.llm.GeminiApiLlmService
import com.example.personaldocumentproject.data.ml.metadata.EmbeddingCategoryClassificationService
import com.example.personaldocumentproject.data.ml.metadata.MediaPipeDescriptionGenerationService
import com.example.personaldocumentproject.data.ml.metadata.SimpleTagGenerationService
import com.example.personaldocumentproject.data.ml.metadata.SimpleTitleGenerationService
import com.example.personaldocumentproject.data.ml.ocr.MlKitOcrService
import com.example.personaldocumentproject.data.ml.translation.MlKitEnglishTranslationService
import com.example.personaldocumentproject.data.debug.DebugDocumentSeeder
import com.example.personaldocumentproject.data.local.storage.LocalImageStorageService
import com.example.personaldocumentproject.data.repository.DefaultDocumentRepository
import com.example.personaldocumentproject.data.repository.SettingRepositoryImpl
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.domain.service.LlmService
import com.example.personaldocumentproject.domain.usecase.AnalyzeDocument
import com.example.personaldocumentproject.domain.usecase.DeleteDocument
import com.example.personaldocumentproject.domain.usecase.GetDocument
import com.example.personaldocumentproject.domain.usecase.ObserveDocuments
import com.example.personaldocumentproject.domain.usecase.SaveDocument
import com.example.personaldocumentproject.domain.usecase.ResetApplicationData
import com.example.personaldocumentproject.domain.usecase.SearchDocuments
import com.example.personaldocumentproject.domain.usecase.UpdateDocument
import com.example.personaldocumentproject.presentation.capture.CaptureViewModel
import com.example.personaldocumentproject.presentation.detail.DetailViewModel
import com.example.personaldocumentproject.presentation.library.LibraryViewModel
import com.example.personaldocumentproject.presentation.review.ReviewViewModel
import com.example.personaldocumentproject.presentation.setting.SettingViewModel

/** Composition root. Replace adapters here without leaking infrastructure into domain or UI. */
class AppContainer(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val repository = DefaultDocumentRepository(database.documentDao(), DocumentMapper())
    private val ocrService = MlKitOcrService(context)
    private val objectDetectionService = MlKitImageLabelingService(context)
    private val titleGenerationService = SimpleTitleGenerationService
    private val descriptionGenerationService = MediaPipeDescriptionGenerationService(context)
    private val tagGenerationService = SimpleTagGenerationService
    private val embeddingService = MediaPipeEmbeddingService(context)
    private val categoryClassificationService = EmbeddingCategoryClassificationService(embeddingService)
    private val imageStorageService = LocalImageStorageService(context)
    private val textTranslationService = MlKitEnglishTranslationService()
    private val debugDocumentSeeder = DebugDocumentSeeder(context, repository)

    private val settingsLocalDataSource = SettingsLocalDataSource(context.dataStore)
    val settingsRepository = SettingRepositoryImpl(settingsLocalDataSource)

    val geminiService: LlmService? = runCatching {
        GeminiApiLlmService(BuildConfig.GEMINI_API_KEY)
    }.getOrNull()

    suspend fun warmCaches() {
        runCatching {
            ocrService.prepare()
            objectDetectionService.prepare()
            descriptionGenerationService.prepare()
            categoryClassificationService.prepare()
        }
    }

    suspend fun seedDebugDocuments() {
        if (!BuildConfig.DEBUG) return
        val isDebuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) debugDocumentSeeder.seed()
    }

    // ── Use-cases ─────────────────────────────────────────────────────────────

    val observeDocuments = ObserveDocuments(repository)
    val getDocument = GetDocument(repository)
    val deleteDocument = DeleteDocument(repository, imageStorageService)

    fun getAnalyzeDocument(disableApiMode: Boolean = false) = AnalyzeDocument(
        context = context,
        ocrService = ocrService,
        objectDetectionService = objectDetectionService,
        titleGenerationService = titleGenerationService,
        descriptionGenerationService = descriptionGenerationService,
        categoryClassificationService = categoryClassificationService,
        tagGenerationService = tagGenerationService,
        textTranslationService = textTranslationService,
        geminiService = if (disableApiMode) null else geminiService,
    )

    val analyzeDocument get() = getAnalyzeDocument()

    val saveDocument = SaveDocument(repository, imageStorageService, embeddingService)
    val updateDocument = UpdateDocument(repository, embeddingService)
    val searchDocuments = SearchDocuments(repository, embeddingService)
    val resetApplicationData = ResetApplicationData(repository, imageStorageService, settingsRepository)

    // ── ViewModel factories ───────────────────────────────────────────────────

    val libraryViewModelFactory = factory { LibraryViewModel(observeDocuments, searchDocuments, deleteDocument) }
    val captureViewModelFactory = factory { CaptureViewModel() }
    fun getReviewViewModelFactory(userSetting: UserSetting = UserSetting()) = factory {
        ReviewViewModel(getAnalyzeDocument(userSetting.disableApiMode), saveDocument)
    }
    val reviewViewModelFactory get() = getReviewViewModelFactory()
    val detailViewModelFactory = factory { DetailViewModel(getDocument, deleteDocument, updateDocument) }
    val mainViewModelFactory = factory { MainViewModel(settingsRepository) }
    val settingViewModelFactory = factory {
        SettingViewModel(
            repository = settingsRepository,
            resetApplicationData = resetApplicationData,
        )
    }

    private fun factory(create: () -> ViewModel): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
        }
}
