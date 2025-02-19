package com.edgetech.bbscout.data.data.local.utils

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edgetech.bbscout.data.data.local.BBScoutDao
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.OtherDataEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity


@Database(
    entities = [
        EntryEntity::class,
        OtherDataEntity::class,
        UserLocationEntity::class,
    ], version = 1
)
abstract class BBScoutDatabase : RoomDatabase() {
    abstract val bbScoutDao: BBScoutDao
}