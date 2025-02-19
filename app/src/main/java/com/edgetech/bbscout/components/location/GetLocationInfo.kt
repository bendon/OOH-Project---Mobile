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
class GetLocationInfo {

    companion object {
        fun getLocationInfo(context: Context, latLng: LatLng, onResult: (UserLocationEntity) -> Unit) {
            if (latLng != null) {
                val location = UserLocationEntity(latitude = latLng.latitude, longitude = latLng.longitude)
                onResult(location)
                try{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Geocoder(context, Locale.getDefault()).getFromLocation(
                            latLng.latitude,
                            latLng.longitude,
                            4
                        ) { address ->
                            location.locationName =
                                address.get(0)?.featureName ?: address.get(0)?.getAddressLine(0)
                            location.locationCity = address.get(0)?.locality
                            location.locationCountry = address.get(0)?.countryName
                            location.mainAdminArea = address.get(0)?.adminArea
                            location.subAdminArea = address.get(0)?.subAdminArea
                            location.building = address.get(0)?.premises
                            onResult(location)

                        }
                    } else {


                        /**
                         * The following us crashing on certain phones with low internet connection
                         *
                         */

                        try {


                            val address = Geocoder(context, Locale.getDefault()).getFromLocation(
                                latLng.latitude,
                                latLng.longitude,
                                4
                            )

                            location.locationName =
                                address?.get(0)?.featureName ?: address?.get(0)?.getAddressLine(0)
                            location.locationCity = address?.get(0)?.locality
                            location.locationCountry = address?.get(0)?.countryName
                            location.mainAdminArea = address?.get(0)?.adminArea
                            location.subAdminArea = address?.get(0)?.subAdminArea
                            location.building = address?.get(0)?.premises
                            onResult(location)

                        }catch (_: Exception){

                        }


                    }
                } catch (_: Exception){

                }
            }
        }

    }


}