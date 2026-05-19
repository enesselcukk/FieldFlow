package com.example.presentation.auth.idscan.ocr

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import com.example.presentation.constants.IdScanTag
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

internal fun captureAndRunOcr(
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
