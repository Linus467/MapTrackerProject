package com.griffith.maptrackerproject.DB

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "locations")
data class Locations(val lat:Double, val long: Double, val alt: Double = 0.0, val dat: Date) {
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0
    val Latitude: Double = lat
    val Longitude: Double = long
    val Altitude: Double = alt
    val Date: Date = dat
}