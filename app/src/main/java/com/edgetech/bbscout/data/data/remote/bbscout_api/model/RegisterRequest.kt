package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class RegisterRequest(
    val email: String? = null,
    val firstName: String? = null,
    val gender: Int? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val password: String? = null,
    val phone: Long? = null
): ApiResponse