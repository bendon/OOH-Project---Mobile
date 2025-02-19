package com.edgetech.bbscout.components.di

import android.app.Application
import androidx.room.Room
import com.edgetech.bbscout.data.data.local.BBScoutDao
import com.edgetech.bbscout.data.data.local.utils.BBScoutDatabase
import com.edgetech.bbscout.data.repositories.MainRepository
import com.edgetech.bbscout.data.repositories.MainRepositoryImplementation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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


}