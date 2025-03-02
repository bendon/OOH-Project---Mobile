package com.edgetech.bbscout.data.data.remote.bbscout_api.api

import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AccountResponseList
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ApiResponsePage
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.AuthResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BBScoutUserStat
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.BillboardResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.CampaignResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ChangePasswordRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.FileResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.LoginRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.RegisterRequest
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.UserResponse
import com.edgetech.bbscout.data.data.remote.bbscout_api.model.api_exception.ApiStatusResponse
import com.edgetech.bbscout.data.data.remote.gen_ai.data_model.bbscout.BBScoutAiAnalyserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface BBScoutApi {


    @POST("api/v1/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @GET("api/v1/auth/refresh/account")
    suspend fun refreshToken(@Header("Authorization") token: String): Response<AuthResponse>


    @POST("api/v1/auth/google/verify")
    suspend fun loginWithGoogle(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("api/v1/en/switch/account")
    suspend fun switchAccount(@Body request: AccountResponse): Response<AuthResponse>

    @GET("api/v1/en/profile")
    suspend fun getProfile(): Response<UserResponse>

    @GET("api/v1/en/accounts")
    suspend fun getAccounts(): Response<AccountResponseList>

    @POST("api/v1/en/sl/change/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthResponse>

    @GET("api/v1/en/sl/my/billboardds/uploads")
    suspend fun getBillboardUploads(): Response<ApiResponsePage<BillboardResponse>>

    @GET("api/v1/en/sl/my/billboards/{id}")
    suspend fun getBillboard(@Path("id") id: String): Response<BillboardResponse>

    @POST("api/v1/en/sl/billboard/campaign")
    suspend fun createCampaign(@Body request: CampaignResponse): Response<CampaignResponse>

    @POST("api/v1/en/sl/billboard")
    suspend fun createBillboard(@Body request: BillboardResponse): Response<BillboardResponse>

    @POST("api/v1/en/sl/upload/files")
    suspend fun uploadFile(@Body body: MultipartBody): Response<FileResponse>

    @GET("api/v1/auth/file/{name}")
    suspend fun getFile(
        @Path("name") name: String
    ): Response<FileResponse>

    @POST("api/v1/auth/gemini/data/extraction")
    suspend fun analyzeFile(
        @Body body: MultipartBody
    ): Response<BBScoutAiAnalyserResponse>

    @GET("api/v1/en/sl/report/billboard/user/monthly")
    suspend fun getMonthlyStats() : Response<BBScoutUserStat>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: LoginRequest): Response<ApiStatusResponse>

}