package com.edgetech.bbscout.features.capture.presentation.review_data

import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo
import com.edgetech.bbscout.features.capture.domain.model.BillboardSides
import com.edgetech.bbscout.features.capture.domain.model.CaptureRecordUiState


enum class RecordType {
    CAMPAIGN,
    BILLBOARD_INFO,
    CONTACT,
    BILLBOARD_STRUCTURE,
    OTHERS_NEW,
    OTHERS_OLD,
}


enum class RecordTypeList {
    PHONE,
    EMAIL,
    WEBSITE,
    SOCIAL_MEDIA,
    PRODUCT,
    OWNNER_PHONE,
    OWNNER_EMAIL,

}


fun getActiveBillboardData(state: CaptureRecordUiState): BillboardExtractedInfo?{
    when(state.newCaptureSelectedSide){
        BillboardSides.SIDE_ONE -> {
            return state.sideOneExtractedInfo
        }
        BillboardSides.SIDE_TWO -> {
            return state.sideTwoExtractedInfo
        }
        BillboardSides.SIDE_THREE -> {
            return state.sideThreeExtractedInfo
        }
        BillboardSides.SIDE_FOUR -> {
            return state.sideFourExtractedInfo
        }
        BillboardSides.MAIN -> {
            return state.billboardData
        }
        else -> {
            return null
        }
    }
}