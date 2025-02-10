package com.edgetech.bbscout.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.location.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.Executors
import com.edgetech.bbscout.screens.ReviewScreen

@Composable
fun CaptureScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Camera states
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    // Analysis states
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var detectedText by remember { mutableStateOf<String?>(null) }
    var billboardDetected by remember { mutableStateOf(false) }
    var extractedBrands by remember { mutableStateOf<List<String>>(emptyList()) }
    var extractedContacts by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLocationEnabled by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }

    // Review screen states
    var showReviewScreen by remember { mutableStateOf(false) }
    var capturedImageFile by remember { mutableStateOf<File?>(null) }

    // Permission states
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    // Permissions launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: hasLocationPermission
    }

    // Initialize location client
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // ML Kit detectors
    val textRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    val objectDetector = remember {
        ObjectDetection.getClient(ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .build())
    }

    // Request permissions on launch
    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
        isLocationEnabled = isLocationEnabled(context)
    }

    // Location updates
    var locationCallback: LocationCallback? by remember { mutableStateOf(null) }
    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val locationRequest = LocationRequest.Builder(10000L)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()

                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        currentLocation = result.lastLocation
                        isLocationEnabled = isLocationEnabled(context)
                    }
                }
                locationCallback = callback

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    context.mainLooper
                )
            } catch (e: SecurityException) {
                Log.e("Location", "Error getting location updates", e)
            }
        }

        onDispose {
            locationCallback?.let { callback ->
                fusedLocationClient.removeLocationUpdates(callback)
            }
            cameraExecutor.shutdown()
        }
    }

    // Function to handle capture
    fun captureImage() {
        if (!isLocationEnabled(context)) {
            showLocationDialog = true
            return
        }

        val photoFile = File(
            context.externalCacheDir,
            "BBScout_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImageFile = photoFile
                    showReviewScreen = true
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(context, "Failed to capture image: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (capturedImageFile != null && showReviewScreen) {
        ReviewScreen(
            imageFile = capturedImageFile!!,
            detectedText = detectedText,
            extractedBrands = extractedBrands,
            extractedContacts = extractedContacts,
            currentLocation = currentLocation,
            onSave = {
                Toast.makeText(context, "Data submitted to backend", Toast.LENGTH_SHORT).show()
                showReviewScreen = false
                capturedImageFile = null
            },
            onRetake = {
                showReviewScreen = false
                capturedImageFile = null
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { context ->
                        PreviewView(context).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }.also { previewView ->
                            val preview = Preview.Builder().build()

                            imageCapture = ImageCapture.Builder()
                                .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                                .build()

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .apply {
                                    setAnalyzer(cameraExecutor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )

                                            textRecognizer.process(image)
                                                .addOnSuccessListener { visionText ->
                                                    detectedText = visionText.text
                                                    extractedBrands = detectBrands(visionText.text)
                                                    extractedContacts = detectContacts(visionText.text)
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("TextRecognition", "Text recognition failed", e)
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }

                                            objectDetector.process(image)
                                                .addOnSuccessListener { objects ->
                                                    billboardDetected = objects.any { obj ->
                                                        obj.labels.any { label ->
                                                            label.text.lowercase().contains("billboard") ||
                                                                    label.text.lowercase().contains("sign")
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("ObjectDetection", "Object detection failed", e)
                                                }
                                        }
                                    }
                                }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture,
                                        imageAnalysis
                                    )
                                    preview.setSurfaceProvider(previewView.surfaceProvider)
                                } catch (e: Exception) {
                                    Log.e("CameraPreview", "Use case binding failed", e)
                                }
                            }, ContextCompat.getMainExecutor(context))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay information
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    currentLocation?.let { location ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Lat: ${String.format("%.6f", location.latitude)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Long: ${String.format("%.6f", location.longitude)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Accuracy: ${String.format("%.1f", location.accuracy)}m",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    if (billboardDetected) {
                        Text(
                            text = "Billboard Detected",
                            color = Color.Green,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Camera Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { flashEnabled = !flashEnabled }
                        ) {
                            Icon(
                                imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Flash"
                            )
                        }

                        FloatingActionButton(
                            onClick = { captureImage() },
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Take Photo",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch Camera"
                            )
                        }
                    }
                }
            } else {
                // Permission Request UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera and location permissions are required",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = {
                            permissionLauncher.launch(arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ))
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Grant Permissions")
                    }
                }
            }
        }

        if (!isLocationEnabled && showLocationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showLocationDialog = false
                },
                title = { Text("Location Required") },
                text = { Text("Please enable location services to use this feature") },
                confirmButton = {
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showLocationDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
private fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}