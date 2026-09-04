package com.example.personaldocumentproject.presentation.capture

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.personaldocumentproject.ui.theme.*
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
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
    CaptureScreen(viewModel::selectImage, onBack)
}

@Composable
fun CaptureScreen(onImageSelected: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageSelected(uri.toString()) else onBack()
    }
    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = runCatching {
                result.data?.let(GmsDocumentScanningResult::fromActivityResultIntent)
            }.getOrNull()
            val uri = scanResult?.pages?.firstOrNull()?.imageUri?.toString()
            if (uri != null) onImageSelected(uri) else onBack()
        } else onBack()
    }

    fun launchScanner() {
        val currentActivity = activity ?: run {
            picker.launch("image/*")
            return
        }
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options).getStartScanIntent(currentActivity)
            .addOnSuccessListener { sender ->
                runCatching {
                    scannerLauncher.launch(IntentSenderRequest.Builder(sender).build())
                }.onFailure { picker.launch("image/*") }
            }
            .addOnFailureListener { picker.launch("image/*") }
    }

    LaunchedEffect(Unit) { launchScanner() }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppPrimary)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
