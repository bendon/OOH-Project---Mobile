package com.edgetech.bbscout.data.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.OtherDataEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity

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

    )
