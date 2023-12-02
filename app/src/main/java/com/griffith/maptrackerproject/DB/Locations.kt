package com.griffith.maptrackerproject.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint
import java.util.Date

@Entity(tableName = "locations")
data class Locations(
    @ColumnInfo(name = "Latitude") val latitude: Double,
    @ColumnInfo(name = "Longitude") val longitude: Double,
    @ColumnInfo(name = "Altitude") val altitude: Double = 0.0,
    @ColumnInfo(name = "Date") val date: Date? = Date(0),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
fun Locations.toGeoPoint(): GeoPoint{
    return GeoPoint(latitude, longitude, altitude)
}

fun List<Locations>.toGeoPoints(): List<GeoPoint> {
    return this.map { location ->
        GeoPoint(location.latitude, location.longitude, location.altitude)
    }
}

fun Locations.toList(): MutableList<List<Locations>> {
    return this.toList();
}
