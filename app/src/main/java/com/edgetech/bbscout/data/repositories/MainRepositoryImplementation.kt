package com.edgetech.bbscout.data.repositories

import com.edgetech.bbscout.data.data.local.BBScoutDao
import com.edgetech.bbscout.data.data.local.dto.EntryRecord
import com.edgetech.bbscout.data.data.local.enities.AuthEntity
import com.edgetech.bbscout.data.data.remote.bbscout_api.api.BBScoutApi
import com.edgetech.bbscout.data.data.remote.bbscout_api.api.RepositoryHelper
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponseList
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AuthResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BBScoutUserStatResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ChangePasswordRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.FileResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.LoginRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.UserResponse
import com.edgetech.bbscout.data.data.remote.gen_ai.data_model.bbscout.BBScoutAiAnalyserResponse
import com.edgetech.bbscout.data.utils.BBScoutException
import com.edgetech.bbscout.data.utils.SimpleResource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

class MainRepositoryImplementation @Inject constructor(
    private val bbScoutDao: BBScoutDao,
    private val bbScoutApi: BBScoutApi,
    private val repoHelper: RepositoryHelper,
) : MainRepository {
    override suspend fun addAuth(authEntity: AuthEntity): SimpleResource<AuthEntity> {
        try {
            bbScoutDao.insertAuthResponse(authEntity)
            return SimpleResource.Success(authEntity)
        } catch (e: Exception) {
            return SimpleResource.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun getAuth(): SimpleResource<AuthEntity?> {
        try {
            val auth = bbScoutDao.getAuth()
            return SimpleResource.Success(auth)
        } catch (e: Exception) {
            return SimpleResource.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun deleteAuth(): SimpleResource<Unit> {
        try {
            bbScoutDao.deleteAuthObject()
            return SimpleResource.Success(Unit)
        } catch (e: Exception) {
            return SimpleResource.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun addEntryRecord(entryRecord: EntryRecord): SimpleResource<EntryRecord> {

        val campaign = EntryRecord.toCampaignResponse(entryRecord)

        val newBillboard = campaign.billboard

        if (newBillboard != null) {
            val billBoardResponse = repoHelper.apiDbRequestOrFail {
                bbScoutApi.createBillboard(newBillboard)
            }
            if (billBoardResponse.data != null){
                val newCampaign = campaign.copy(billboard = null, billboardId = billBoardResponse.data!!.id)
                val campaignResponse = repoHelper.apiDbRequestOrFail {
                    bbScoutApi.createCampaign(newCampaign)
                }
                if (campaignResponse.data != null){
                    val newRecord = EntryRecord.fromCampaignResponse(campaignResponse.data!!)
                    insertRecord(newRecord)
                    return SimpleResource.Success(newRecord)
                }
                else {
                    return SimpleResource.Error(campaignResponse.message ?: "", campaignResponse.exceptionType ?: BBScoutException())
                }
            }
            else {
                return SimpleResource.Error(billBoardResponse.message ?: "", billBoardResponse.exceptionType ?: BBScoutException())
            }

        }
        else {
            return SimpleResource.Error("No Billboard Data")
        }

    }


    private suspend fun insertRecord(newRecord: EntryRecord): SimpleResource<EntryRecord>{

        try {
            val entryId = bbScoutDao.insertEntityRecord(newRecord.entryEntity)
            coroutineScope {
                listOf(launch {
                    if (newRecord.location != null) {
                        bbScoutDao.insertUserLocation(newRecord.location.copy(entryId = entryId))
                    }
                }, launch {
                    if (newRecord.otherData.isNotEmpty()) {
                        bbScoutDao.insertOtherData(newRecord.otherData.map { it.copy(entryId = entryId) })
                    }
                },
                    launch {
                        if (newRecord.billboardData != null) {
                            bbScoutDao.insertBillboardDataEntity(newRecord.billboardData.copy(entryId = entryId))
                        }
                    }).joinAll()
            }

        } catch (e: Exception) {
            // return SimpleResource.Error(e.message ?: "Unknown Error")
        }
        return SimpleResource.Success(newRecord)
    }

    override suspend fun getAllEntries(): SimpleResource<List<EntryRecord>> {


        val billboardsUpload = repoHelper.apiDbRequestOrFail {
            bbScoutApi.getBillboardUploads()
        }
        if (billboardsUpload.data != null){
            val billboards =  billboardsUpload.data!!.data?.map { EntryRecord.fromBillboardResponse(it) } ?: emptyList()
            billboards.forEach {
                insertRecord(it)
            }
            return SimpleResource.Success(billboards)
        } else {
            try {
                val entries = bbScoutDao.getAllEntryRecords()
                return SimpleResource.Success(entries)
            } catch (e: Exception) {
                return SimpleResource.Error(e.message ?: "Unknown Error")
            }
        }
    }

    override suspend fun getCaptureRecord(entry: String): SimpleResource<EntryRecord?> {

        val billboardsUpload = repoHelper.apiDbRequestOrFail {
            bbScoutApi.getBillboard(entry)
        }

        if (billboardsUpload.data != null) {
            val remBillboard = billboardsUpload.data!!
            val billboard = EntryRecord.fromBillboardResponse(remBillboard)
            insertRecord(billboard)
            return SimpleResource.Success(billboard)
        } else
            try {
                val entries = bbScoutDao.getEntryRecordsById(entry)
                return SimpleResource.Success(entries.firstOrNull())
            } catch (e: Exception) {
                return SimpleResource.Error(e.message ?: "Unknown Error")
            }
    }


    override suspend fun login(request: LoginRequest): SimpleResource<AuthResponse> {
        return repoHelper.apiDbRequestOrFail {
            bbScoutApi.login(request)
        }
    }

    override suspend fun loginWithGoogle(request: LoginRequest): SimpleResource<AuthResponse> {
        return repoHelper.apiDbRequestOrFail {
            bbScoutApi.loginWithGoogle(request)
        }
    }

    override suspend fun getProfile(): SimpleResource<UserResponse> {
        return repoHelper.apiDbRequestOrFail {
            bbScoutApi.getProfile()
        }
    }

    override suspend fun getAccounts(): SimpleResource<AccountResponseList> {
        return repoHelper.apiDbRequestOrFail {
            bbScoutApi.getAccounts()
        }
    }

    override suspend fun changePassword(request: ChangePasswordRequest): SimpleResource<AuthResponse> {
        return  repoHelper.apiDbRequestOrFail {
            bbScoutApi.changePassword(request)
        }
    }

    override suspend fun switchAccount(request: AccountResponse): SimpleResource<AuthResponse> {
        return repoHelper.apiDbRequestOrFail {
            bbScoutApi.switchAccount(request)
        }
    }

    override suspend fun uploadFile(file: MultipartBody): SimpleResource<FileResponse> {
        return repoHelper.apiDbRequestOrFail {
            bbScoutApi.uploadFile(file)
        }
    }

    override suspend fun analyzeFile(file: MultipartBody): SimpleResource<BBScoutAiAnalyserResponse> {
        return repoHelper.apiDbRequestOrFail {
            bbScoutApi.anylizeFile(file)
        }
    }

    override suspend fun getMonthlyStats(): SimpleResource<BBScoutUserStatResponse> {
        return repoHelper.apiDbRequestOrFail {
            bbScoutApi.getMonthlyStats()
        }
    }


}