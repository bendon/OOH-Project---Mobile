package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class BBScoutUserStat(
    val billboardCount: Int? = null,
    val email: String? = null,
    val organizationId: String? = null,
    val organizationName: String? = null,
    val uploadMonth: Int? = null,
    val uploadYear: Int? = null,
    val userId: String? = null,
    val userName: String? = null
): ApiResponse

class BBScoutUserStatResponse : ArrayList<BBScoutUserStat>(), ApiResponse