package com.example.personaldocumentproject.presentation.capture

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.ui.theme.*
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context.findActivity()
    var flashOn by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.toString()?.let(onImageSelected)
    }
    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = result.data?.let(GmsDocumentScanningResult::fromActivityResultIntent)
            val uri = scanResult?.pages?.firstOrNull()?.imageUri?.toString()
                ?: scanResult?.pdf?.uri?.toString()
            if (uri != null) onImageSelected(uri)
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
        GmsDocumentScanning.getClient(options)
            .getStartScanIntent(currentActivity)
            .addOnSuccessListener { sender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(sender).build())
            }
            .addOnFailureListener { picker.launch("image/*") }
    }

    Column(
        Modifier.fillMaxSize().background(AppBackground).statusBarsPadding().navigationBarsPadding()
    ) {
        Box(Modifier.weight(1f).fillMaxWidth().background(Color.Black)) {
            CameraScene()
            GuideFrame(Modifier.align(Alignment.Center).fillMaxWidth(.78f).aspectRatio(1.4f))
            Row(
                Modifier.align(Alignment.TopStart).padding(12.dp)
                    .background(Color.Black.copy(alpha = .58f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Back", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Position your document inside the frame",
                color = Color.White.copy(alpha = .75f),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp),
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 34.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallControl("▧", "Gallery", false) { picker.launch("image/*") }
            Box(
                Modifier.size(68.dp).border(3.dp, Color.White, CircleShape).clickable { launchScanner() },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(52.dp).background(Color.White, CircleShape))
            }
            SmallControl("ϟ", "Flash", flashOn) { flashOn = !flashOn }
        }
    }
}

@Composable
private fun CameraScene() = Canvas(Modifier.fillMaxSize()) {
    drawCircle(Color(0xFF252537), size.width * .65f, Offset(size.width * .15f, size.height * .2f))
    drawCircle(Color(0xFF191C25), size.width * .8f, Offset(size.width, size.height * .85f))
}

@Composable
private fun GuideFrame(modifier: Modifier) = Canvas(modifier) {
    drawRoundRect(
        Color.White.copy(alpha = .45f),
        style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
    )
}

@Composable
private fun SmallControl(symbol: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(48.dp)
                .background(if (active) AppPrimary.copy(alpha = .18f) else SurfaceRaised, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(symbol, color = if (active) AppPrimary else TextPrimary, fontSize = 20.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = TextDim, fontSize = 10.sp)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
