package com.griffith.maptrackerproject.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationsDAO {
    @Insert
    suspend fun insert(vararg location: Locations)

    @Query("SELECT * FROM locations where date = :date")
    suspend fun getLocationsForDay(date: String): List<Locations>

    @Query("SELECT * FROM locations")
    fun getAllLocations(): Flow<List<Locations>>

}