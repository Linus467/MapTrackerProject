package com.griffith.maptrackerproject.Views

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import com.griffith.maptrackerproject.DB.calculateDistance
import com.griffith.maptrackerproject.DB.calculateNegativeAltitue
import com.griffith.maptrackerproject.DB.calculatePositiveAltitue
import com.griffith.maptrackerproject.DB.filterLocationsOver30Kmph
import com.griffith.maptrackerproject.DB.groupLocationsByDay
import com.griffith.maptrackerproject.R
import com.griffith.maptrackerproject.ui.theme.GreenLight
import com.griffith.maptrackerproject.ui.theme.GreenPrimary
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class History : ComponentActivity() {

    @Inject
    lateinit var locationsDAO: LocationsDAO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HistoryMain(locationsDAO)
        }
    }


}


@Composable
fun HistoryMain(
    locationsDAO: LocationsDAO
){
    val context = LocalContext.current
    val mapIntent = Intent(context, RouteDisplay::class.java)
    val historyIntent = Intent(context, History::class.java)

    BottomBar(
        context = context,
        mapIntent = mapIntent,
        historyIntent = historyIntent,
    ) {
        HistoryColumn(locationsDAO)
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
        items(liveLocations.value.groupLocationsByDay()){ (date, locations) ->
            DayRow(
                locations = locations,
                date = date,
            )
        }
    }
}

//Shows on day that was recorded with its locations recorded where and on what date
@Composable
fun DayRow(locations : List<Locations>, date: Date){
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val context = LocalContext.current
    val statisticsIntent = Intent(context,DayStatistics::class.java)

    val filteredLocations = locations.filterLocationsOver30Kmph() as MutableList<Locations>
    LazyRow(
        modifier = Modifier
            .clickable {
                val sendDate: String = formatter.format(date)
                statisticsIntent.putExtra("date", sendDate)
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
                    text = if (locations.size == 1) "${locations.size} Location" else "${locations.size} Locations",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Row(){
                    Icon(
                        painter = painterResource(id = R.drawable.figure_walk),
                        contentDescription = "Location Icon",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(0.dp, 0.dp, 5.dp, 0.dp),
                        tint = Color.Black
                    )
                    Text(
                        text = "${String.format("%.2f",filteredLocations.calculateDistance() / 1000)} km",
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.arrow_up_right),
                        contentDescription = "Location Icon",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(0.dp, 0.dp, 5.dp, 0.dp),
                        tint = Color.Black
                    )
                    Text(
                        text = "${String.format("%.2f",filteredLocations.calculatePositiveAltitue())}m",
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.arrow_down_right),
                        contentDescription = "Location Icon",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(0.dp, 0.dp, 5.dp, 0.dp),
                        tint = Color.Black
                    )

                    Text(
                        text = "${String.format("%.2f",filteredLocations.calculateNegativeAltitue())}m",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

            }

        }
    }
}