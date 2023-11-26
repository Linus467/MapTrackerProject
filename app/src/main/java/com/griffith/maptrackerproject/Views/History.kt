package com.griffith.maptrackerproject.Views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import com.griffith.maptrackerproject.DB.toGeoPoint
import com.griffith.maptrackerproject.ui.theme.GreenLight
import com.griffith.maptrackerproject.ui.theme.GreenPrimary
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class History : ComponentActivity(){

    @Inject
    lateinit var locationsDAO: LocationsDAO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            HistoryColumn(locationsDAO)
        }
    }
}

//Shows all the days where locations where recorded
@Composable
fun HistoryColumn(locationsDAO: LocationsDAO){
    val liveLocations = remember { mutableStateOf<List<Locations>>(listOf()) }

    LaunchedEffect(key1 = Unit) {
        locationsDAO.getAllLocations().collect { locations ->
            liveLocations.value = locations
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp)),
        Alignment.Center
    ){
        Text(
            "History",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenPrimary),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    LazyColumn(
        Modifier
            .padding(5.dp, 40.dp, 5.dp, 5.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        items(groupLocationsByDay(liveLocations.value)){ (Date, Locations) ->
            DayRow(
                geoPoints = Locations,
                date = Date,
            )
        }
    }
}

//Shows on day that was recorded with its locations recorded where and on what date
@Composable
fun DayRow(geoPoints : List<GeoPoint>, date: Date){
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val context = LocalContext.current
    val statisticsIntent = Intent(context,DayStatistics::class.java)
    LazyRow(
        modifier = Modifier
            .clickable {
                val sendDate: String = formatter.format(date)
                statisticsIntent.putExtra("date",sendDate)
                context.startActivity(statisticsIntent)
            }
            .padding(top = 4.dp)
    ){
        item{
            Column(
                modifier = Modifier
                    .width(380.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenLight)
                    .padding(5.dp)

            ){
                Text(
                    text = formatter.format(date),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Locations: ${geoPoints.size}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Locations: ${geoPoints.first()}",
                    fontSize = 14.sp,
                    color = Color.Black

                )
            }

        }
    }
}

//Groups all locations by day to display them in this pattern in the list
fun groupLocationsByDay(liveLocations: List<Locations>): List<Pair<Date, List<GeoPoint>>> {
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    val locationsByDate = liveLocations.groupBy {
        dateFormat.format(it.date)
    }

    Log.d("groupLocationsByDay", "Grouped locations by date: $locationsByDate")

    return locationsByDate.map { (dateString, locations) ->
        val date = dateFormat.parse(dateString) ?: Date()
        val geoPoints = locations.map { it.toGeoPoint() }
        Pair(date, geoPoints)
    }.also {
        Log.d("groupLocationsByDay", "Final grouped list: $it")
    }
}


