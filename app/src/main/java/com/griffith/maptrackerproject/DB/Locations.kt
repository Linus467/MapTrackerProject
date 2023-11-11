package com.griffith.maptrackerproject.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "locations")
data class Locations(
    @ColumnInfo(name = "Latitude") val latitude: Double,
    @ColumnInfo(name = "Longitude") val longitude: Double,
    @ColumnInfo(name = "Altitude") val altitude: Double = 0.0,
    @ColumnInfo(name = "Date") val date: Date? = Date(0),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)