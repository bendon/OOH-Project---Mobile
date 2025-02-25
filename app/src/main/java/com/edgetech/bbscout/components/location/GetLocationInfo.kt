package com.edgetech.bbscout.components.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

/**
 * Warning: Do not pass this to the viewmodel as
 * it hold a reference to the application context
 *
 */
interface GetLocationInfo {
    fun getLocationInfo(latLng: LatLng, onResult: (UserLocationEntity) -> Unit)



}