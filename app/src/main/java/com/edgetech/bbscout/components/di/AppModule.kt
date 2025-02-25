package com.edgetech.bbscout.components.di

import android.app.Application
import androidx.room.Room
import com.edgetech.bbscout.components.file_saver.FileSaver
import com.edgetech.bbscout.components.file_saver.FileSaverImpl
import com.edgetech.bbscout.components.location.GetLocationInfo
import com.edgetech.bbscout.components.location.GetLocationInfoImplementation
import com.edgetech.bbscout.data.data.local.BBScoutDao
import com.edgetech.bbscout.data.data.local.utils.BBScoutDatabase
import com.edgetech.bbscout.data.data.remote.gen_ai.llm.FulltextAndImageInference
import com.edgetech.bbscout.data.data.remote.gen_ai.llm.GeminiInference
import com.edgetech.bbscout.data.repositories.MainRepository
import com.edgetech.bbscout.data.repositories.MainRepositoryImplementation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
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

    @Provides
    @Singleton
    fun provideMainRepository(bbScoutDao: BBScoutDao): MainRepository =
        MainRepositoryImplementation(bbScoutDao)

    @Provides
    @Singleton
    fun provideRemoteInference(): FulltextAndImageInference =
        GeminiInference()

    @Provides
    @Singleton
    fun provideFileSaver(app: Application, @IoDispatcher ioDispatcher: CoroutineDispatcher): FileSaver = FileSaverImpl(app.baseContext, ioDispatcher)

    @Provides
    @Singleton
    fun provideGetLocationInfo(app: Application): GetLocationInfo = GetLocationInfoImplementation(app.baseContext)


}