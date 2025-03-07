package com.edgetech.bbscout.features.capture.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.activity.compose.LocalActivity
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.edgetech.bbscout.components.location.AppLocation
import com.edgetech.bbscout.components.utils.logD
import com.edgetech.bbscout.components.utils.toLong
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
//import com.edgetech.bbscout.features.capture.BillboardDetectionResult
//import com.edgetech.bbscout.features.capture.detectBillboardWithText
import com.edgetech.bbscout.features.capture.domain.model.DetectedObjectWithLabels
import com.edgetech.bbscout.features.capture.domain.model.EntityInfo
import com.example.core.core.utils.components.LocationAwareActivity
import com.example.core.core.utils.components.toLatLng
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
//import com.google.mlkit.common.model.DownloadConditions
//import com.google.mlkit.nl.entityextraction.DateTimeEntity
//import com.google.mlkit.nl.entityextraction.Entity
//import com.google.mlkit.nl.entityextraction.EntityExtractionParams
//import com.google.mlkit.nl.entityextraction.EntityExtractor
//import com.google.mlkit.nl.entityextraction.MoneyEntity
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.objects.DetectedObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.Executor
import kotlin.math.max
import kotlin.math.min

// Entity extraction helper
//suspend fun extractEntities(
//    extractor: EntityExtractor,
//    text: String,
//    onResult: (List<EntityInfo>) -> Unit
//) = withContext(Dispatchers.IO) {
//    // Download the model if needed
//    val downloadConditions = DownloadConditions.Builder()
//        .requireWifi()
//        .build()
//
//    try {
//        extractor.downloadModelIfNeeded(downloadConditions)
//            .addOnSuccessListener {
//                // Model is ready, extract entities
//                val params = EntityExtractionParams.Builder(text)
//                    .setEntityTypesFilter(
//                        setOf(
//                            Entity.TYPE_ADDRESS,
//                            Entity.TYPE_DATE_TIME,
//                            Entity.TYPE_EMAIL,
//                            Entity.TYPE_PHONE,
//                            Entity.TYPE_URL,
//                            Entity.TYPE_PAYMENT_CARD,
//                            Entity.TYPE_TRACKING_NUMBER,
//                            Entity.TYPE_MONEY,
//                            Entity.TYPE_FLIGHT_NUMBER
//                        )
//                    )
//                    .build()
//
//                extractor.annotate(params)
//                    .addOnSuccessListener { entityAnnotations ->
//                        val results = mutableListOf<EntityInfo>()
//
//                        for (entityAnnotation in entityAnnotations) {
//                            when (val entity = entityAnnotation.entities.firstOrNull()) {
//                                is DateTimeEntity -> {
//                                    results.add(EntityInfo("DATE_TIME", entityAnnotation.annotatedText))
//                                }
//                                is MoneyEntity -> {
//                                    results.add(EntityInfo("MONEY", entityAnnotation.annotatedText))
//                                }
//                                else -> {
//                                    val type = when (entity?.type) {
//                                        Entity.TYPE_ADDRESS -> "ADDRESS"
//                                        Entity.TYPE_EMAIL -> "EMAIL"
//                                        Entity.TYPE_PHONE -> "PHONE"
//                                        Entity.TYPE_URL -> "URL"
//                                        Entity.TYPE_PAYMENT_CARD -> "PAYMENT_CARD"
//                                        Entity.TYPE_TRACKING_NUMBER -> "TRACKING_NUMBER"
//                                        Entity.TYPE_FLIGHT_NUMBER -> "FLIGHT_NUMBER"
//                                        else -> "UNKNOWN"
//                                    }
//                                    results.add(EntityInfo(type, entityAnnotation.annotatedText))
//                                }
//                            }
//                        }
//
//                        onResult(results)
//                    }
//                    .addOnFailureListener {
//                        onResult(emptyList())
//                    }
//            }
//            .addOnFailureListener {
//                onResult(emptyList())
//            }
//    } catch (e: Exception) {
//        onResult(emptyList())
//    }
//}


/**
 * try to detect the billboard
 * As the big square object
 */

