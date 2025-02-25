package com.edgetech.bbscout.features.capture.presentation.camera_capture

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.utils.log
import com.edgetech.bbscout.features.capture.detectBillboardWithText
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.DetectedObjectWithLabels
import com.edgetech.bbscout.features.capture.domain.model.EntityInfo
import com.edgetech.bbscout.features.capture.domain.model.ImageLabel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.extractEntities
import com.edgetech.bbscout.features.capture.presentation.setupZoomListener
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CaptureBillboardScreen(
    appState: BBScoutAppState?,
    captureRecordViewmodel: CaptureRecordViewmodel
){
    CaptureBillboardMain(
        appState,
        captureRecordViewmodel.uiModel
    )
}



@OptIn(ExperimentalGetImage::class)
@Composable
fun CaptureBillboardMain(
    appState: BBScoutAppState?,
    captureRecordUiModel: CaptureRecordUiModel

) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Analysis results states
    var detectedObjects by remember { mutableStateOf<List<DetectedObjectWithLabels>>(emptyList()) }
    var imageLabels by remember { mutableStateOf<List<ImageLabel>>(emptyList()) }
    var barcodes by remember { mutableStateOf<List<String>>(emptyList()) }
    var recognizedText by remember { mutableStateOf("") }
    var entities by remember { mutableStateOf<List<EntityInfo>>(emptyList()) }
    var lastCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Create ML Kit analyzers
    val objectDetector = remember {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        ObjectDetection.getClient(options)
    }



    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    val textRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    val entityExtractor = remember {
        val options = EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
            .build()
        EntityExtraction.getClient(options)
    }


    // Setup camera
    LaunchedEffect(previewView) {
        val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener({
                    continuation.resume(future.get())
                }, cameraExecutor)
            }
        }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageAnalysis = ImageAnalysis.Builder()
            //.setTargetResolution(Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        val frameInterval = 2000L // 5 seconds in milliseconds
        var lastAnalyzedTimestamp = 0L

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = imageProxy.image

            val currentTimestamp = System.currentTimeMillis()

            if (image != null && currentTimestamp - lastAnalyzedTimestamp >= frameInterval) {
                val inputImage = InputImage.fromMediaImage(image, rotationDegrees)

                try {
                    objectDetector.process(inputImage)
                        .addOnSuccessListener { objects ->
                            Log.d("ObjectDetection", "Objects detected: ${objects.size}")
                            detectedObjects =
                                objects.map { DetectedObjectWithLabels.fromDetectedObject(it) }
                            lastCapturedBitmap = imageProxy.toBitmap()
                            coroutineScope.launch(Dispatchers.IO) {
                                val (detectedBitmap, isCleared, message) = detectBillboardWithText(
                                    objects,
                                    lastCapturedBitmap!!,
                                    rotationDegrees,
                                    textRecognizer
                                )
                                withContext(Dispatchers.Main) {
                                    detectedBitmap?.let {
                                        croppedBitmap = it
                                    }
                                }
                            }
                        }.addOnFailureListener {
                             Log.e("ObjectDetection", "Error detecting objects", it)
                        }
                        .addOnCompleteListener { ob ->
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { codes ->
                                    barcodes = codes.mapNotNull { it.displayValue }
                                }
                                .addOnCompleteListener {
                                    // Process with text recognizer
                                    textRecognizer.process(inputImage)
                                        .addOnSuccessListener { visionText ->
                                            recognizedText = visionText.text

                                            // Extract entities from recognized text
                                            coroutineScope.launch {
                                                extractEntities(
                                                    entityExtractor,
                                                    visionText.text
                                                ) { result ->
                                                    entities = result
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                }
                        }

                } catch (e: Exception) {
                    Log.e("TAG", "Object detection failed", e)
                    imageProxy.close()
                } finally {
                    //imageProxy.close()
                }

                lastAnalyzedTimestamp = currentTimestamp
            } else {
                imageProxy.close()
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            setupZoomListener(context, camera, previewView)
        } catch (exc: Exception) {
            log("Error binding camera: $exc")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    //Todo: handle when the user rotate the phone
    Scaffold(

    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                FloatingActionButton(
                    onClick = {
                        if (croppedBitmap != null) {
                            captureRecordUiModel.captureEventSink(
                                CaptureRecordEventSink.OnCaptureEvent(
                                    BillboardExtractedInfo(
                                        fullImage = lastCapturedBitmap,
                                        billboardImage = croppedBitmap,
                                        detectedObjects = detectedObjects,
                                        imageLabels = imageLabels,
                                        qrCode = barcodes,
                                        rawText = recognizedText,
                                        entityInfos = entities
                                    )
                                )
                            )
                            appState?.navController?.navigate(AppDestinations.EditCapture(null))
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Capture"
                    )
                }
                Text(
                    if (croppedBitmap != null) "Ready" else "Analysing...",
                    modifier = Modifier.padding(16.dp)
                )

            }
            if (croppedBitmap != null)
                Surface(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .height(250.dp).width(150.dp)
                        .align(Alignment.BottomStart)
                        .padding(bottom = 140.dp, start = 16.dp),
                ) {
                    Image(
                        bitmap = croppedBitmap!!.asImageBitmap(),
                        contentDescription = "Cropped Billboard",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
        }
    }
}