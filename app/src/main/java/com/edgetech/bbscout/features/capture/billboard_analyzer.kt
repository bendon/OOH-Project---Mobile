package com.edgetech.bbscout.features.capture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min



@Composable
fun BillboardDetectorScreen() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var lastCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isImageClear by remember { mutableStateOf(true) }
    var textDetected by remember { mutableStateOf(false) }
    var detectionMessage by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf(1f) }
    var imageAnalyzer: ImageAnalysis? by remember { mutableStateOf(null) }
    var previewUseCase: Preview? by remember { mutableStateOf(null) }

    // Initialize ML Kit Object Detector
    val objectDetector = remember {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()
        ObjectDetection.getClient(options)
    }

    // Initialize ML Kit Text Recognizer
    val textRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (croppedBitmap == null) {
            // Camera preview
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        // Preview Use Case
                        previewUseCase = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        // Image Analysis Use Case
                        imageAnalyzer = ImageAnalysis.Builder()

                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            // ML Kit analysis
                                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                            val bitmap =  imageProxy.toBitmap()

                                            val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)

                                            // Process the image for object detection
                                            try {
                                                val detectedObjects = Tasks.await(objectDetector.process(inputImage))

                                               // Log.e("objectDetector", "objectDetector: ${detectedObjects.size}")

                                                // Process detected objects (billboards)
                                                val (detectedBitmap, isCleared,  message) = detectBillboardWithText(
                                                    detectedObjects,
                                                    bitmap,
                                                    rotationDegrees,
                                                    textRecognizer
                                                )

                                                detectedBitmap?.let {
                                                    lastCapturedBitmap = it
                                                    isImageClear = isCleared
                                                    textDetected = true
                                                    detectionMessage = message
                                                }
                                            } catch (e: Exception) {
                                                Log.e("TAG", "Object detection failed", e)
                                            } finally {
                                                imageProxy.close()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("TAG", "Error in image analysis", e)
                                            imageProxy.close()
                                        }
                                    }
                                }
                            }

                        // Select back camera
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            // Unbind any bound use cases before rebinding
                            cameraProvider.unbindAll()

                            // Bind use cases to camera
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                previewUseCase,
                                imageAnalyzer
                            )
                            setupZoomListener(ctx, camera, previewView)
                        } catch (e: Exception) {
                            // Log.e(TAG, "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Overlay to show detection status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = {
                        lastCapturedBitmap?.let { bitmap ->
                            croppedBitmap = bitmap
                        }
                    },
                    enabled = lastCapturedBitmap != null && textDetected
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Capture Billboard")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (detectionMessage.isNotEmpty()) {
                    Text(
                        detectionMessage,
                        color = if (!isImageClear || !textDetected) Color.Red else Color.Green,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(8.dp)
                    )
                } else {
                    Text(
                        "Make sure the full billboard is in the frame",
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(8.dp)
                    )
                }
            }
        } else {
            // Display the captured and cropped billboard with zoom capability
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                    }
                }
            ) {
                croppedBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Cropped Billboard",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                ) {
                    Row(

                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            croppedBitmap = null
                            scale = 1f
                        }) {
                            Text("Back to Camera")
                        }

                        Button(onClick = {
                            coroutineScope.launch {
                                croppedBitmap?.let { bitmap ->
                                    // Call your save function here
                                    Toast.makeText(
                                        context,
                                        "Billboard image saved",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }) {
                            Text("Extract data")
                        }
                    }
                    Text(text = "Not the right frame? Make sure the full image is in the frame")
                }

                Text(
                    "Scale: ${String.format("%.1f", scale)}x",
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    color = Color.White
                )
            }
        }
    }
}

// Billboard detection with text recognition
suspend fun detectBillboardWithText(
    detectedObjects: List<DetectedObject>,
    bitmap: Bitmap,
    rotation: Int,
    textRecognizer: com.google.mlkit.vision.text.TextRecognizer
): BillboardDetectionResult = withContext(Dispatchers.Default) {
    // Filter for objects that are likely billboards (rectangular objects with certain constraints)
    val potentialBillboards = detectedObjects.filter { obj ->
        val boundingBox = obj.boundingBox
        val aspectRatio = boundingBox.width().toFloat() / boundingBox.height().toFloat()

        // Billboards typically have aspect ratios between 0.5:1 and 4:1
        aspectRatio in 0.5f..4f &&
                boundingBox.width() > bitmap.width * 0.15 && // At least 15% of image width
                boundingBox.height() > bitmap.height * 0.15   // At least 15% of image height
    }

    if (potentialBillboards.isNotEmpty()) {
        // Take the largest potential billboard
        val billboard = potentialBillboards.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }

        billboard?.let {
            val boundingBox = it.boundingBox
            val rotatedBitmap = rotateBitmap(bitmap, rotation.toFloat())

            // Ensure bounding box is within bitmap bounds
            val left = max(0, boundingBox.left)
            val top = max(0, boundingBox.top)
            val right = min(rotatedBitmap.width, boundingBox.right)
            val bottom = min(rotatedBitmap.height, boundingBox.bottom)

            if (right > left && bottom > top) {
                try {
                    val croppedBitmap = Bitmap.createBitmap(
                        rotatedBitmap,
                        left,
                        top,
                        right - left,
                        bottom - top
                    )

                    // Check if the image is clear (brightness/contrast check)
                    val isImageClear = isImageClear(croppedBitmap)

                    // Perform text recognition on the cropped bitmap
                    val textResult = withContext(Dispatchers.IO) {
                        Tasks.await( textRecognizer.process(InputImage.fromBitmap(croppedBitmap, 0)))
                    }

                    // Calculate text density
                    val textDensity = calculateTextDensity(textResult, croppedBitmap.width, croppedBitmap.height)
                    val hasEnoughText = textDensity >= 0.00 // At least 3% of the billboard area should contain text

                    // Determine detection message
                    val message = when {
                        !isImageClear -> "Image is not clear enough - adjust lighting or distance"
                        !hasEnoughText -> "This doesn't appear to be a billboard - not enough information to confirm"
                        else -> "Press capture to to review image"
                    }

                    return@withContext BillboardDetectionResult(croppedBitmap, isImageClear,  message)
                } catch (e: Exception) {
                    Log.e("BillboardDetector", "Error processing billboard", e)
                }
            }
        }
    }

    return@withContext BillboardDetectionResult(null, false,  "Point camera at a billboard")
}

// Helper function to calculate text density in the image
private fun calculateTextDensity(textResult: Text, width: Int, height: Int): Float {
    var textArea = 0
    val totalArea = width * height

    for (textBlock in textResult.textBlocks) {
        for (line in textBlock.lines) {
            for (element in line.elements) {
                val boundingBox = element.boundingBox ?: continue
                textArea += boundingBox.width() * boundingBox.height()
            }
        }
    }

    return textArea.toFloat() / totalArea
}

// Helper function to check image clarity
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

@SuppressLint("ClickableViewAccessibility")
private fun setupZoomListener(context: Context, camera: Camera, previewView: PreviewView) {
    val scaleGestureDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        })

    previewView.setOnTouchListener { _, event ->
        scaleGestureDetector.onTouchEvent(event)
        return@setOnTouchListener true
    }
}


// Data class to represent billboard detection result
data class BillboardDetectionResult(
    val bitmap: Bitmap?,
    val isImageClear: Boolean,
    val message: String
)