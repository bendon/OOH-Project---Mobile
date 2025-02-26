package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class BillboardResponse(
    val billboardActive: Boolean? = null,
    val billboardId: String? = null,
    val boardCode: String? = null,
    val campaignActive: Boolean? = null,
    val campaignId: String? = null,
    val createdAt: Long? = null,
    val createdById: String? = null,
    val height: Double? = null,
    val image: FileResponse? = null,
    val image_id: String? = null,
    val latitude: Double? = null,
    val location: String? = null,
    val longitude: Double? = null,
    val campaign: CampaignResponse? = null,
    val organizationId: String? = null,
    val price: Double? = null,
    val staff: UserResponse? = null,
    val type: String? = null,
    val unit: String? = null,
    val updatedAt: Long? = null,
    val width: Double? = null
) : ApiResponse