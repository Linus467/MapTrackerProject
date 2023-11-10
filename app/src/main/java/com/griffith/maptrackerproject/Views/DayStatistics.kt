package com.griffith.maptrackerproject.Views

import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import org.osmdroid.util.GeoPoint
import java.sql.Date

class DayStatistics: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle??) {
        super.onCreate(savedInstanceState)
        val tempDate = (intent.getSerializableExtra("DateToDisplay") as? Long)
        val date = Date(tempDate!!)
        setContent{
            Text("Day Statistics for the $date")
        }
    }

    fun calculateDistance(locations: List<GeoPoint>): Float {
        var totalDistance: Float = 0.0F

        for(i in 0 until locations.size -2){
            val start = locations[i]
            val end = locations[i+1]

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

}