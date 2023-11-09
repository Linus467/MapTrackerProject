package com.griffith.maptrackerproject.DB

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Locations::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDAO(): LocationsDAO
}