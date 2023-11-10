package com.griffith.maptrackerproject.Views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.griffith.maptrackerproject.R
import com.griffith.maptrackerproject.Views.RouteDisplay
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

class History : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            //TODO get locationsPoints from database and replace sample data
            //HistoryColumn(liveLocations = sampleLiveLocationsPreview())
            //TODO Nothing is displayed on appstart
            HistoryScreen()
        }
    }
}

@Composable
fun HistoryScreen(){
    HistoryColumn(liveLocations = sampleLiveLocationsPreview())
}
@Preview(showBackground = true)
@Composable
fun HistoryColumnPreview() {
    HistoryColumn(sampleLiveLocationsPreview())
}

fun sampleLiveLocationsPreview(): Map<Date, GeoPoint> {
    val sampleLiveLocations = mapOf(
        Date(123, 0, 1) to GeoPoint(37.7749, -122.4194),
        Date(123, 0, 2) to GeoPoint(23.4749, -39.4394),
        Date(123, 0, 2) to GeoPoint(34.0522, -118.2437),
        Date(123, 0, 3) to GeoPoint(40.7128, -74.0060),
        Date(123, 0, 4) to GeoPoint(40.7128, -74.0060)
    )
    return sampleLiveLocations
}

@Composable
fun HistoryColumn(liveLocations: Map<Date,GeoPoint>){

    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .height(30.dp),
        contentAlignment = Alignment.Center

    ){
        Text(
            "History",
            Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    LazyColumn(
        Modifier
            .padding(5.dp,23.dp,5.dp,5.dp)
    ){
        items(groupLocationsByDay(liveLocations)){ (Date, Locations) ->
            DayRow(
                geoPoints = Locations,
                date = Date,
                onClick = { onDayRowClick(Date,context) }
            )
        }
    }
}

@Composable
fun DayRow(geoPoints : List<GeoPoint>, date: Date, onClick: () -> Unit){
    val formatter = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ){
        item{
            Column {
                Text(
                    text = formatter.format(date),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Locations: ${geoPoints.size}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Locations: ${geoPoints.first()}",
                    fontSize = 14.sp
                )
            }
        }
    }
}

//Context given by Composable functions
fun onDayRowClick(date: Date, context: Context){
    val intent = Intent(context, DayStatistics::class.java);
    intent.putExtra("DateToDisplay", date.time);
    context.startActivity(intent);
}

fun groupLocationsByDay(liveLocations: Map<Date, GeoPoint>): List<Pair<Date, List<GeoPoint>>> {
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    // Groups the Geolocations by date
    val locationsByDate = liveLocations.entries.groupBy {
        dateFormat.format(it.key)
    }
    //Getting all locations based on the date
    for(locationsOrder in liveLocations){
        for(locationsGrouping in liveLocations){

        }
    }

    Log.d("groupLocationsByDay", "Grouped locations by date: $locationsByDate")

    return locationsByDate.map { (dateString, entries) ->
        val date = dateFormat.parse(dateString) ?: Date()
        val locations = entries.map { it.value }
        Log.d("groupLocationsByDay", "Date: $dateString, Locations: $locations")
        Pair(date, locations)
    }.also {
        Log.d("groupLocationsByDay", "Final grouped list: $it")
    }
}


