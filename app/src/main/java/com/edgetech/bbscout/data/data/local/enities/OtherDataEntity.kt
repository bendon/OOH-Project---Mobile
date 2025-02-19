package com.edgetech.bbscout.data.data.local.enities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edgetech.bbscout.data.data.local.utils.BaseEntity

@Entity
data class OtherDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val entryId: Long,
    val type: String? = null,
    val key: String? = null,
    val value: String? = null,
    override var createdAt: Long? = null,
    override var updatedAt: Long? = null
): BaseEntity()
