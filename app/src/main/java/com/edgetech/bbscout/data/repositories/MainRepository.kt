package com.edgetech.bbscout.data.repositories

import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.utils.SimpleResource

interface MainRepository {

    suspend fun addEntryRecord(entryRecord: EntryRecord): SimpleResource<Long>

    suspend fun getAllEntries(): SimpleResource<List<EntryRecord>>

    suspend fun getCaptureRecord(entry: Long): SimpleResource<EntryRecord?>
}