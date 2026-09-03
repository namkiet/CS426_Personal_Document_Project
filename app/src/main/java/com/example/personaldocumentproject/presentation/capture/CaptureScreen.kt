package com.example.personaldocumentproject.presentation.capture

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.personaldocumentproject.presentation.components.CaptureButton

@Composable
fun CaptureRoute(viewModel: CaptureViewModel, onImageSelected: (String) -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.selectedImageUri) {
        state.selectedImageUri?.let {
            onImageSelected(it)
            viewModel.onImageSelectionHandled()
        }
    }
    CaptureScreen2(onImageSelected = viewModel::selectImage, onBack = onBack)
}
//
//
////change this to capture button + animation
//@Composable
//fun CaptureScreen(onImageSelected: (String) -> Unit, onBack: () -> Unit) {
//    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri?.toString()?.let(onImageSelected)
//    }
//    Surface(modifier = Modifier.fillMaxHeight(0.3f), color = Color.Black) {
//        Column(
//            Modifier.fillMaxSize().padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp),
//        ) {
//            Text("Capture or import a document", color = Color.White)
//            ImageSourceButton { picker.launch("image/*") }
//            OutlinedButton(onClick = onBack) { Text("Back", color = Color.White) }
//        }
//    }
//}


@Composable
fun CaptureScreen2(onImageSelected: (String) -> Unit, onBack: () -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.toString()?.let(onImageSelected)
    }

    CaptureButton(
        onLeftHalfClick = {},
        onRightHalfClick = { picker.launch("image/*") },
        onClick = {}
    )
}