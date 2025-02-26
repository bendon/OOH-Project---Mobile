package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class ChangePasswordRequest(
    val newPassword: String? = null,
    val oldPassword: String? = null
) : ApiResponse