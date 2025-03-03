package com.edgetech.bbscout.data.data.remote.bbscout_api.model

import com.edgetech.bbscout.data.data.local.utils.LongList
import com.edgetech.bbscout.data.data.local.utils.StringList
import com.google.gson.annotations.SerializedName

data class CampaignResponse(
    val active: Boolean? = null,
    val billboard: BillboardResponse? = null,
    val billboardId: String? = null,
    val campaignDescription: String? = null,
    val campaignInsights: String? = null,
    val clientFirstName: String? = null,
    val clientLastName: String? = null,
    val closedDate: Long? = null,
    val createdAt: Long? = null,
    val createdById: String? = null,
    val phone: LongList? = null,
    val email: StringList? = null,
    val campainSocials: StringList? = null,
    val siteUrl: StringList? = null,
    val products: StringList? = null,
    val targetGender: String? = null,
    val targetAge : String? = null,
    val endDate: Long? = null,
    val id: String? = null,
    val image: FileResponse? = null,
    @SerializedName("imageId", alternate = ["image_id"])
    val imageId: String? = null,
    val location: String? = null,
    val organizationId: String? = null,
    val startDate: Long? = null,
    val updatedAt: Long? = null
) : ApiResponse