//private suspend fun detectBillboard(
//    detectedObjects: List<DetectedObject>,
//    bitmap: Bitmap,
//    rotation: Int,
//    textRecognizer: com.google.mlkit.vision.text.TextRecognizer
//): BillboardDetectionResult = withContext(Dispatchers.Default) {
//    // Filter for objects that are likely billboards (rectangular objects with certain constraints)
//    val potentialBillboards = detectedObjects.filter { obj ->
//        val boundingBox = obj.boundingBox
//        val aspectRatio = boundingBox.width().toFloat() / boundingBox.height().toFloat()
//
//        // Billboards typically have aspect ratios between 0.5:1 and 4:1
//        aspectRatio in 0.5f..4f &&
//                boundingBox.width() > bitmap.width * 0.15 && // At least 15% of image width
//                boundingBox.height() > bitmap.height * 0.15   // At least 15% of image height
//    }
//
//    if (potentialBillboards.isNotEmpty()) {
//        // Take the largest potential billboard
//        val billboard = potentialBillboards.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
//
//        billboard?.let {
//            val boundingBox = it.boundingBox
//            val rotatedBitmap = rotateBitmap(bitmap, rotation.toFloat())
//
//            // Ensure bounding box is within bitmap bounds
//            val left = max(0, boundingBox.left)
//            val top = max(0, boundingBox.top)
//            val right = min(rotatedBitmap.width, boundingBox.right)
//            val bottom = min(rotatedBitmap.height, boundingBox.bottom)
//
//            if (right > left && bottom > top) {
//                try {
//                    val croppedBitmap = Bitmap.createBitmap(
//                        rotatedBitmap,
//                        left,
//                        top,
//                        right - left,
//                        bottom - top
//                    )
//
//                    // Check if the image is clear (brightness/contrast check)
//                    val isImageClear = isImageClear(croppedBitmap)
//
//                    // Perform text recognition on the cropped bitmap
//                    val textResult = withContext(Dispatchers.IO) {
//                        Tasks.await( textRecognizer.process(InputImage.fromBitmap(croppedBitmap, 0)))
//                    }
//
//
//
//                    // Determine detection message
//                    val message = when {
//                        !isImageClear -> "Image is not clear enough - adjust lighting or distance"
//                        else -> "Press capture to to review image"
//                    }
//
//                    return@withContext BillboardDetectionResult(croppedBitmap, isImageClear,  message)
//                } catch (e: Exception) {
//                    Log.e("BillboardDetector", "Error processing billboard", e)
//                }
//            }
//        }
//    }
//
//    return@withContext BillboardDetectionResult(null, false,  "Point camera at a billboard")
//}

// To check if the cropped image is clear

private fun isImageClear(bitmap: Bitmap): Boolean {
    var totalBrightness = 0L
    var pixelCount = 0

    // Sample the image (every 10th pixel for performance)
    for (x in 0 until bitmap.width step 10) {
        for (y in 0 until bitmap.height step 10) {
            val pixel = bitmap.getPixel(x, y)
            val r = (pixel shr 16) and 0xff
            val g = (pixel shr 8) and 0xff
            val b = pixel and 0xff
            totalBrightness += (r + g + b) / 3
            pixelCount++
        }
    }

    val avgBrightness = totalBrightness.toFloat() / pixelCount

    // Check for adequate brightness and contrast
    return avgBrightness > 40 && avgBrightness < 215 // Not too dark, not too bright
}

private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Float): Bitmap {
    if (rotationDegrees == 0f) return bitmap

    val matrix = Matrix()
    matrix.postRotate(rotationDegrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

//To enable zoom on the preview image
@SuppressLint("ClickableViewAccessibility")
fun setupZoomListener(context: Context, camera: Camera, previewView: PreviewView) {
    val scaleGestureDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera.cameraInfo.zoomState?.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor
                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        })

    previewView.setOnTouchListener { _, event ->
        scaleGestureDetector.onTouchEvent(event)
        return@setOnTouchListener true
    }
}

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (android.net.Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // Create timestamped output file


    // Create output options
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            File.createTempFile(
                "JPEG_${LocalDateTime.now().toLong()}_", /* prefix */
                ".jpg", /* suffix */
                context.cacheDir /* directory */
            )
        )
        .build()

    // Take the picture
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { uri ->
                    onImageCaptured(uri)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}


