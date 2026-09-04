package com.example.personaldocumentproject.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaldocumentproject.di.AppContainer
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.presentation.detail.DetailRoute
import com.example.personaldocumentproject.presentation.detail.DetailViewModel
import com.example.personaldocumentproject.presentation.library.LibraryRoute
import com.example.personaldocumentproject.presentation.library.LibraryViewModel
import com.example.personaldocumentproject.presentation.review.ReviewRoute
import com.example.personaldocumentproject.presentation.review.ReviewViewModel
import com.example.personaldocumentproject.presentation.setting.SettingRoute
import com.example.personaldocumentproject.presentation.setting.SettingViewModel

@Composable
fun AppNavGraph(container: AppContainer, userSetting: UserSetting = UserSetting()) {
    var backStack: List<Routes> by rememberSaveable(stateSaver = Routes.BackStackSaver) {
        mutableStateOf(listOf<Routes>(Routes.Library))
    }
    val current = backStack.lastOrNull() ?: Routes.Library

    fun navigateTo(route: Routes) {
        backStack = backStack + route
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        }
    }

    fun popToLibrary() {
        backStack = listOf(Routes.Library)
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    LaunchedEffect(Unit) {
        container.seedDebugDocuments()
        container.warmCaches()
    }

    when (current) {
        Routes.Library -> {
            val viewModel: LibraryViewModel = viewModel(factory = container.libraryViewModelFactory)
            LibraryRoute(
                viewModel = viewModel,
                onImageCaptured = { imageUri -> navigateTo(Routes.Review(imageUri)) },
                onDocumentClick = { navigateTo(Routes.Detail(it)) },
                onSettingClick = { navigateTo(Routes.Setting) },
            )
        }
        Routes.Capture -> {
            // Handled directly via FAB launcher in LibraryRoute
        }
        is Routes.Review -> {
            val viewModel: ReviewViewModel = viewModel(
                key = "review:${current.imageUri}",
                factory = container.reviewViewModelFactory,
            )
            ReviewRoute(
                imageUri = current.imageUri,
                viewModel = viewModel,
                onSaved = { popToLibrary() },
                onBack = { navigateBack() },
            )
        }
        is Routes.Detail -> {
            val viewModel: DetailViewModel = viewModel(
                key = "detail:${current.documentId}",
                factory = container.detailViewModelFactory,
            )
            DetailRoute(
                documentId = current.documentId,
                viewModel = viewModel,
                onBack = { navigateBack() },
            )
        }
        Routes.Setting -> {
            val viewModel: SettingViewModel = viewModel(
                factory = container.settingViewModelFactory,
            )
            SettingRoute(
                viewModel = viewModel,
                onBack = { navigateBack() },
            )
        }
        Routes.Account -> {
            // Placeholder
        }
    }
}
