package com.edgetech.bbscout.features.capture_start

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.diracks.app.app.app_state.BBScoutAppState
import com.droidbird.gharama.feature.naviagation.AppScreenData
import com.edgetech.bbscout.components.utils.getApplicationPackagedName
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.edgetech.bbscout.ui.theme.mainBlue
import kotlinx.coroutines.launch


@Composable
fun CaptureCheckPermission(
    appState: BBScoutAppState?
){

    var hasLocationPermission by rememberSaveable { mutableStateOf(false) }
    var hasCameraPermission by rememberSaveable { mutableStateOf(false) }

    var hasRequestedLocationPermission by rememberSaveable { mutableStateOf(false) }
    var hasRequestedCameraPermission by rememberSaveable { mutableStateOf(false) }

    var hasGPSEnabled by rememberSaveable { mutableStateOf(false) }
    var hasRequestedGPS by rememberSaveable { mutableStateOf(false) }

    var actionType by rememberSaveable { mutableStateOf<CheckPermissionType>(CheckPermissionType.INIT) }

    val context = LocalContext.current

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Check GPS status after returning from settings
        hasGPSEnabled = isGPSEnabled(context)
        hasRequestedGPS = true
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
            hasLocationPermission = isGranted
            hasRequestedLocationPermission = true
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        hasRequestedCameraPermission = true
    }

    hasLocationPermission = checkLocationPermission(context)
    hasCameraPermission = checkCameraPermission(context)
    hasGPSEnabled = isGPSEnabled(context)

    hasRequestedCameraPermission = hasRequestedPermissionBefore(context, Manifest.permission.CAMERA)
    hasRequestedLocationPermission = hasRequestedPermissionBefore(context, Manifest.permission.ACCESS_FINE_LOCATION)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (!hasLocationPermission && !hasRequestedLocationPermission) {
        actionType = CheckPermissionType.LOCATION_INIT

    } else if (!hasCameraPermission && !hasRequestedCameraPermission){
        actionType = CheckPermissionType.CAMERA_INIT

    } else if (!hasLocationPermission && hasRequestedLocationPermission){
        actionType = CheckPermissionType.LOCATION_SETTINGS
    } else if (!hasCameraPermission && hasRequestedCameraPermission){
        actionType = CheckPermissionType.CAMERA_SETTINGS
    } else if (hasLocationPermission && !hasGPSEnabled){
        actionType = CheckPermissionType.GPS
    } else if (hasLocationPermission && hasCameraPermission && hasGPSEnabled){
        actionType = CheckPermissionType.ALL_GRANTED
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Checking for requirements",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Icon(
                actionType.data.pIcon ?: Icons.Outlined.Smartphone,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
            )


            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = actionType.data.title ?: "",
                fontSize = 16.sp,
                //color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            if (actionType == CheckPermissionType.LOCATION_SETTINGS || actionType == CheckPermissionType.CAMERA_SETTINGS) {
                TextButton(onClick = {
                    if (actionType == CheckPermissionType.LOCATION_SETTINGS) {
                        hasLocationPermission = checkLocationPermission(context)
                        if (!hasLocationPermission){
                           
                                scope.launch {
                                    val result = snackbarHostState
                                        .showSnackbar(
                                            message = "Permissions not granted",
                                            duration = SnackbarDuration.Short
                                        )
                                    when (result) {
                                        SnackbarResult.Dismissed -> {

                                        }

                                        else -> {}
                                    }
                                }
                            
                        }
                    }
                    else {
                        hasCameraPermission = checkCameraPermission(context)
                        if (!hasCameraPermission){
                                scope.launch {
                                    val result = snackbarHostState
                                        .showSnackbar(
                                            message = "Permissions not granted",
                                            duration = SnackbarDuration.Short
                                        )
                                    when (result) {
                                        SnackbarResult.Dismissed -> {

                                        }

                                        else -> {}
                                    }
                                }
                            }

                        
                    }
                     }) {
                    Text(text = "Check permission")
                    
                }
                Spacer(modifier = Modifier.height(24.dp))

            }


            if (!actionType.data.name.isNullOrEmpty()) {
                Button(
                    // colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = mainBlue),
                    onClick = {
                        when (actionType) {
                            CheckPermissionType.INIT -> {

                            }

                            CheckPermissionType.LOCATION_INIT -> {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }

                            CheckPermissionType.CAMERA_INIT -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }

                            CheckPermissionType.LOCATION_SETTINGS -> {
                                goSettings(context)
                            }

                            CheckPermissionType.CAMERA_SETTINGS -> {
                                goSettings(context)
                            }

                            CheckPermissionType.GPS -> {
                                locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }

                            CheckPermissionType.ALL_GRANTED -> {

                            }
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(actionType.data.name!!)
                }
            }

        }
    }






}

enum class CheckPermissionType(val data: AppScreenData) {
    INIT(
        data = AppScreenData(
            title = "Checking for permissions...",
            pIcon = Icons.Outlined.Smartphone,
        )
    ),
    LOCATION_INIT(
        data = AppScreenData(
            title = "This app need access to your location to work properly. Your location will only be recorded when adding a new entry.",
            pIcon = Icons.Outlined.LocationOn,
            name = "Request location permission"
        )
    ),
    CAMERA_INIT(
        data = AppScreenData(
            title = "This app need access to your camera to work properly. Your camera will only be recorded when adding a new entry.",
            pIcon = Icons.Outlined.CameraAlt,
            name = "Request camera permission"
        )
    ),
    LOCATION_SETTINGS(
        data = AppScreenData(
            title = "This app need access to your location to work properly. Your location will only be recorded when adding a new entry.",
            pIcon = Icons.Outlined.LocationOn,
            name = "Open location settings"
        )
    ),
    CAMERA_SETTINGS(
        data = AppScreenData(
            title = "This app need access to your camera to work properly. Your camera will only be recorded when adding a new entry.",
            pIcon = Icons.Outlined.CameraAlt,
            name = "Open camera settings"
        )
    ),
    GPS(
        data = AppScreenData(
            title = "This app need access to your location to work properly. Your location will only be recorded when adding a new entry. Please enable your GPS",
            pIcon = Icons.Outlined.GpsFixed,
            name = "Enable GPS"
        )
    ),
    ALL_GRANTED(
        data = AppScreenData(
            title = "All permissions granted",
            pIcon = Icons.Outlined.CheckCircleOutline,
            name = "Continue"
        )
    )
}

fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun checkCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

fun hasRequestedPermissionBefore(context: Context, permission: String): Boolean {
    return when (context) {
        is ComponentActivity -> {
            ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
                .not() && isPermissionDenied(context, permission)
        }
        else -> false
    }
}

private fun isPermissionDenied(context: Context, permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_DENIED
}

fun isGPSEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun goSettings(context: Context){
    try {
        // Try vendor-specific intent first (works on most modern devices)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", getApplicationPackagedName(), null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // This extra attempts to open the permissions page directly
            putExtra(":settings:fragment_args_key", "permission_settings")
        }
        startActivity(context, intent, null)
    } catch (e: Exception) {
        // Fallback to regular app settings if vendor-specific intent fails
        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", getApplicationPackagedName(), null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(context, fallbackIntent, null)
    }
}

