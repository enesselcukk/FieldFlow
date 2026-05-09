package com.example.presentation.auth

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
import com.example.domain.model.IdentityInfo
import com.example.presentation.R
import com.example.utils.extensions.containsLabelKey
import com.example.utils.extensions.isIdentityLabel
import com.example.utils.extensions.sanitizeIdentityCandidate
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

@Composable
fun IdScanScreen(
    onIdentityDetected: (IdentityInfo) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }

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
                    isLoading = true
                    errorText = null
                    captureAndRunOcr(
                        context = context,
                        imageCapture = imageCapture,
                        onSuccess = { detected ->
                            isLoading = false
                            name = detected.name
                            surname = detected.surname
                        },
                        onError = {
                            isLoading = false
                            errorText = it
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isLoading) stringResource(R.string.scanning)
                    else stringResource(R.string.scan_id_card)
                )
            }
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        errorText?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.first_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text(stringResource(R.string.last_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                onIdentityDetected(IdentityInfo(name = name.trim(), surname = surname.trim()))
            },
            enabled = name.isNotBlank() && surname.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.continue_button))
        }
    }
}

private fun captureAndRunOcr(
    context: Context,
    imageCapture: ImageCapture,
    onSuccess: (IdentityInfo) -> Unit,
    onError: (String) -> Unit
) {
    val tempFile = File.createTempFile("id_scan_", ".jpg", context.cacheDir)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val uri = outputFileResults.savedUri ?: Uri.fromFile(tempFile)
                runOcr(context = context, uri = uri, onSuccess = onSuccess, onError = onError)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(
                    context.getString(
                        R.string.camera_capture_error,
                        exception.message ?: context.getString(R.string.unknown_error)
                    )
                )
            }
        }
    )
}

private fun runOcr(
    context: Context,
    uri: Uri,
    onSuccess: (IdentityInfo) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                val parsed = IdentityTextParser.parse(text.text)
                if (parsed.name.isBlank() && parsed.surname.isBlank()) {
                    onError(context.getString(R.string.ocr_not_found))
                } else {
                    onSuccess(parsed)
                }
                recognizer.close()
            }
            .addOnFailureListener { e ->
                onError(
                    context.getString(
                        R.string.ocr_failed,
                        e.message ?: context.getString(R.string.unknown_error)
                    )
                )
                recognizer.close()
            }
    } catch (_: Exception) {
        onError(context.getString(R.string.image_processing_failed))
    }
}

private object IdentityTextParser {
    private val allLabelKeys = listOf(
        "ADI", "ADI SOYADI", "GIVEN NAME", "GIVEN NAMES", "GIVEN NAME(S)", "NAME",
        "SOYADI", "SURNAME", "LAST NAME"
    )

    fun parse(rawText: String): IdentityInfo {
        val lines = rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val name = findValue(lines, listOf("ADI", "GIVEN NAME", "GIVEN NAMES", "GIVEN NAME(S)"))
        val surname = findValue(lines, listOf("SOYADI", "SURNAME", "LAST NAME"))

        return IdentityInfo(
            name = name.orEmpty(),
            surname = surname.orEmpty()
        )
    }

    private fun findValue(lines: List<String>, keys: List<String>): String? {
        val index = lines.indexOfFirst { line ->
            keys.any { key -> line.containsLabelKey(key) }
        }
        if (index == -1) return null

        val nextLine = lines.getOrNull(index + 1)?.trim().orEmpty().sanitizeIdentityCandidate()
        if (nextLine.isNotBlank() && !looksLikeLabel(nextLine)) {
            return nextLine
        }
        return null
    }

    private fun looksLikeLabel(value: String): Boolean {
        return value.isIdentityLabel(allLabelKeys)
    }
}
