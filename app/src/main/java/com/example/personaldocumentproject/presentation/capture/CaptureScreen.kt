package com.example.personaldocumentproject.presentation.capture

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.personaldocumentproject.ui.responsive.ScreenBreakpoint
import com.example.personaldocumentproject.ui.responsive.screenSizeInfo
import com.example.personaldocumentproject.ui.theme.*
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun CaptureRoute(
    viewModel: CaptureViewModel,
    onImageSelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.selectedImageUri) {
        state.selectedImageUri?.let {
            onImageSelected(it)
            viewModel.onImageSelectionHandled()
        }
    }

    CaptureScreen(
        state = state,
        onImageSelected = viewModel::selectImage,
        onBack = onBack,
        onError = viewModel::setError,
        onClearError = viewModel::clearError,
        setLoading = viewModel::setLoading,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    state: CaptureUiState,
    onImageSelected: (String) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit = {},
    onClearError: () -> Unit = {},
    setLoading: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        setLoading(false)
        if (uri != null) {
            onImageSelected(uri.toString())
        }
    }

    val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            picker.launch("image/*")
        } else {
            onError("Permission to access photos was denied. Please allow photo access in settings.")
        }
    }

    fun openGallery() {
        onClearError()
        // On Android 13+ Photo Picker / GetContent doesn't strictly block without runtime permission,
        // but if permission is required or on older SDKs, we verify and request appropriately.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, mediaPermission) == PackageManager.PERMISSION_GRANTED) {
            picker.launch("image/*")
        } else {
            permissionLauncher.launch(mediaPermission)
        }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        setLoading(false)
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = runCatching {
                result.data?.let(GmsDocumentScanningResult::fromActivityResultIntent)
            }.getOrNull()
            val uri = scanResult?.pages?.firstOrNull()?.imageUri?.toString()
            if (uri != null) {
                onImageSelected(uri)
            } else {
                onError("Could not retrieve scanned document image.")
            }
        }
    }

    fun launchScanner() {
        setLoading(true)
        onClearError()
        val currentActivity = activity ?: run {
            setLoading(false)
            picker.launch("image/*")
            return
        }
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options).getStartScanIntent(currentActivity)
            .addOnSuccessListener { sender ->
                runCatching {
                    scannerLauncher.launch(IntentSenderRequest.Builder(sender).build())
                }.onFailure {
                    setLoading(false)
                    onError("Document scanner intent failed, falling back to gallery.")
                    picker.launch("image/*")
                }
            }
            .addOnFailureListener { error ->
                setLoading(false)
                onError("Scanner unavailable: ${error.message ?: "Opening gallery..."}")
                picker.launch("image/*")
            }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val breakpoint = screenSizeInfo().breakpoint
        val isWide = breakpoint != ScreenBreakpoint.COMPACT
        val horizontalPadding = when (breakpoint) {
            ScreenBreakpoint.COMPACT -> 20.dp
            ScreenBreakpoint.MEDIUM -> 28.dp
            ScreenBreakpoint.EXPANDED -> 32.dp
        }

        Scaffold(
            containerColor = AppBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Capture Document", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Add a document to extract text with metadata and enable private semantic search.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )

                    state.errorMessage?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeleteColor.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = msg,
                                    color = DeleteColor,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = onClearError) {
                                    Text("Dismiss", color = DeleteColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            CaptureActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.DocumentScanner,
                                iconTint = AppPrimary,
                                title = "Scan Document",
                                subtitle = "Use camera with auto-edge detection, crop, and enhanced contrast for documents.",
                                onClick = { launchScanner() },
                            )
                            CaptureActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.PhotoLibrary,
                                iconTint = StudyColor,
                                title = "Import from Gallery",
                                subtitle = "Select an existing photo, scanned file, or screenshot from your device.",
                                onClick = { openGallery() },
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CaptureActionCard(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.DocumentScanner,
                                iconTint = AppPrimary,
                                title = "Scan Document",
                                subtitle = "Use camera with auto-edge detection, crop, and enhanced contrast for documents.",
                                onClick = { launchScanner() },
                            )
                            CaptureActionCard(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.PhotoLibrary,
                                iconTint = StudyColor,
                                title = "Import from Gallery",
                                subtitle = "Select an existing photo, scanned file, or screenshot from your device.",
                                onClick = { openGallery() },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceRaised.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Tips for best OCR accuracy",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                )
                                Text(
                                    text = "• Place document on a flat, contrasting background.\n• Ensure adequate lighting and avoid strong glare.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                )
                            }
                        }
                    }
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = AppPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AppBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview(name = "Capture Screen - Dark", showBackground = true, backgroundColor = 0xFF0D0D12, widthDp = 390, heightDp = 844)
@Composable
private fun CaptureScreenPreview() {
    PersonalDocumentProjectTheme {
        CaptureScreen(
            state = CaptureUiState(),
            onImageSelected = {},
            onBack = {},
        )
    }
}