//val imageAnalysis = ImageAnalysis.Builder()
//    //.setTargetResolution(Size(640, 480))
//    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//    .build()
//val frameInterval = 2000L // 5 seconds in milliseconds
//var lastAnalyzedTimestamp = 0L
//
//imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
//    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//    val image = imageProxy.image
//
//    val currentTimestamp = System.currentTimeMillis()
//
//    if (image != null && currentTimestamp - lastAnalyzedTimestamp >= frameInterval) {
//        val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
//
//        try {
//            objectDetector.process(inputImage)
//                .addOnSuccessListener { objects ->
//                    Log.d("ObjectDetection", "Objects detected: ${objects.size}")
//                    detectedObjects =
//                        objects.map { DetectedObjectWithLabels.fromDetectedObject(it) }
//                    lastCapturedBitmap = imageProxy.toBitmap()
//                    coroutineScope.launch(Dispatchers.IO) {
//                        val (detectedBitmap, isCleared, message) = detectBillboardWithText(
//                            objects,
//                            lastCapturedBitmap!!,
//                            rotationDegrees,
//                            textRecognizer
//                        )
//                        withContext(Dispatchers.Main) {
//                            detectedBitmap?.let {
//                                croppedBitmap = it
//                            }
//                        }
//                    }
//                }.addOnFailureListener {
//                    Log.e("ObjectDetection", "Error detecting objects", it)
//                }
//                .addOnCompleteListener { ob ->
//                    barcodeScanner.process(inputImage)
//                        .addOnSuccessListener { codes ->
//                            barcodes = codes.mapNotNull { it.displayValue }
//                        }
//                        .addOnCompleteListener {
//                            // Process with text recognizer
//                            textRecognizer.process(inputImage)
//                                .addOnSuccessListener { visionText ->
//                                    recognizedText = visionText.text
//
//                                    // Extract entities from recognized text
//                                    coroutineScope.launch {
//                                        extractEntities(
//                                            entityExtractor,
//                                            visionText.text
//                                        ) { result ->
//                                            entities = result
//                                        }
//                                    }
//                                }
//                                .addOnCompleteListener {
//                                    imageProxy.close()
//                                }
//                        }
//                }
//
//        } catch (e: Exception) {
//            Log.e("TAG", "Object detection failed", e)
//            imageProxy.close()
//        } finally {
//            //imageProxy.close()
//        }
//
//        lastAnalyzedTimestamp = currentTimestamp
//    } else {
//        imageProxy.close()
//    }
//}

@Composable
fun GetLocationComp(
    captureRecordUiModel: CaptureRecordUiModel,
    alwaysGetEvenIfGottenBefore: Boolean = false
) {

    val userLoc by captureRecordUiModel.captureUiState.collectAsState()

    val hasLoc =
        userLoc.selectedLocation?.latitude != null && userLoc.selectedLocation?.longitude != null

    val shouldGetLocation = alwaysGetEvenIfGottenBefore || !hasLoc

    if (shouldGetLocation) {
        GetLocation {
            captureRecordUiModel.captureEventSink(
                CaptureRecordEventSink.OnSetLocation(it)
            )
        }
    }

}

@Composable
fun GetLocation(
    onLocation: (LatLng) -> Unit
) {
    val context = LocalActivity.current as LocationAwareActivity
    val currentLocation by context.appLocation.observeAsState(AppLocation())

    LaunchedEffect(
        key1 = currentLocation
    ) {
        context.getLocation()
    }


//    DisposableEffect(Unit) {
//        onDispose {
//            context.stopLocationUpdates()
//        }
//    }
    if (currentLocation?.toLatLng() != null) {
        logD("Location: ${currentLocation?.toLatLng()}")
        onLocation(currentLocation!!.toLatLng()!!)
    }
}
