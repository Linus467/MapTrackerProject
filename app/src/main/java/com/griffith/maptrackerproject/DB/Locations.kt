package com.griffith.maptrackerproject.DB

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

fun List<Locations>.groupLocationsByDay(): List<Pair<Date, List<Locations>>> {
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    val locationsByDate = this.groupBy {
        dateFormat.format(it.date)
    }

    return locationsByDate.map { (dateString, locations) ->
        val date = dateFormat.parse(dateString) ?: Date()
        Pair(date, locations)
    }
}
fun List<Locations>.calculateAveragePosition(): GeoPoint{
    var averageLocationLat: Double = 0.0
    var averageLocationLong: Double = 0.0
    var averageLocationAlt: Double = 0.0
    for(loc in this){
        averageLocationLat += loc.latitude
        averageLocationLong += loc.longitude
        averageLocationAlt += loc.altitude
    }
    if(averageLocationLat != 0.0 && averageLocationLong != 0.0){
        averageLocationLat /= this.size
        averageLocationLong /= this.size
    }
    if(averageLocationAlt != 0.0){
        averageLocationAlt /= this.size
    }
    return GeoPoint(averageLocationLong, averageLocationLat, averageLocationAlt)
}

fun List<Locations>.calculateDistance(): Float {
    var totalDistance: Float = 0.0F

    for(i in 0 .. this.size -2){
        val start = this[i]
        val end = this[i+1]

        val result = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            result
        )
        totalDistance += result[0]
    }
    return totalDistance
}

fun List<Locations>.calculateHourlyHeightDistance() : Map<Int, Float>{
    //totalHeight travels by a user for the locations list given
    var hourlyHeight = mutableMapOf<Int, Float>()

    for( hour in 0..23){
        hourlyHeight[hour] = 0.0f
    }

    //Get all the locations by Hour
    val locationsByHour = this.groupBy {
        val calendar = Calendar.getInstance()
        calendar.time = it.date
        calendar.get(Calendar.HOUR_OF_DAY)
    }

    //set the location into
    for ((hour, locationsInHour) in locationsByHour) {
        var totalDistance = 0f
        for (i in 0 until locationsInHour.size - 1) {
            val startLocation = locationsInHour[i]
            val endLocation = locationsInHour[i + 1]
            val heightDifference =  endLocation.altitude - startLocation.altitude
            if(heightDifference > 0){
                totalDistance += heightDifference.toFloat()
            }
        }
        hourlyHeight[hour] = totalDistance
    }
    return hourlyHeight
}
//Calculates the Hourly path taken
fun List<Locations>.calculateHourlyDistance():  Map<Int,Float>{
    val hourlyDistances = mutableMapOf<Int, Float>()

    for (hour in 0..23){
        hourlyDistances[hour] = 0.0f
    }

    //Get all the locations by Hour
    val locationsByHour = this.groupBy {
        val calendar = Calendar.getInstance()
        calendar.time = it.date
        calendar.get(Calendar.HOUR_OF_DAY)
    }

    //set the location into
    for ((hour, locationsInHour) in locationsByHour) {
        var totalDistance = 0f
        for (i in 0 until locationsInHour.size - 1) {
            val startLocation = locationsInHour[i]
            val endLocation = locationsInHour[i + 1]

            val results = FloatArray(1)
            Location.distanceBetween(
                startLocation.latitude, startLocation.longitude,
                endLocation.latitude, endLocation.longitude,
                results
            )
            totalDistance += results[0]
        }
        hourlyDistances[hour] = totalDistance
    }
    return hourlyDistances
}

fun List<Locations>.calculatePositiveAltitue() : Float {
    var totalHeight: Float = 0.0F

    for(i in 0 .. this.size -2){
        val start = this[i]
        val end = this[i+1]

        if(end.altitude > start.altitude){
            totalHeight += (end.altitude - start.altitude).toFloat()
        }
    }
    return totalHeight
}

fun List<Locations>.calculateNegativeAltitue() : Float {
    var totalHeight: Float = 0.0F

    for(i in 0 .. this.size -2){
        val start = this[i]
        val end = this[i+1]

        if(end.altitude < start.altitude){
            totalHeight += (start.altitude - end.altitude).toFloat()
        }
    }
    return totalHeight
}