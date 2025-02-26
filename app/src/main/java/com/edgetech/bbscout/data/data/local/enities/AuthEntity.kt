package com.edgetech.bbscout.data.data.local.enities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AuthEntity(
    @PrimaryKey var authId: Long = 1,
    var refreshToken: String? = null,
    var accessToken: String? = null, //this will be from object with id 5
    var authObject: String? = null,
    var timeAdded: Long? = null
)