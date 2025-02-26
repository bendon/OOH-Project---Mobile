package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class UserResponse(
    val active: Boolean? = null,
    val country: String? = null,
    val createdAt: Long? = null,
    val email: String? = null,
    val firstName: String? = null,
    val gender: Int? = null,
    val id: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val phone: Long? = null,
    val updatedAt: Long? = null,
    val verified: Boolean? = null
) : ApiResponse