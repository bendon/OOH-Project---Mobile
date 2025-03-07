package com.edgetech.bbscout.features.capture.presentation.capture_flow.ui_components

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.edgetech.bbscout.components.utils.log
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordEventSink
import com.edgetech.bbscout.features.capture.presentation.setupZoomListener
import com.edgetech.bbscout.features.capture.presentation.takePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun CameraComp(
    onCaptureTaken: (Uri) -> Unit,
    modifier: Modifier = Modifier
){

    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var cameraSelector by remember {
        mutableStateOf<CameraSelector>(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    LaunchedEffect(previewView, cameraSelector) {
        val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener({
                    continuation.resume(future.get())
                }, cameraExecutor)
            }
        }

        val preview = Preview.Builder().build().also { p ->
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

    InfoComposableContainer(InfoComposableType.ALL_GRAY, modifier = modifier) {
        Box {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
            )
            FloatingActionButton(
                onClick = {
                    takePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        executor = cameraExecutor,
                        onImageCaptured = { uri ->
                           onCaptureTaken(uri)
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
        }
    }


}
