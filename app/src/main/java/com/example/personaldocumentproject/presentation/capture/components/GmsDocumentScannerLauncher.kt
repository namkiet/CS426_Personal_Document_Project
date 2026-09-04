package com.example.personaldocumentproject.presentation.capture.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

class DocumentScannerState(
    private val activity: Activity?,
    private val onScanSuccess: (String) -> Unit,
    private val onFallback: () -> Unit,
    private val launchIntentSender: (IntentSenderRequest) -> Unit
) {
    fun launch() {
        val currentActivity = activity ?: run {
            onFallback()
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
                    launchIntentSender(IntentSenderRequest.Builder(intentSender).build())
                }.onFailure {
                    onFallback()
                }
            }
            .addOnFailureListener {
                onFallback()
            }
    }
}

@Composable
fun rememberDocumentScanner(
    onScanSuccess: (String) -> Unit,
    onFallback: () -> Unit
): DocumentScannerState {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

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
                onScanSuccess(selectedUri)
            } else {
                onFallback()
            }
        }
    }

    return remember(activity, onScanSuccess, onFallback) {
        DocumentScannerState(
            activity = activity,
            onScanSuccess = onScanSuccess,
            onFallback = onFallback,
            launchIntentSender = { intentSenderRequest ->
                scannerLauncher.launch(intentSenderRequest)
            }
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}