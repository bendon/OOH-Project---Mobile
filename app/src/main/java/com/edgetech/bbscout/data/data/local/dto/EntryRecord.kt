package com.edgetech.bbscout.data.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.OtherDataEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
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
                height = entryRecord.billboardData?.height,
                width = entryRecord.billboardData?.width,
                type = entryRecord.billboardData?.type,
                image_id = entryRecord.entryEntity.remoteFileId,
                location = entryRecord.location?.locationName,
                latitude = entryRecord.location?.latitude,
                longitude = entryRecord.location?.longitude
            ),
            imageId = entryRecord.entryEntity.remoteFileId,
            location = entryRecord.location?.locationName,
            campaignDescription = entryRecord.entryEntity.augmentedText,
            clientFirstName = entryRecord.entryEntity.brand,
        )

        fun fromBillboardResponse(billboardResponse: BillboardResponse) = EntryRecord(
            entryEntity = EntryEntity(
                brand = billboardResponse.campaign?.clientFirstName,
                remoteFileUrl = billboardResponse.image?.fileUrl,
                rawText = billboardResponse.campaign?.campaignDescription,
                augmentedText = billboardResponse.campaign?.campaignDescription,
                remoteId = billboardResponse.billboardId
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
                type = billboardResponse.type
            )
        )

        fun fromCampaignResponse(campaignResponse: CampaignResponse) = EntryRecord(
            entryEntity = EntryEntity(
                brand = campaignResponse.clientFirstName,
                remoteFileUrl = campaignResponse.image?.fileUrl,
                rawText = campaignResponse.campaignDescription,
                augmentedText = campaignResponse.campaignDescription,
                remoteId = campaignResponse.billboard?.billboardId
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
                type = campaignResponse.billboard?.type
            )
        )


    }

}
