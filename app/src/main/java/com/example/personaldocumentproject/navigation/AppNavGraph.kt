package com.example.personaldocumentproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaldocumentproject.di.AppContainer
import com.example.personaldocumentproject.domain.model.UserSetting
import com.example.personaldocumentproject.presentation.capture.CaptureRoute
import com.example.personaldocumentproject.presentation.capture.CaptureViewModel
import com.example.personaldocumentproject.presentation.detail.DetailRoute
import com.example.personaldocumentproject.presentation.detail.DetailViewModel
import com.example.personaldocumentproject.presentation.library.LibraryRoute
import com.example.personaldocumentproject.presentation.library.LibraryViewModel
import com.example.personaldocumentproject.presentation.review.ReviewRoute
import com.example.personaldocumentproject.presentation.review.ReviewViewModel
import com.example.personaldocumentproject.presentation.search.SearchRoute
import com.example.personaldocumentproject.presentation.search.SearchViewModel
import com.example.personaldocumentproject.presentation.setting.SettingRoute
import com.example.personaldocumentproject.presentation.setting.SettingViewModel

@Composable
fun AppNavGraph(container: AppContainer, userSetting: UserSetting) {
    var route: Routes by remember { mutableStateOf(Routes.Library) }
    when (val current = route) {
        Routes.Library -> {
            val viewModelLibrary: LibraryViewModel = viewModel(factory = container.libraryViewModelFactory)
            val viewModelCapture: CaptureViewModel = viewModel(factory = container.captureViewModelFactory)

            LibraryRoute(
                viewModelLibrary = viewModelLibrary,
                userSetting = userSetting,
                onDocumentClick = { route = Routes.Detail(it) },
                onSettingClick = { route = Routes.Setting },
            )
            CaptureRoute(
                viewModel = viewModelCapture,
                onImageSelected = { route = Routes.Review(it) },
                onBack = {},
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
                onSaved = { route = Routes.Library },
                onBack = { route = Routes.Library },
            )
        }

        Routes.Search -> {
            val viewModelSearch: SearchViewModel = viewModel(factory = container.searchViewModelFactory)
            val viewModelCapture: CaptureViewModel = viewModel(factory = container.captureViewModelFactory)
            SearchRoute(
                viewModelSearch = viewModelSearch,
                userSetting = userSetting,
                onBack = { route = Routes.Library },
                onDocumentClick = { route = Routes.Detail(it) },
                onSettingClick = { route = Routes.Setting },
            )
            CaptureRoute(
                viewModel = viewModelCapture,
                onImageSelected = { route = Routes.Review(it) },
                onBack = {},
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
                onBack = { route = Routes.Library },
            )
        }

        Routes.ModelManager -> { /* reserved for future use */ }

        Routes.Setting -> {
            val viewModel: SettingViewModel = viewModel(factory = container.settingViewModelFactory)
            SettingRoute(
                viewModel = viewModel,
                onBack = { route = Routes.Library },
            )
        }
    }
}
