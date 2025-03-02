package com.edgetech.bbscout.data.repositories

import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.AuthEntity
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponseList
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AuthResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BBScoutUserStat
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BBScoutUserStatResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ChangePasswordRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.FileResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.LoginRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.UserResponse
import com.edgetech.bbscout.data.data.remote.gen_ai.data_model.bbscout.BBScoutAiAnalyserResponse
import com.edgetech.bbscout.data.utils.SimpleResource
import okhttp3.MultipartBody

interface MainRepository {

    suspend fun addAuth(authEntity: AuthEntity): SimpleResource<AuthEntity>

    suspend fun getAuth(): SimpleResource<AuthEntity?>

    suspend fun deleteAuth(): SimpleResource<Unit>

    suspend fun addEntryRecord(entryRecord: EntryRecord): SimpleResource<EntryRecord>

    suspend fun getAllEntries(): SimpleResource<List<EntryRecord>>

    suspend fun getCaptureRecord(entry: String): SimpleResource<EntryRecord?>

    suspend fun login(request: LoginRequest): SimpleResource<AuthResponse>

    suspend fun loginWithGoogle(request: LoginRequest): SimpleResource<AuthResponse>

    suspend fun getProfile(): SimpleResource<UserResponse>

    suspend fun getAccounts(): SimpleResource<AccountResponseList>

    suspend fun changePassword(request: ChangePasswordRequest): SimpleResource<AuthResponse>

    suspend fun switchAccount(request: AccountResponse): SimpleResource<AuthResponse>

    suspend fun uploadFile(file: MultipartBody): SimpleResource<FileResponse>

    suspend fun analyzeFile(file: MultipartBody): SimpleResource<BBScoutAiAnalyserResponse>

    suspend fun getMonthlyStats(): SimpleResource<BBScoutUserStat>
}