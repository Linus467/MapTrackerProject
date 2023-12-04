package com.griffith.maptrackerproject.Views

import DayStatisticsViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import co.yml.charts.axis.AxisData
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import com.griffith.maptrackerproject.DB.toGeoPoints
import com.griffith.maptrackerproject.ui.theme.GreenDark
import com.griffith.maptrackerproject.ui.theme.GreenLight
import com.griffith.maptrackerproject.ui.theme.GreenPrimary
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class DayStatistics : ComponentActivity(){

    @Inject
    lateinit var locationsDAO: LocationsDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //View Model to keep the Locations gathering before the opening of the activity
        val viewModel: DayStatisticsViewModel = DayStatisticsViewModel(locationsDAO)
        //Formatting date
        val dateString = intent.getStringExtra("date") ?: ""
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = try {
            dateFormat.parse(dateString)
        } catch (e: ParseException) {
            Date()
        }
        //Calculating distances for display
        viewModel.loadHourlyDistances(date)

        setContent {
            DayStatisticsPage(date = date, mapVisible = true, locationsDAO, viewModel)
        }
    }
}

@Composable
fun DayStatisticsPage(date: Date, mapVisible: Boolean, locationsDAO: LocationsDAO, viewModel: DayStatisticsViewModel) {
    val hourlyDistances by viewModel.hourlyDistances.collectAsState()
    val hourlyHeight by viewModel.hourlyHeight.collectAsState()
    val liveLocations by viewModel.liveLocations.collectAsState()
    val averageLocation by viewModel.locationsAverage.collectAsState()

    //List of map and statistics
    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 30.dp)) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Check if hourlyDistances is not null
        if (hourlyDistances != null) {
            item {
                // Map display
                if (mapVisible) {
                    walkMap(liveLocations, averageLocation)
                }
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                // Display hourly movement based on the data
                hourlyMovement(hourlyDistances!!)
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                elevationChange(hourlyHeight!!)
            }
        } else {
            item {
                Text("Loading data...", modifier = Modifier)
            }
        }
    }
    val formatter = SimpleDateFormat("dd.MM.yyyy")
    HeaderBox("${formatter.format(date)}")
}

//Header box of the entire page
@Composable
fun HeaderBox(text: String) {
    Row{
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GreenDark),
            Alignment.Center
        ) {
            Text(
                text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

//Map if the walked distance that day
@Composable
fun walkMap(liveLocations: List<Locations>, averageLocation: GeoPoint){
    val examplePoints = listOf(
        GeoPoint(52.5194, 13.4010),
        GeoPoint(52.5163, 13.3777),
        GeoPoint(52.5186, 13.3762),
        GeoPoint(52.5208, 13.4094)
    )
    Row{
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GreenPrimary),
            Alignment.Center
        ){
            Text(
                "Map",
                Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }


    Spacer(modifier = Modifier.height(5.dp))

    Row{
        //Chart offset is not working in preview
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(300.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GreenPrimary),
            Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        controller.setCenter(averageLocation)
                        setMapZoomToShowAllLocations(this, liveLocations)
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    if(liveLocations.isNotEmpty()){
                        mapView.controller.setCenter(averageLocation)
                        val polyline = Polyline().apply {
                            outlinePaint.color = Color.Black.toArgb()
                            outlinePaint.strokeWidth = 8f
                            setPoints(liveLocations.toGeoPoints())
                        }
                        setMapZoomToShowAllLocations(mapView, liveLocations)
                        mapView.overlays.add(polyline)
                    }
                    mapView.invalidate()
                }
            )
        }
    }
}


fun setMapZoomToShowAllLocations(mapView: MapView, locations: List<Locations>) {
    if (locations.isEmpty()) return

    var minLat = 300.0
    var maxLat = -300.0
    var minLon = 300.0
    var maxLon = -300.0

    // Calculate bounds
    for (location in locations) {
        if (location.latitude > maxLat)
            maxLat = location.latitude
        if (location.latitude < minLat)
            minLat = location.latitude
        if (location.longitude > maxLon)
            maxLon = location.longitude
        if (location.longitude < minLon)
            minLon = location.longitude
    }

    if ((minLat == maxLat && minLon == maxLon) || (minLat > maxLat && minLon > maxLon)) {
        mapView.controller.setZoom(12)
        return
    }

    val boundingBox = BoundingBox(maxLat, maxLon, minLat, minLon)
    mapView.minZoomLevel = 11.0
    mapView.zoomToBoundingBox(boundingBox, false)
    mapView.invalidate()
}

//a Statistic that shows the elevation change made that day
@Composable
fun elevationChange(hourlyDistances: Map<Int, Float>) {
    val pointsData = hourlyDistances.map { (hour, elevation) ->
        Point(hour.toFloat(), elevation, "$elevation m")
    }
    Row{
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GreenPrimary),
            Alignment.Center
        ){
            Text(
                "Elevation",
                Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.White
            )
        }

    }
    Row{
        //Chart offset is not working in preview
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(300.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GreenPrimary),
            Alignment.Center
        ) {
            pointsData(pointsData)
        }
    }
}


//Statistic of the movement one made in one day
@Composable
fun hourlyMovement(hourlyDistances: Map<Int, Float>) {
    val pointsData = hourlyDistances.map { (hour, distance) ->
        Point(hour.toFloat(), distance, "$distance m")
    }
    Row(){
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GreenPrimary),
            Alignment.Center
        ){
            Text(
                "Hourly movement",
                Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.White
            )
        }

    }
    Row(){
        //Chart offset is not working in preview
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(300.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GreenPrimary),
            Alignment.Center
        ) {
            pointsData(pointsData)
        }
    }
}

//preview of the statistic
@Preview
@Composable
fun pointsDataPreview(){
    val pointsData: List<Point> =
        listOf(Point(0f, 40f,"40km"), Point(1f, 90f), Point(2f, 0f), Point(3f, 60f), Point(4f, 10f))
    pointsData(pointsData = pointsData)
}


//Method used to display data like Distance Walked in a day
@Composable
fun pointsData(pointsData: List<Point>){
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp)
        .steps(pointsData.size - 1)
        .labelData { i -> "${i.toString()}:00" }
        .labelAndAxisLinePadding(10.dp)
        .bottomPadding(10.dp)
        .build()

    val steps = 5
    val yAxisData = AxisData.Builder()
        .steps(steps)
        .labelAndAxisLinePadding(10.dp)
        .labelData { i ->
            val yScale = pointsData.maxBy { it.y }.y / steps
            (i * yScale).formatToSinglePrecision() + " m"
        }.build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    LineStyle(),
                    IntersectionPoint(),
                    SelectionHighlightPoint(color = GreenLight),
                    ShadowUnderLine(),
                    SelectionHighlightPopUp(backgroundColor = GreenPrimary)
                )
            ),
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(),
        backgroundColor = Color.White
    )
    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        lineChartData = lineChartData
    )
}
