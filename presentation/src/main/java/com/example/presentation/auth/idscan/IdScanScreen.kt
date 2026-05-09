package com.example.presentation.auth.idscan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.IdentityInfo
import com.example.presentation.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

@Composable
fun IdScanScreen(
    onIdentityDetected: (IdentityInfo) -> Unit = {},
    viewModel: IdScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(hasCameraPermission, lifecycleOwner) {
        if (hasCameraPermission) {
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
                        imageCapture
                    )
                },
                executor
            )
        }
        onDispose {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }

    val ocrNotFoundMessage = stringResource(R.string.ocr_not_found)
    val cameraCaptureErrorPrefix = stringResource(R.string.camera_capture_error, "")
    val ocrFailedPrefix = stringResource(R.string.ocr_failed, "")
    val imageProcessingFailedMessage = stringResource(R.string.image_processing_failed)
    val unknownErrorMessage = stringResource(R.string.unknown_error)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.id_scan_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.id_scan_description)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasCameraPermission) {
            Text(text = stringResource(R.string.camera_permission_required))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text(stringResource(R.string.grant_camera_permission))
            }
        } else {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    viewModel.setLoading(true)
                    captureAndRunOcr(
                        context = context,
                        imageCapture = imageCapture,
                        onRawTextReady = { rawText ->
                            viewModel.onOcrSuccess(rawText, ocrNotFoundMessage)
                        },
                        onError = { errorMsg ->
                            viewModel.onOcrError(errorMsg)
                        },
                        cameraCaptureErrorPrefix = cameraCaptureErrorPrefix,
                        ocrFailedPrefix = ocrFailedPrefix,
                        imageProcessingFailedMessage = imageProcessingFailedMessage,
                        unknownErrorMessage = unknownErrorMessage
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState.isLoading) stringResource(R.string.scanning)
                    else stringResource(R.string.scan_id_card)
                )
            }
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.errorText?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text(stringResource(R.string.first_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.surname,
            onValueChange = viewModel::onSurnameChanged,
            label = { Text(stringResource(R.string.last_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onIdentityDetected(viewModel.buildIdentityInfo()) },
            enabled = uiState.name.isNotBlank() && uiState.surname.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.continue_button))
        }
    }
}

private fun captureAndRunOcr(
    context: Context,
    imageCapture: ImageCapture,
    onRawTextReady: (String) -> Unit,
    onError: (String) -> Unit,
    cameraCaptureErrorPrefix: String,
    ocrFailedPrefix: String,
    imageProcessingFailedMessage: String,
    unknownErrorMessage: String
) {
    val tempFile = File.createTempFile("id_scan_", ".jpg", context.cacheDir)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val uri = outputFileResults.savedUri ?: Uri.fromFile(tempFile)
                runOcr(
                    context = context,
                    uri = uri,
                    onRawTextReady = onRawTextReady,
                    onError = onError,
                    ocrFailedPrefix = ocrFailedPrefix,
                    imageProcessingFailedMessage = imageProcessingFailedMessage,
                    unknownErrorMessage = unknownErrorMessage
                )
            }

            override fun onError(exception: ImageCaptureException) {
                onError("$cameraCaptureErrorPrefix${exception.message ?: unknownErrorMessage}")
            }
        }
    )
}

private fun runOcr(
    context: Context,
    uri: Uri,
    onRawTextReady: (String) -> Unit,
    onError: (String) -> Unit,
    ocrFailedPrefix: String,
    imageProcessingFailedMessage: String,
    unknownErrorMessage: String
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                onRawTextReady(text.text)
                recognizer.close()
            }
            .addOnFailureListener { e ->
                onError("$ocrFailedPrefix${e.message ?: unknownErrorMessage}")
                recognizer.close()
            }
    } catch (_: Exception) {
        onError(imageProcessingFailedMessage)
    }
}
