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

@Composable
fun CaptureRoute(viewModel: CaptureViewModel, onImageSelected: (String) -> Unit, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.selectedImageUri) {
        state.selectedImageUri?.let {
            onImageSelected(it)
            viewModel.onImageSelectionHandled()
        }
    }
    CaptureScreen(onImageSelected = viewModel::selectImage, onBack = onBack)
}

@Composable
fun CaptureScreen(onImageSelected: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.toString()?.let(onImageSelected)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = runCatching {
                result.data?.let { intent ->
                    GmsDocumentScanningResult.fromActivityResultIntent(intent)
                }
            }.getOrNull()

            val selectedUri = scanResult?.pages?.firstOrNull()?.imageUri?.toString()
                ?: scanResult?.pdf?.uri?.toString()

            if (selectedUri != null) {
                onImageSelected(selectedUri)
            } else {
                picker.launch("image/*")
            }
        }
    }

    fun launchScanner() {
        val currentActivity = activity ?: run {
            picker.launch("image/*")
            return
        }

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()

        GmsDocumentScanning.getClient(options).getStartScanIntent(currentActivity)
            .addOnSuccessListener { intentSender ->
                runCatching {
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }.onFailure {
                    picker.launch("image/*")
                }
            }
            .addOnFailureListener {
                // Fallback to basic picker if scanner fails to initialize
                picker.launch("image/*")
            }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Capture or import a document", color = Color.White)
            ScanDocumentButton { launchScanner() }
            ImportImageButton { picker.launch("image/*") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back", color = Color.White) }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
