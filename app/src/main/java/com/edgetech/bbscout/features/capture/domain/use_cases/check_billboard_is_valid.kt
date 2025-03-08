package com.edgetech.bbscout.features.capture.domain.use_cases

import com.edgetech.bbscout.components.utils.isDebug
import com.edgetech.bbscout.features.capture.domain.model.BillboardExtractedInfo

fun isBillboardValid(
    bill: BillboardExtractedInfo?,
): Boolean{
    if (isDebug){
        return bill?.closedUpUri != null && bill.isDistanceValid == true && bill.billboardLocation != null
    } else {
        return bill?.closedUpUri != null && bill.isDistanceValid == true && bill.billboardLocation != null && !bill.objectType.isNullOrEmpty() && !bill.billboardType.isNullOrEmpty()
    }
}