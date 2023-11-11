package com.griffith.maptrackerproject.DB

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//TODO IMPORT Hilt into gradle and use it in here
@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule{
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase{
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "Location_data"
        ).build()
    }

    @Provides
    fun provideLocationsDAO(appDatabase: AppDatabase) : LocationsDAO{
        return appDatabase.locationDAO()
    }
}