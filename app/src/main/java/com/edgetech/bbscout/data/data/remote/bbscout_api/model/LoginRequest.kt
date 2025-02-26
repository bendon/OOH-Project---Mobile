package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class LoginRequest(
    val email: String? = null,
    val password: String? = null,
    val token: String? = null
): ApiResponse