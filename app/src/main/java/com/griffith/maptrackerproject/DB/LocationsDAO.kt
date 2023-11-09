package com.griffith.maptrackerproject.DB

import androidx.room.Insert
import androidx.room.Query

interface LocationsDAO {
    @Insert
    suspend fun insert(location: Locations)

    @Query("SELECT * FROM locations where date = :date")
    suspend fun getLocationsForDay(date: String): List<Locations>


}