package com.example.personaldocumentproject.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaldocumentproject.di.AppContainer
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.presentation.capture.CaptureRoute
import com.example.personaldocumentproject.presentation.capture.CaptureViewModel
import com.example.personaldocumentproject.presentation.detail.DetailRoute
import com.example.personaldocumentproject.presentation.detail.DetailViewModel
import com.example.personaldocumentproject.presentation.library.LibraryNavigationActions
import com.example.personaldocumentproject.presentation.library.LibraryRoute
import com.example.personaldocumentproject.presentation.library.LibraryViewModel
import com.example.personaldocumentproject.presentation.modelmanager.ModelManagerRoute
import com.example.personaldocumentproject.presentation.modelmanager.ModelManagerViewModel
import com.example.personaldocumentproject.presentation.review.ReviewRoute
import com.example.personaldocumentproject.presentation.review.ReviewViewModel
import com.example.personaldocumentproject.presentation.search.SearchRoute
import com.example.personaldocumentproject.presentation.search.SearchViewModel
import com.example.personaldocumentproject.presentation.setting.SettingRoute
import com.example.personaldocumentproject.presentation.setting.SettingViewModel

@Composable
fun AppNavGraph(
    container: AppContainer,
    userSetting: UserSetting,
    navigator: AppNavigator = rememberAppNavigator(),
) {
    BackHandler(enabled = navigator.canGoBack) {
        navigator.popBackStack()
    }

    when (val current = navigator.currentRoute) {
        Routes.Library -> {
            val viewModelLibrary: LibraryViewModel = viewModel(factory = container.libraryViewModelFactory)
            val viewModelCapture: CaptureViewModel = viewModel(factory = container.captureViewModelFactory)

            LibraryRoute(
                viewModelLibrary = viewModelLibrary,
                userSetting = userSetting,
                navigationActions = LibraryNavigationActions(
                    onDocumentClick = { navigator.navigateTo(Routes.Detail(it)) },
                    onSettingClick = { navigator.navigateTo(Routes.Setting) },
                    onManageModels = { navigator.navigateTo(Routes.ModelManager) },
                ),
            )
            CaptureRoute(
                viewModel = viewModelCapture,
                onImageSelected = { navigator.navigateTo(Routes.Review(it)) },
                onBack = { navigator.popBackStack() },
            )
        }

        Routes.Capture -> {}

        is Routes.Review -> {
            val viewModel: ReviewViewModel = viewModel(
                key = "review:${current.imageUri}",
                factory = container.reviewViewModelFactory,
            )
            ReviewRoute(
                imageUri = current.imageUri,
                viewModel = viewModel,
                onSaved = { navigator.popToRoot() },
                onBack = { navigator.popBackStack() },
            )
        }

        Routes.Search -> {
            val viewModelSearch: SearchViewModel = viewModel(factory = container.searchViewModelFactory)
            val viewModelCapture: CaptureViewModel = viewModel(factory = container.captureViewModelFactory)
            SearchRoute(
                viewModelSearch = viewModelSearch,
                userSetting = userSetting,
                onBack = { navigator.popBackStack() },
                onDocumentClick = { navigator.navigateTo(Routes.Detail(it)) },
                onSettingClick = { navigator.navigateTo(Routes.Setting) },
            )
            CaptureRoute(
                viewModel = viewModelCapture,
                onImageSelected = { navigator.navigateTo(Routes.Review(it)) },
                onBack = { navigator.popBackStack() },
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
                onBack = { navigator.popBackStack() },
            )
        }

        Routes.ModelManager -> {
            val viewModel: ModelManagerViewModel =
                viewModel(factory = container.modelManagerViewModelFactory)
            ModelManagerRoute(
                viewModel = viewModel,
                onBack = { route = Routes.Library },
            )
        }

        Routes.Setting -> {
            val viewModel: SettingViewModel = viewModel(factory = container.settingViewModelFactory)
            SettingRoute(
                viewModel = viewModel,
                onBack = { navigator.popBackStack() },
            )
        }
    }
}
