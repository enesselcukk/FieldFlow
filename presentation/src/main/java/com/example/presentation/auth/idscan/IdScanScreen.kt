package com.example.presentation.auth.idscan

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.IdentityInfo
import com.example.presentation.R
import com.example.presentation.auth.idscan.components.IdScanCaptureContent
import com.example.presentation.auth.idscan.components.IdScanConfirmContent
import com.example.presentation.auth.idscan.ocr.captureAndRunOcr
import com.example.presentation.constants.IdScanPhase
import com.example.utils.permissions.AppRuntimePermissions
import com.example.utils.permissions.hasCameraPermission

@Composable
fun IdScanScreen(
    onIdentityDetected: (IdentityInfo) -> Unit = {},
    viewModel: IdScanViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setJpegQuality(95)
            .build()
    }

    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var phase by remember { mutableStateOf(IdScanPhase.Capture) }
    var pendingPostCapture by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading, uiState.errorText, uiState.name, uiState.surname) {
        if (!uiState.isLoading && pendingPostCapture) {
            pendingPostCapture = false
            val hasDetectedName = uiState.name.isNotBlank() || uiState.surname.isNotBlank()
            if (uiState.errorText == null && hasDetectedName) {
                phase = IdScanPhase.ConfirmDetails
            }
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(context.hasCameraPermission())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(AppRuntimePermissions.camera)
        }
    }

    DisposableEffect(hasCameraPermission, lifecycleOwner, phase) {
        if (hasCameraPermission && phase == IdScanPhase.Capture) {
            val executor = ContextCompat.getMainExecutor(context)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture,
                    )
                },
                executor,
            )
        }
        onDispose {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }

    BackHandler(enabled = phase == IdScanPhase.ConfirmDetails) {
        phase = IdScanPhase.Capture
        viewModel.clearDetectedIdentity()
    }

    val ocrNotFoundMessage = stringResource(R.string.ocr_not_found)
    val photoCaptureFailedMessage = stringResource(R.string.id_scan_photo_capture_failed)
    val textReadFailedMessage = stringResource(R.string.id_scan_text_read_failed)
    val imageProcessingFailedMessage = stringResource(R.string.image_processing_failed)

    when (phase) {
        IdScanPhase.Capture -> {
            IdScanCaptureContent(
                modifier = Modifier.fillMaxSize(),
                hasCameraPermission = hasCameraPermission,
                previewView = previewView,
                uiState = uiState,
                onRequestPermission = { permissionLauncher.launch(AppRuntimePermissions.camera) },
                onEnterManually = {
                    viewModel.clearDetectedIdentity()
                    phase = IdScanPhase.ConfirmDetails
                },
                onCapture = {
                    pendingPostCapture = true
                    viewModel.setLoading(true)
                    captureAndRunOcr(
                        context = context,
                        imageCapture = imageCapture,
                        onRawTextReady = { rawText ->
                            viewModel.onOcrSuccess(rawText, ocrNotFoundMessage)
                        },
                        onError = { errorMsg ->
                            pendingPostCapture = false
                            viewModel.onOcrError(errorMsg)
                        },
                        photoCaptureFailedMessage = photoCaptureFailedMessage,
                        textReadFailedMessage = textReadFailedMessage,
                        imageProcessingFailedMessage = imageProcessingFailedMessage,
                    )
                },
            )
        }

        IdScanPhase.ConfirmDetails -> {
            IdScanConfirmContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onNameChange = viewModel::onNameChanged,
                onSurnameChange = viewModel::onSurnameChanged,
                onContinue = { onIdentityDetected(viewModel.buildIdentityInfo()) },
                onScanAgain = {
                    viewModel.clearDetectedIdentity()
                    phase = IdScanPhase.Capture
                },
            )
        }
    }
}
