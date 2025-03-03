package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class BillboardResponse(
    val id: String? = null,
    val accuracy: Double? = null,
    val billboardActive: Boolean? = null,
    val billboardId: String? = null,
    val boardCode: String? = null,
    val campaignActive: Boolean? = null,
    val campaignId: String? = null,
    val createdAt: Long? = null,
    val createdById: String? = null,
    val description: String? = null,
    val height: Double? = null,
    val image: FileResponse? = null,
    val imageId: String? = null,
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
    val width: Double? = null,
    val objectType: String? = null
) : ApiResponse