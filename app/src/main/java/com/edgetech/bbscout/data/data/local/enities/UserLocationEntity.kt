package com.edgetech.bbscout.data.data.local.enities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edgetech.bbscout.data.data.local.utils.BaseEntity
import com.google.android.gms.maps.model.LatLng

@Entity
data class UserLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long = 0,
    var locationId: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var locationPrecision: Double? = null,
    var locationName: String? = null,
    var locationCountry: String? = null,
    var locationCity: String? = null,
    var mainAdminArea: String? = null,
    var subAdminArea: String? = null,
    var building: String? = null,
    val isLocationLocked: Boolean = false,
    override var createdAt: Long? = null,
    override var updatedAt: Long? = null
): BaseEntity()
