package com.edgetech.bbscout.features.capture.domain.model

import android.graphics.Bitmap
import com.google.mlkit.vision.objects.DetectedObject


data class BillboardExtractedInfo(
    val fullImage: Bitmap? = null,
    val billboardImage: Bitmap? = null,
    val detectedObjects: DetectedObjectWithLabels? = null,
    val qrCode: List<String>? = null,
    val rawText: String? = null,
    val imageLabels: List<ImageLabel>? = null,
    val entityInfos: List<EntityInfo>? = null
)



data class DetectedObjectWithLabels(
    val trackingId: Int?,
    val labels: List<ObjectLabel>
){
    companion object {
        fun fromDetectedObject(data: DetectedObject): DetectedObjectWithLabels {
            return DetectedObjectWithLabels(
                trackingId = data.trackingId,
                labels = data.labels.map {
                    ObjectLabel(
                        it.text,
                        it.confidence
                    )
                }
            )
        }
    }
}

data class ObjectLabel(
    val text: String,
    val confidence: Float
)


data class ImageLabel(
    val label: String,
    val confidence: String
)

data class EntityInfo(
    val type: String,
    val text: String
)