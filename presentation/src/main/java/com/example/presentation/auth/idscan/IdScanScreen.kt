package com.example.presentation.auth.idscan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.IdentityInfo
import com.example.presentation.R
import com.example.presentation.constants.IdCardAspectWidthOverHeight
import com.example.presentation.constants.IdScanPhase
import com.example.presentation.constants.IdScanTag
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

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
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
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
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
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

@Composable
private fun IdScanCaptureContent(
    modifier: Modifier = Modifier,
    hasCameraPermission: Boolean,
    previewView: PreviewView,
    uiState: IdScanUiState,
    onRequestPermission: () -> Unit,
    onEnterManually: () -> Unit,
    onCapture: () -> Unit,
) {
    Column(modifier = modifier) {
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.camera_permission_required),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRequestPermission) {
                    Text(stringResource(R.string.grant_camera_permission))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize(),
                )

                IdCardViewfinderOverlay(
                    modifier = Modifier.fillMaxSize(),
                    frameStrokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f),
                ) {
                    Text(
                        text = stringResource(R.string.id_scan_frame_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.errorText?.let { err ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(14.dp),
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = onCapture,
                        modifier = Modifier.size(72.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = stringResource(R.string.scan_id_card),
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    TextButton(onClick = onEnterManually) {
                        Text(
                            text = stringResource(R.string.id_scan_enter_manually),
                            color = Color.White.copy(alpha = 0.88f),
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text(
                        text = stringResource(R.string.id_scan_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun IdCardViewfinderOverlay(
    modifier: Modifier = Modifier,
    frameStrokeColor: Color,
) {
    Canvas(modifier = modifier) {
        val cornerPx = 20.dp.toPx()
        val cornerRadius = CornerRadius(cornerPx, cornerPx)
        val margin = 28.dp.toPx()
        val maxW = size.width - margin * 2f
        val maxH = size.height - margin * 2f

        var frameWidth = maxW * 0.9f
        var frameHeight = frameWidth / IdCardAspectWidthOverHeight
        if (frameHeight > maxH * 0.65f) {
            frameHeight = maxH * 0.65f
            frameWidth = frameHeight * IdCardAspectWidthOverHeight
        }

        val left = (size.width - frameWidth) / 2f
        val top = (size.height - frameHeight) / 2f
        val roundRect = RoundRect(
            left = left,
            top = top,
            right = left + frameWidth,
            bottom = top + frameHeight,
            cornerRadius = cornerRadius,
        )

        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, this@Canvas.size))
            addRoundRect(roundRect)
        }

        drawPath(path, color = Color.Black.copy(alpha = 0.55f))

        drawRoundRect(
            color = frameStrokeColor,
            topLeft = Offset(left, top),
            size = Size(frameWidth, frameHeight),
            cornerRadius = cornerRadius,
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}

@Composable
private fun IdScanConfirmContent(
    modifier: Modifier = Modifier,
    uiState: IdScanUiState,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onScanAgain: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val canContinue = uiState.name.isNotBlank() && uiState.surname.isNotBlank()

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 16.dp),
        ) {
            val fieldShape = RoundedCornerShape(14.dp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.first_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = fieldShape,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        colors = outlinedFieldColorsStrongLabel(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    )
                    OutlinedTextField(
                        value = uiState.surname,
                        onValueChange = onSurnameChange,
                        label = { Text(stringResource(R.string.last_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = fieldShape,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        colors = outlinedFieldColorsStrongLabel(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                                if (canContinue) onContinue()
                            },
                        ),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            val buttonShape = RoundedCornerShape(16.dp)

            Button(
                onClick = {
                    keyboard?.hide()
                    onContinue()
                },
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(stringResource(R.string.continue_button))
            }

            OutlinedButton(
                onClick = {
                    keyboard?.hide()
                    onScanAgain()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = buttonShape,
            ) {
                Text(stringResource(R.string.id_scan_scan_again))
            }
        }
    }
}

@Composable
private fun outlinedFieldColorsStrongLabel(): TextFieldColors {
    val scheme = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedLabelColor = scheme.primary,
        unfocusedLabelColor = scheme.onSurfaceVariant,
        focusedBorderColor = scheme.primary,
        unfocusedBorderColor = scheme.outline,
        unfocusedTextColor = scheme.onSurface,
        focusedTextColor = scheme.onSurface,
    )
}

private fun captureAndRunOcr(
    context: Context,
    imageCapture: ImageCapture,
    onRawTextReady: (String) -> Unit,
    onError: (String) -> Unit,
    photoCaptureFailedMessage: String,
    textReadFailedMessage: String,
    imageProcessingFailedMessage: String,
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
                    textReadFailedMessage = textReadFailedMessage,
                    imageProcessingFailedMessage = imageProcessingFailedMessage,
                )
            }

            override fun onError(exception: ImageCaptureException) {
                Log.w(
                    IdScanTag,
                    "Image capture failed; imageCaptureError=${exception.imageCaptureError}",
                    exception,
                )
                onError(photoCaptureFailedMessage)
            }
        },
    )
}

private fun runOcr(
    context: Context,
    uri: Uri,
    onRawTextReady: (String) -> Unit,
    onError: (String) -> Unit,
    textReadFailedMessage: String,
    imageProcessingFailedMessage: String,
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { vision ->
                onRawTextReady(vision.extractTextForIdentityParsing())
                recognizer.close()
            }
            .addOnFailureListener { e ->
                Log.w(IdScanTag, "ML Kit TextRecognition failed", e)
                onError(textReadFailedMessage)
                recognizer.close()
            }
    } catch (e: Exception) {
        Log.w(IdScanTag, "InputImage or OCR setup failed", e)
        onError(imageProcessingFailedMessage)
    }
}
