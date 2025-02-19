package com.edgetech.bbscout.data.data.local.enities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edgetech.bbscout.data.data.local.utils.BaseEntity

@Entity
data class BillboardDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long = 0,
    val type: String? = null,
    val typeSecondary: String? = null,
    val height: Double? = null,
    val width: Double? = null,
    val unitOfMeasurement: String? = null,
    val owner: String? = null,
    override var createdAt: Long? = null,
    override var updatedAt: Long? = null
): BaseEntity()
