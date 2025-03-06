package com.edgetech.bbscout.data.data.local.enities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.edgetech.bbscout.data.data.local.utils.BaseEntity

@Entity(indices = [Index(value = ["remoteId"], unique = true)])
data class BillboardDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long = 0,
    val remoteId: String? = null,
    val type: String? = null,
    val typeSecondary: String? = null,
    val height: Double? = null,
    val width: Double? = null,
    val parentBoardCode: String? = null,
    val unitOfMeasurement: String? = null,
    val owner: String? = null,
    val objectType: String? = null,
    val imageId: String? = null,
    val closeUpImageId: String? = null,
    val remoteFileName: String? = null,
    val remoteCloseUpFileName: String? = null,
    val city: String? = null,
    val ownerContacts: String? = null,
    val ownerEmail: String? = null,
    val structure: String? = null,
    val material: String? = null,
    val angle: String? = null,
    val boardCode: String? = null,
    val visibility: String? = null,
    val illumination: String? = null,
    override var createdAt: Long? = null,
    override var updatedAt: Long? = null
): BaseEntity()
