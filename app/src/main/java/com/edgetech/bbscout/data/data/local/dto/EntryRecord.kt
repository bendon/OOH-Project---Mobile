package com.edgetech.bbscout.data.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import com.edgetech.bbscout.components.utils.fromJson
import com.edgetech.bbscout.components.utils.toJson
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.OtherDataEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.edgetech.bbscout.data.data.local.utils.LongList
import com.edgetech.bbscout.data.data.local.utils.StringList
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BillboardResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.CampaignResponse

data class EntryRecord(
    @Embedded val entryEntity: EntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val otherData: List<OtherDataEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val location: UserLocationEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val billboardData: BillboardDataEntity?,

    ) {

    companion object {

        fun toCampaignResponse(entryRecord: EntryRecord) = CampaignResponse(
                billboard = BillboardResponse(
                    height = entryRecord.billboardData?.height ?: 0.0,
                    width = entryRecord.billboardData?.width ?: 0.0,
                    type = entryRecord.billboardData?.type ?: "traditional",
                    unit = entryRecord.billboardData?.unitOfMeasurement ?: "centimeters",
                    price = 0.0,
                    imageId = entryRecord.entryEntity.remoteFileId,
                    location = entryRecord.location?.locationName,
                    latitude = entryRecord.location?.latitude,
                    longitude = entryRecord.location?.longitude,
                    description = "",
                    accuracy = 1.0,
                    objectType = entryRecord.billboardData?.objectType
                ),
            imageId = entryRecord.entryEntity.remoteFileId,
            location = entryRecord.location?.locationName,
            campaignDescription = entryRecord.entryEntity.augmentedText ?: "a billboard",
            clientFirstName = entryRecord.entryEntity.brand,
            phone = entryRecord.entryEntity.phone?.fromJson(LongList::class.java),
            email = entryRecord.entryEntity.email?.fromJson(StringList::class.java),
            campainSocials = entryRecord.entryEntity.campainSocials?.fromJson(StringList::class.java),
            siteUrl = entryRecord.entryEntity.siteUrl?.fromJson(StringList::class.java),
            products = entryRecord.entryEntity.products?.fromJson(StringList::class.java),
            targetGender = entryRecord.entryEntity.targetGender,
            targetAge = entryRecord.entryEntity.targetAge,
        )

        fun fromBillboardResponse(billboardResponse: BillboardResponse) = EntryRecord(
            entryEntity = EntryEntity(
                brand = billboardResponse.campaign?.clientFirstName,
                remoteFileUrl = billboardResponse.image?.fileName,
                rawText = billboardResponse.campaign?.campaignDescription,
                augmentedText = billboardResponse.campaign?.campaignDescription,
                remoteId = billboardResponse.billboardId,
                updatedAt = billboardResponse.updatedAt,
                createdAt = billboardResponse.createdAt,
                phone = billboardResponse.campaign?.phone?.toJson(),
                email = billboardResponse.campaign?.email?.toJson(),
                campainSocials = billboardResponse.campaign?.campainSocials?.toJson(),
                siteUrl = billboardResponse.campaign?.siteUrl?.toJson(),
                products = billboardResponse.campaign?.products?.toJson(),
                targetGender = billboardResponse.campaign?.targetGender,
                targetAge = billboardResponse.campaign?.targetAge,
            ),
            otherData = emptyList(),
            location = UserLocationEntity(
                locationName = billboardResponse.location,
                latitude = billboardResponse.latitude,
                longitude = billboardResponse.longitude
            ),
            billboardData = BillboardDataEntity(
                height = billboardResponse.height,
                width = billboardResponse.width,
                type = billboardResponse.type,
                unitOfMeasurement = billboardResponse.unit,
                objectType = billboardResponse.objectType
            )
        )

        fun fromCampaignResponse(campaignResponse: CampaignResponse) = EntryRecord(
            entryEntity = EntryEntity(
                brand = campaignResponse.clientFirstName,
                remoteFileUrl = campaignResponse.image?.fileName,
                rawText = campaignResponse.campaignDescription,
                augmentedText = campaignResponse.campaignDescription,
                remoteId = campaignResponse.billboard?.billboardId,
                phone = campaignResponse.phone?.toJson(),
                email = campaignResponse.email?.toJson(),
                campainSocials = campaignResponse.campainSocials?.toJson(),
                siteUrl = campaignResponse.siteUrl?.toJson(),
                products = campaignResponse.products?.toJson(),
                targetGender = campaignResponse.targetGender,
                targetAge = campaignResponse.targetAge,
                updatedAt = campaignResponse.billboard?.updatedAt,
                createdAt = campaignResponse.billboard?.createdAt
            ),
            otherData = emptyList(),
            location = UserLocationEntity(
                locationName = campaignResponse.billboard?.location,
                latitude = campaignResponse.billboard?.latitude,
                longitude = campaignResponse.billboard?.longitude
            ),
            billboardData = BillboardDataEntity(
                height = campaignResponse.billboard?.height,
                width = campaignResponse.billboard?.width,
                type = campaignResponse.billboard?.type,
                unitOfMeasurement = campaignResponse.billboard?.unit,
                objectType = campaignResponse.billboard?.objectType
            )
        )


    }

}
