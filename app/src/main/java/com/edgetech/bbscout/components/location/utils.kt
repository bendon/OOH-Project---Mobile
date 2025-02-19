package com.edgetech.bbscout.components.location

import com.google.android.gms.location.LocationRequest

val locationRequest = LocationRequest.create().apply {
    interval = 8000
    fastestInterval = 5000
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
}

data class AppLocation(
    val accuracy: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)