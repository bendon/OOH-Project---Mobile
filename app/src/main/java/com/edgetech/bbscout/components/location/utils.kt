package com.edgetech.bbscout.components.location

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

//val locationRequest = LocationRequest.create().apply {
//    interval = 4000
//    fastestInterval = 3000
//    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//}

val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
    .build()

data class AppLocation(
    val accuracy: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)