package com.edgetech.bbscout.features.capture_start

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.edgetech.bbscout.components.utils.getApplicationPackagedName

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