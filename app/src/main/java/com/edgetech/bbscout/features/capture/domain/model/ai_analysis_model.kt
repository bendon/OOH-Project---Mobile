package com.edgetech.bbscout.features.capture.domain.model

import android.graphics.Bitmap


data class BillboardExtractedInfo(
    val fullImage: Bitmap? = null,
    val billboardImage: Bitmap? = null,
    val detectedObjects: List<DetectedObjectWithLabels>? = null,
    val qrCode: List<String>? = null,
    val rawText: String? = null,
    val imageLabels: List<ImageLabel>? = null,
    val entityInfos: List<EntityInfo>? = null,
    val brandName: String? = null,
    val brandCampaign: String? = null,
    val brandSlogan: String? = null,
    val fileUri: String? = null,
    val billboardType: String? = null,
    val billboardOwner: String? = null,
    val billboardWidth: String? = null,
    val billboardLength: String? = null,

)



data class DetectedObjectWithLabels(
    val trackingId: Int?,
    val labels: List<ObjectLabel>
){
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