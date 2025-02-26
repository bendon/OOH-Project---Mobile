package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class AuthResponse(
    val accessToken: String? = null,
    val account: AccountResponse? = null,
    val permissions: List<String>? = null,
    val refreshToken: String? = null,
    val user: UserResponse? = null,
    val message: String? = null
) : ApiResponse