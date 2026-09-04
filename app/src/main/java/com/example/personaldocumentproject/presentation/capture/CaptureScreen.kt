package com.example.personaldocumentproject.presentation.capture

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.personaldocumentproject.presentation.capture.components.ImportImageButton
import com.example.personaldocumentproject.presentation.capture.components.ScanDocumentButton
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
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
fun CaptureScreen(onImageSelected: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()

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
