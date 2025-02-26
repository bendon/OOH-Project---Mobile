package com.edgetech.bbscout.data.data.local.enities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.edgetech.bbscout.data.data.local.utils.BaseEntity

@Entity(indices = [Index(value = ["remoteId"], unique = true)])
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id : Long = 0,
    val remoteId: String? = null,
    val userId: String? = null,
    val brand: String? = null,
    val mainFileUri: String? = null,
    val billboardFileUri: String? = null,
    val remoteFileId: String? = null,
    val remoteFileUrl: String? = null,
    val rawText: String? = null,
    val augmentedText: String? = null,
    val advertDescription: String? = null,
    override var createdAt: Long? = null,
    override var updatedAt: Long? = null
): BaseEntity()
