package com.edgetech.bbscout.data.repositories

import com.edgetech.bbscout.data.data.local.BBScoutDao
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.utils.SimpleResource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainRepositoryImplementation @Inject constructor(
    private val bbScoutDao: BBScoutDao
) : MainRepository {
    override suspend fun addEntryRecord(entryRecord: EntryRecord): SimpleResource<Long> {
        try {
            val entryId = bbScoutDao.insertEntityRecord(entryRecord.entryEntity)
            coroutineScope {
                listOf(launch {
                    if (entryRecord.location != null) {
                        bbScoutDao.insertUserLocation(entryRecord.location.copy(entryId = entryId))
                    }
                }, launch {
                    if (entryRecord.otherData.isNotEmpty()) {
                        bbScoutDao.insertOtherData(entryRecord.otherData.map { it.copy(entryId = entryId) })
                    }
                },
                    launch {
                        if (entryRecord.billboardData != null) {
                            bbScoutDao.insertBillboardDataEntity(entryRecord.billboardData.copy(entryId = entryId))
                        }
                    }).joinAll()
            }
            return SimpleResource.Success(entryId)
        } catch (e: Exception) {
            return SimpleResource.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun getAllEntries(): SimpleResource<List<EntryRecord>> {
        try {
            val entries = bbScoutDao.getAllEntryRecords()
            return SimpleResource.Success(entries)
        } catch (e: Exception) {
            return SimpleResource.Error(e.message ?: "Unknown Error")
        }
    }


}