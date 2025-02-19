//package com.edgetech.bbscout.features.capture.ui
//
//import android.os.Build
//import android.util.Log
//import android.util.Size
//import androidx.annotation.OptIn
//import androidx.annotation.RequiresApi
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ExperimentalGetImage
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.Divider
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import com.google.mlkit.common.model.DownloadConditions
//import com.google.mlkit.nl.entityextraction.DateTimeEntity
//import com.google.mlkit.nl.entityextraction.EntityExtraction
//import com.google.mlkit.nl.entityextraction.EntityExtractionParams
//import com.google.mlkit.nl.entityextraction.EntityExtractor
//import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
//import com.google.mlkit.nl.entityextraction.MoneyEntity
//import com.google.mlkit.vision.barcode.BarcodeScannerOptions
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.barcode.common.Barcode
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.label.ImageLabeling
//import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
//import com.google.mlkit.vision.objects.ObjectDetection
//import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
//import com.google.mlkit.vision.text.TextRecognition
//import com.google.mlkit.vision.text.latin.TextRecognizerOptions
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//import com.google.mlkit.nl.entityextraction.Entity
//
//@RequiresApi(Build.VERSION_CODES.P)
//@OptIn(ExperimentalGetImage::class)
//@Composable
//fun CameraMLApp() {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val previewView = remember { PreviewView(context) }
//
//    // Analysis results states
//    var detectedObjects by remember { mutableStateOf<List<DetectedObjectWithLabels>>(emptyList()) }
//    var imageLabels by remember { mutableStateOf<List<String>>(emptyList()) }
//    var barcodes by remember { mutableStateOf<List<String>>(emptyList()) }
//    var recognizedText by remember { mutableStateOf("") }
//    var entities by remember { mutableStateOf<List<EntityInfo>>(emptyList()) }
//
//    // Create ML Kit analyzers
//    val objectDetector = remember {
//        val options = ObjectDetectorOptions.Builder()
//            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
//            .enableMultipleObjects()
//            .enableClassification()
//            .build()
//        ObjectDetection.getClient(options)
//    }
//
//    val imageLabeler = remember {
//        ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
//    }
//
//    val barcodeScanner = remember {
//        val options = BarcodeScannerOptions.Builder()
//            .setBarcodeFormats(
//                Barcode.FORMAT_QR_CODE,
//                Barcode.FORMAT_EAN_13,
//                Barcode.FORMAT_EAN_8,
//                Barcode.FORMAT_UPC_A,
//                Barcode.FORMAT_UPC_E
//            )
//            .build()
//        BarcodeScanning.getClient(options)
//    }
//
//    val textRecognizer = remember {
//        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//    }
//
//    val entityExtractor = remember {
//        val options = EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
//            .build()
//        EntityExtraction.getClient(options)
//    }
//
//    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Setup camera
//    LaunchedEffect(previewView) {
//        val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
//            ProcessCameraProvider.getInstance(context).also { future ->
//                future.addListener({
//                    continuation.resume(future.get())
//                }, context.mainExecutor)
//            }
//        }
//
//        val preview = Preview.Builder().build().also {
//            it.setSurfaceProvider(previewView.surfaceProvider)
//        }
//
//        val imageAnalysis = ImageAnalysis.Builder()
//            .setTargetResolution(Size(640, 480))
//            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            .build()
//        val frameInterval = 5000L // 5 seconds in milliseconds
//        var lastAnalyzedTimestamp = 0L
//        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
//            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//            val image = imageProxy.image
//
//            val currentTimestamp = System.currentTimeMillis()
//
//            if (image != null && currentTimestamp - lastAnalyzedTimestamp >= frameInterval) {
//                val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
//
//                // Process with object detector
////                objectDetector.process(inputImage)
////                    .addOnSuccessListener { objects ->
////                        Log.d("ObjectDetection", "Objects detected: ${objects.size}")
////                        detectedObjects = objects.map { it.toDetectedObject() }
////                    }.addOnFailureListener{
////                        Log.e("ObjectDetection", "Error detecting objects", it)
////                    }
////                    .addOnCompleteListener {ob ->
//                       // log("the following object have been detected: ${ob}")
//                        // Process with image labeler
//                objectDetector.process(inputImage)
//                    .addOnSuccessListener { objects ->
//                        Log.d("ObjectDetection", "Objects detected: ${objects.size}")
//                        detectedObjects = objects.map { it.toDetectedObject() }
//                    }.addOnFailureListener{
//                        Log.e("ObjectDetection", "Error detecting objects", it)
//                    }
//                    .addOnCompleteListener {ob ->
//                        imageProxy.close()
//                    }
////                        imageLabeler.process(inputImage)
////                            .addOnSuccessListener { labels ->
////                                imageLabels = labels.map { "${it.text} (${it.confidence})" }
////                            }
////                            .addOnCompleteListener {
////                                // Process with barcode scanner
////                                barcodeScanner.process(inputImage)
////                                    .addOnSuccessListener { codes ->
////                                        barcodes = codes.mapNotNull { it.displayValue }
////                                    }
////                                    .addOnCompleteListener {
////                                        // Process with text recognizer
////                                        textRecognizer.process(inputImage)
////                                            .addOnSuccessListener { visionText ->
////                                                recognizedText = visionText.text
////
////                                                // Extract entities from recognized text
////                                                coroutineScope.launch {
////                                                    extractEntities(entityExtractor, visionText.text) { result ->
////                                                        entities = result
////                                                    }
////                                                }
////                                            }
////                                            .addOnCompleteListener {
////
////
////                                            }
////                                    }
////                            }
//           //         }
//                lastAnalyzedTimestamp = currentTimestamp
//            }
//            else {
//                imageProxy.close()
//            }
//        }
//
//        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//        try {
//            cameraProvider.unbindAll()
//            cameraProvider.bindToLifecycle(
//                lifecycleOwner,
//                cameraSelector,
//                preview,
//                imageAnalysis
//            )
//        } catch (exc: Exception) {
//            // Handle exceptions
//        }
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//           // cameraExecutor.
//        }
//    }
//
//    // UI
//    Surface(modifier = Modifier.fillMaxSize()) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            // Camera preview
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//            ) {
//                AndroidView(
//                    factory = { previewView },
//                    modifier = Modifier.fillMaxSize()
//                )
//            }
//
//            // Results
//            LazyColumn(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                item {
//                    Text(
//                        text = "Objects Detected (${detectedObjects.size})",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    for (obj in detectedObjects) {
//                        val categories = obj.labels.joinToString { "${it.text} (${it.confidence})" }
//                        Text("Object: $categories")
//                    }
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//                }
//
//                item {
//                    Text(
//                        text = "Image Labels (${imageLabels.size})",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    for (label in imageLabels) {
//                        Text(label)
//                    }
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//                }
//
//                item {
//                    Text(
//                        text = "Barcodes (${barcodes.size})",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    for (barcode in barcodes) {
//                        Text(barcode)
//                    }
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//                }
//
//                item {
//                    Text(
//                        text = "Text Recognition",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(recognizedText.ifEmpty { "No text recognized" })
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//                }
//
//                item {
//                    Text(
//                        text = "Extracted Entities (${entities.size})",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    for (entity in entities) {
//                        Text("${entity.type}: ${entity.text}")
//                    }
//                }
//            }
//        }
//    }
//}
//
//data class DetectedObjectWithLabels(
//    val boundingBox: android.graphics.Rect,
//    val trackingId: Int?,
//    val labels: List<ObjectLabel>
//)
//
//data class ObjectLabel(
//    val text: String,
//    val confidence: Float
//)
//
//data class EntityInfo(
//    val type: String,
//    val text: String
//)
//
//// Helper to convert ML Kit objects to our data model
//private fun com.google.mlkit.vision.objects.DetectedObject.toDetectedObject(): DetectedObjectWithLabels {
//    return DetectedObjectWithLabels(
//        boundingBox = boundingBox,
//        trackingId = trackingId,
//        labels = labels.map { ObjectLabel(it.text, it.confidence) }
//    )
//}
//
