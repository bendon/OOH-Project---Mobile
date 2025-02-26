package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class AccountResponse(
    val active: Boolean? = null,
    val createdAt: Long? = null,
    val id: String? = null,
    val isLocked: Boolean? = null,
    val organization: OrganizationResponse? = null,
    val updatedAt: Long? = null,
    val userId: String? = null,
    val accountId : String? = null
) : ApiResponse

class AccountResponseList: ArrayList<AccountResponse>(), ApiResponse