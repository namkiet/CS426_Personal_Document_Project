package com.example.personaldocumentproject.presentation.capture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldocumentproject.ui.theme.*
import kotlinx.coroutines.delay

enum class CaptureStage { CAMERA, PREVIEW, PROCESSING }

@Composable
fun CaptureRoute(viewModel: CaptureViewModel, onImageSelected: (String) -> Unit, onBack: () -> Unit) {
    CaptureScreen(onAnalyzed = { viewModel.selectImage("mock://captured-document") }, onBack = onBack)
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.selectedImageUri) { state.selectedImageUri?.let { onImageSelected(it); viewModel.onImageSelectionHandled() } }
}

@Composable
fun CaptureScreen(onAnalyzed: () -> Unit, onBack: () -> Unit, initialStage: CaptureStage = CaptureStage.CAMERA) {
    var stage by remember { mutableStateOf(initialStage) }
    var flashOn by remember { mutableStateOf(false) }
    LaunchedEffect(stage) { if (stage == CaptureStage.PROCESSING) { delay(1400); onAnalyzed() } }
    Column(
        Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(4.dp)) {
                Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(6.dp))
                Text("Add Document", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF111218))) {
            MockCameraScene()
            if (stage == CaptureStage.CAMERA) GuideFrame(Modifier.align(Alignment.Center).size(280.dp, 200.dp))
            if (stage != CaptureStage.CAMERA) MockCapturedPaper(Modifier.align(Alignment.Center).fillMaxWidth(.86f).aspectRatio(3f / 4f))
            if (stage == CaptureStage.PROCESSING) ProcessingOverlay()
        }
        if (stage == CaptureStage.CAMERA) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 34.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                SmallControl("▧", "Gallery", false) { stage = CaptureStage.PREVIEW }
                Box(Modifier.size(68.dp).border(3.dp, Color.White, CircleShape).clickable { stage = CaptureStage.PREVIEW }, contentAlignment = Alignment.Center) {
                    Box(Modifier.size(52.dp).background(Color.White, CircleShape))
                }
                SmallControl("ϟ", "Flash", flashOn) { flashOn = !flashOn }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { stage = CaptureStage.CAMERA }, modifier = Modifier.weight(1f), enabled = stage != CaptureStage.PROCESSING,
                    shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)) { Text("Retake") }
                Button(onClick = { stage = CaptureStage.PROCESSING }, modifier = Modifier.weight(2f), enabled = stage != CaptureStage.PROCESSING,
                    shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)) { Text("Analyze  →") }
            }
        }
    }
}

@Composable private fun MockCameraScene() = Canvas(Modifier.fillMaxSize()) {
    drawCircle(Color(0xFF252537), size.width * .65f, Offset(size.width * .15f, size.height * .2f))
    drawCircle(Color(0xFF191C25), size.width * .8f, Offset(size.width, size.height * .85f))
}

@Composable private fun GuideFrame(modifier: Modifier) = Canvas(modifier) {
    drawRoundRect(Color.White.copy(alpha = .45f), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))), cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()))
    val l = 30.dp.toPx(); val w = 3.dp.toPx()
    drawLine(Color.White, Offset(0f, l), Offset(0f, 0f), w); drawLine(Color.White, Offset(0f, 0f), Offset(l, 0f), w)
    drawLine(Color.White, Offset(size.width-l, size.height), Offset(size.width, size.height), w); drawLine(Color.White, Offset(size.width, size.height-l), Offset(size.width, size.height), w)
}

@Composable private fun MockCapturedPaper(modifier: Modifier) = Column(modifier.background(Color(0xFFE8E5DD), RoundedCornerShape(4.dp)).padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Box(Modifier.fillMaxWidth(.65f).height(14.dp).background(Color(0xFF30303B)))
    repeat(8) { Box(Modifier.fillMaxWidth(if (it % 3 == 0) .72f else 1f).height(5.dp).background(Color(0xFF9997A0))) }
    Spacer(Modifier.height(12.dp)); Box(Modifier.fillMaxWidth().height(90.dp).background(AppPrimary.copy(alpha = .18f)))
}

@Composable private fun ProcessingOverlay() = Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .68f)), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(44.dp)); Spacer(Modifier.height(18.dp))
        Text("Analyzing document...", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp)); Text("Extracting text", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable private fun SmallControl(symbol: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).background(if (active) AppPrimary.copy(alpha = .18f) else SurfaceRaised, RoundedCornerShape(14.dp))
            .border(1.dp, if (active) AppPrimary else AppBorder, RoundedCornerShape(14.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Text(symbol, color = if (active) AppPrimary else TextPrimary, fontSize = 22.sp)
        }
        Spacer(Modifier.height(6.dp)); Text(label, color = TextDim, fontSize = 10.sp)
    }
}

@Preview(name="Capture - Camera", showBackground=true, widthDp=390, heightDp=844) @Composable private fun CameraPreview() = PersonalDocumentProjectTheme { CaptureScreen({}, {}) }
@Preview(name="Capture - Image Preview", showBackground=true, widthDp=390, heightDp=844) @Composable private fun ImagePreview() = PersonalDocumentProjectTheme { CaptureScreen({}, {}, CaptureStage.PREVIEW) }
@Preview(name="Capture - Processing", showBackground=true, widthDp=390, heightDp=844) @Composable private fun ProcessingPreview() = PersonalDocumentProjectTheme { CaptureScreen({}, {}, CaptureStage.PROCESSING) }
