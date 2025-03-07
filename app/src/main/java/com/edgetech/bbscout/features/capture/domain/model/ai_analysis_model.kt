package com.edgetech.bbscout.features.capture.domain.model

import android.graphics.Bitmap
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity
import com.edgetech.bbscout.data.data.local.utils.LongList
import com.edgetech.bbscout.data.data.local.utils.StringList


data class BillboardExtractedInfo(
    val billboardSideInfo: BillboardSides = BillboardSides.MAIN,
    val parentBillboard: String? = null,
    val billboardNumberOfSide: Int = 1,
    val fullImage: Bitmap? = null,
    val billboardImage: Bitmap? = null,
    val detectedObjects: List<DetectedObjectWithLabels>? = null,
    val qrCode: List<String>? = null,
    val rawText: String? = null,
    val brandName: String? = null,
    val brandCampaign: String? = null,
    val brandSlogan: String? = null,
    val fileUri: String? = null,
    val closedUpUri: String? = null,
    val remoteFileId: String? = null,
    val remoteCloseUpId: String? = null,
    val billboardType: String? = null,
    val billboardOwner: String? = null,
    val billboardWidth: String? = null,
    val billboardLength: String? = null,
    val objectType: String? = null,
    val unitOfMeasurement: String? = "meters",
    val phone: List<Long>? = null,
    val email: List<String>? = null,
    val campainSocials: List<String>? = null,
    val siteUrl: List<String>? = null,
    val products: List<String>? = null,
    val targetGender: String? = null,
    val targetAge: String? = null,
    val status: Boolean? = null,
    val billboardLocation: UserLocationEntity? = null,
    val distanceFromBillboard: Double? = null,
    val isDistanceValid: Boolean? = null,
    val ownerContacts: List<String>? = null,
    val ownerEmail: List<String>? = null,
    val structure: String? = null,
    val material: String? = null,
    val angle: String? = null,
    val visibility: String? = null,
    val illumination: String? = null,
)


data class DetectedObjectWithLabels(
    val trackingId: Int?,
    val labels: List<ObjectLabel>
) {
    companion object {
//        fun fromDetectedObject(data: DetectedObject): DetectedObjectWithLabels {
//            return DetectedObjectWithLabels(
//                trackingId = data.trackingId,
//                labels = data.labels.map {
//                    ObjectLabel(
//                        it.text,
//                        it.confidence
//                    )
//                }
//            )
//        }
    }
}

data class ObjectLabel(
    val text: String,
    val confidence: Float
)


data class ImageLabel(
    val label: String,
    val confidence: Float
)

data class EntityInfo(
    val type: String,
    val text: String
)

enum class BillboardSides {
    SIDE_ONE,
    SIDE_TWO,
    SIDE_THREE,
    SIDE_FOUR,
    MAIN
}