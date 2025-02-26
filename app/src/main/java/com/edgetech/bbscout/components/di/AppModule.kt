package com.edgetech.bbscout.components.di

import android.app.Application
import androidx.room.Room
import com.edgetech.bbscout.components.file_saver.FileSaver
import com.edgetech.bbscout.components.file_saver.FileSaverImpl
import com.edgetech.bbscout.components.location.GetLocationInfo
import com.edgetech.bbscout.components.location.GetLocationInfoImplementation
import com.edgetech.bbscout.components.utils.isDebug
import com.edgetech.bbscout.data.data.local.BBScoutDao
import com.edgetech.bbscout.data.data.local.utils.BBScoutDatabase
import com.edgetech.bbscout.data.data.remote.bbscout_api.api.BBScoutApi
import com.edgetech.bbscout.data.data.remote.bbscout_api.api.RepositoryHelper
import com.edgetech.bbscout.data.data.remote.gen_ai.llm.FulltextAndImageInference
import com.edgetech.bbscout.data.data.remote.gen_ai.llm.GeminiInference
import com.edgetech.bbscout.data.repositories.MainRepository
import com.edgetech.bbscout.data.repositories.MainRepositoryImplementation
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClient(db: BBScoutDatabase, application: Application): OkHttpClient {
        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        //WifiService.instance.initializeWithApplicationContext(application)

        val client: OkHttpClient =
            OkHttpClient.Builder().apply {
                // addInterceptor(ConnectivityInterceptor())
                readTimeout(40, TimeUnit.SECONDS)
                writeTimeout(40, TimeUnit.SECONDS)
                addInterceptor { chain ->
                    val request = chain.request()

                    var token = ""
                    try {
                        runBlocking {
                            token = db.bbScoutDao.getAuth().accessToken ?: ""
                        }
                    } catch (e: Exception) {

                    }

                    val requestBuilder = chain.request().newBuilder().apply {

                        if (!request.url.pathSegments.contains(
                                "auth"
                            )
                        ) {
                            addHeader("Authorization", "Bearer $token")
                        }


                    }.build()
                    return@addInterceptor chain
                        .proceed(requestBuilder)
                }
                if (isDebug) addInterceptor(interceptor)
            }.build()


        return client
    }


    @Provides
    @Singleton
    fun provideApi(client: OkHttpClient): BBScoutApi {

        val gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl("https://scout.edgetech.co.ke/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(BBScoutApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBBScoutDatabase(app: Application): BBScoutDatabase {
        return Room.databaseBuilder(
            app, BBScoutDatabase::class.java, "bbscout_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBBScoutDao(db: BBScoutDatabase): BBScoutDao = db.bbScoutDao

    @Singleton
    @Provides
    fun provideRepoHelper(arSpringApi: BBScoutApi, db: BBScoutDatabase) =
        RepositoryHelper(arSpringApi, db)

    @Provides
    @Singleton
    fun provideMainRepository(
        bbScoutDao: BBScoutDao,
        api: BBScoutApi,
        repoHelper: RepositoryHelper
    ): MainRepository =
        MainRepositoryImplementation(bbScoutDao, api, repoHelper)

    @Provides
    @Singleton
    fun provideRemoteInference(): FulltextAndImageInference =
        GeminiInference()

    @Provides
    @Singleton
    fun provideFileSaver(
        app: Application,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): FileSaver = FileSaverImpl(app.baseContext, ioDispatcher)

    @Provides
    @Singleton
    fun provideGetLocationInfo(app: Application): GetLocationInfo =
        GetLocationInfoImplementation(app.baseContext)


}