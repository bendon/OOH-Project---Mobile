package com.edgetech.bbscout.data.data.remote.bbscout_api.api

import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ApiResponsePage
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AuthResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BillboardResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.CampaignResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ChangePasswordRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.FileResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.LoginRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BBScoutApi {

    @POST("/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("/auth/google/verify")
    suspend fun loginWithGoogle(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("/en/switch/account")
    suspend fun switchAccount(@Body request: AccountResponse): Response<AuthResponse>

    @GET("/en/profile")
    suspend fun getProfile(): Response<UserResponse>

    @GET("/en/accounts")
    suspend fun getAccounts(): Response<List<AccountResponse>>

    @POST("/en/sl/change/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthResponse>

    @GET("/en/sl/my/billboardds/uploads")
    suspend fun getBillboardUploads(): Response<ApiResponsePage<BillboardResponse>>

    @GET("/en/sl/my/billboards/{id}")
    suspend fun getBillboard(@Path("id") id: String): Response<BillboardResponse>

    @POST("/en/sl/billboard/campaign")
    suspend fun createCampaign(@Body request: CampaignResponse): Response<BillboardResponse>

    @POST("/en/sl/billboard")
    suspend fun createBillboard(@Body request: BillboardResponse): Response<BillboardResponse>

    @POST("/en/sl/upload/files")
    suspend fun uploadFile(@Body body: MultipartBody): Response<FileResponse>
}