package com.example.core.core.utils.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.edgetech.bbscout.components.location.AppLocation
import com.edgetech.bbscout.components.location.locationRequest
import com.edgetech.bbscout.components.utils.log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

abstract class LocationAwareActivity : ComponentActivity() {
    var fusedLocationProviderClient: FusedLocationProviderClient? = null


    val appLocation = MutableLiveData(AppLocation())
    var loc: AppLocation? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {


            p0.locations.forEachIndexed { index, location ->

                    if (loc == null) {

                        appLocation.postValue(AppLocation(location.accuracy.toInt(), location.latitude, location.longitude))
                        loc = AppLocation(
                            location.accuracy.toInt(),
                            location.latitude,
                            location.longitude
                        )

                    } else {
                        appLocation.postValue(AppLocation())
                        loc = null
                    }
                log("Location source: $loc")
            }

//            for (location in p0.locations){
//                appLocation.postValue(AppLocation())
//
//                appLocation.postValue(AppLocation(location.accuracy.toInt(), location.latitude, location.longitude))
//
//            }
        }
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }



    @SuppressLint("MissingPermission")
    fun getLocation(){
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

    }


    @Deprecated("use GetLocationInfo.getLocationInfo() instead")
    fun getAddress(loc: LatLng, onResult: ((Address?) -> Unit)? = null){
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                Geocoder(this, Locale.getDefault()).getFromLocation(
                    loc.latitude,
                    loc.longitude,
                    4
                ){ address ->
                    onResult?.invoke(address[0])
                }
            } else {


                /**
                 * The following us crashing on certain phones with low internet connection
                 *
                 */

                try {


                    val address = Geocoder(this, Locale.getDefault()).getFromLocation(
                        loc.latitude,
                        loc.longitude,
                        4
                    )
                    onResult?.invoke(address?.get(0))
                }catch (e: Exception){

                }


            }



        }catch (e: Exception){

        }
    }
    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

}



fun AppLocation.toLatLng(): LatLng? {
    if (this.latitude != null && this.longitude != null){
        return LatLng(this.latitude, this.longitude)
    }
    return  null
}