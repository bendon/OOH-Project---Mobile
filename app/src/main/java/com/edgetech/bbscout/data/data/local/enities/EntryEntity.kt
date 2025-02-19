package com.edgetech.bbscout.data.data.local.enities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edgetech.bbscout.data.data.local.utils.BaseEntity

@Entity
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id : Long = 0,
    val userId: String? = null,
    val brand: String? = null,
    val mainFileUri: String? = null,
    val billboardFileUri: String? = null,
    val rawText: String? = null,
    val augmentedText: String? = null,
    val advertDescription: String? = null,
    override var createdAt: Long? = null,
    override var updatedAt: Long? = null
): BaseEntity()
