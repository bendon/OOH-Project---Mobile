package com.edgetech.bbscout.data.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.BillboardDataEntity
import com.edgetech.bbscout.data.data.local.enities.EntryEntity
import com.edgetech.bbscout.data.data.local.enities.OtherDataEntity
import com.edgetech.bbscout.data.data.local.enities.UserLocationEntity


@Dao
interface BBScoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntityRecord(record: EntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtherData(data: List<OtherDataEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserLocation(location: UserLocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillboardDataEntity(billboard: BillboardDataEntity): Long

    @Transaction
    @Query("SELECT * FROM EntryEntity ORDER BY createdAt DESC")
    suspend fun getAllEntryRecords(): List<EntryRecord>

    @Transaction
    @Query("SELECT * FROM EntryEntity WHERE id = :entryId ORDER BY createdAt DESC")
    suspend fun getEntryRecordsById(entryId: Long): List<EntryRecord>

    @Query("DELETE FROM EntryEntity WHERE id = :entryId")
    suspend fun deleteEntryRecordById(entryId: Long)

    @Query("DELETE FROM OtherDataEntity WHERE entryId = :entryId")
    suspend fun deleteOtherDataById(entryId: Long)

    @Query("DELETE FROM UserLocationEntity WHERE entryId = :entryId")
    suspend fun deleteUserLocationById(entryId: Long)

}