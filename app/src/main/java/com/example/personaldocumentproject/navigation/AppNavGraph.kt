package com.example.personaldocumentproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personaldocumentproject.di.AppContainer
import com.example.personaldocumentproject.presentation.capture.CaptureRoute
import com.example.personaldocumentproject.presentation.capture.CaptureViewModel
import com.example.personaldocumentproject.presentation.library.LibraryRoute
import com.example.personaldocumentproject.presentation.library.LibraryViewModel
import com.example.personaldocumentproject.presentation.review.ReviewRoute
import com.example.personaldocumentproject.presentation.review.ReviewViewModel
import com.example.personaldocumentproject.presentation.search.SearchRoute
import com.example.personaldocumentproject.presentation.search.SearchViewModel

@Composable
fun AppNavGraph(container: AppContainer) {
    var route: Routes by remember { mutableStateOf(Routes.Library) }
    when (val current = route) {
        Routes.Library -> {
            val viewModel: LibraryViewModel = viewModel(factory = container.libraryViewModelFactory)
            LibraryRoute(
                viewModel = viewModel,
                onCapture = { route = Routes.Capture },
                onSearch = { route = Routes.Search },
            )
        }
        Routes.Capture -> {
            val viewModel: CaptureViewModel = viewModel(factory = container.captureViewModelFactory)
            CaptureRoute(
                viewModel = viewModel,
                onImageSelected = { route = Routes.Review(it) },
                onBack = { route = Routes.Library },
            )
        }
        is Routes.Review -> {
            val viewModel: ReviewViewModel = viewModel(
                key = "review:${current.imageUri}",
                factory = container.reviewViewModelFactory,
            )
            ReviewRoute(
                imageUri = current.imageUri,
                viewModel = viewModel,
                onSaved = { route = Routes.Library },
                onBack = { route = Routes.Capture },
            )
        }
        Routes.Search -> {
            val viewModel: SearchViewModel = viewModel(factory = container.searchViewModelFactory)
            SearchRoute(viewModel, onBack = { route = Routes.Library })
        }
    }
}
