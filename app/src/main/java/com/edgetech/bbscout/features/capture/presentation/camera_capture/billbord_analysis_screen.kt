package com.edgetech.bbscout.features.capture.presentation.camera_capture

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.components.utils.log
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiEvent
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiModel
import com.edgetech.bbscout.features.capture.domain.model.DetectedObjectWithLabels
import com.edgetech.bbscout.features.capture.domain.model.EntityInfo
import com.edgetech.bbscout.features.capture.domain.model.ImageLabel
import com.edgetech.bbscout.features.capture.domain.viewmodel.CaptureRecordViewmodel
import com.edgetech.bbscout.features.capture.presentation.GetLocationComp
import com.edgetech.bbscout.features.capture.presentation.setupZoomListener
import com.edgetech.bbscout.features.capture.presentation.takePhoto
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.example.core.core.utils.components.LocationAwareActivity
import com.example.core.core.utils.components.toLatLng
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

    GetLocationComp(captureRecordUiModel, true)

    BackHandler {
        appState?.navController?.popBackStack(AppDestinations.Dashboard, false)
    }


    val previewView = remember { PreviewView(context) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val imageCapture = remember { ImageCapture.Builder().build() }
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

    var cameraSelector by remember {
        mutableStateOf<CameraSelector>(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    val uiEvent by captureRecordUiModel.captureUiEvent.collectAsState()

    if (uiEvent is CaptureRecordUiEvent.CaptureAdded){
        appState?.navController?.navigate(AppDestinations.ReviewBillboardData)
        captureRecordUiModel.captureEventSink(
            CaptureRecordEventSink.ResetState
        )
    }

    // Setup camera
    LaunchedEffect(previewView, cameraSelector) {
        val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener({
                    continuation.resume(future.get())
                }, cameraExecutor)
            }
        }

        val preview = Preview.Builder().build().also {p ->
            withContext(Dispatchers.Main) {
                p.setSurfaceProvider(previewView.surfaceProvider)
            }
        }



        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
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



            }
            FloatingActionButton(
                onClick = {
                    appState?.navController?.navigateUp()

                },
                modifier = Modifier.padding(bottom = 16.dp, start = 24.dp).align(Alignment.BottomStart),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Capture"
                )
            }
            FloatingActionButton(
                onClick = {
                    takePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        executor = cameraExecutor,
                        onImageCaptured = { uri ->
                            captureRecordUiModel.captureEventSink(
                                CaptureRecordEventSink.OnCaptureEvent(
                                    fileUri = uri.path ?: ""
                                )
                            )
                        },
                        onError = { error ->

                        }
                    )

                },
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.BottomCenter),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Capture"
                )
            }
            FloatingActionButton(
                onClick = {
                   if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA){
                       cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                   } else {
                       cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                   }

                },
                modifier = Modifier.padding(bottom = 16.dp, end = 24.dp).align(Alignment.BottomEnd),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    Icons.Default.Sync,
                    contentDescription = "Capture"
                )
            }
//            if (croppedBitmap != null)
//                Surface(
//                    shape = MaterialTheme.shapes.small,
//                    modifier = Modifier
//                        .height(250.dp).width(150.dp)
//                        .align(Alignment.BottomStart)
//                        .padding(bottom = 140.dp, start = 16.dp),
//                ) {
//                    Image(
//                        bitmap = croppedBitmap!!.asImageBitmap(),
//                        contentDescription = "Cropped Billboard",
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.FillBounds
//                    )
//                }
        }
    }
}