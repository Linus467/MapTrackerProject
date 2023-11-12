package com.griffith.maptrackerproject.Views

import android.location.Location
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.griffith.maptrackerproject.ui.theme.Purple700
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date


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

@Preview()
@Composable
fun DayStatisticsPagePreview(){
    DayStatisticsPage(date = Date(2023,2,1),false)
}

@Composable
fun DayStatisticsPage(date: Date, mapVisible: Boolean) {
    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 30.dp)) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            //Map doesn't work in Preview
            if(mapVisible){
                walkMap()
            }
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            hourlyMovement()
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            elevationChange()
        }
    }
    val formatter = SimpleDateFormat("dd.MM.yyyy")
    HeaderBox("${formatter.format(date)}")
}

@Composable
fun HeaderBox(text: String) {
    Row{
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Purple700),
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

@Composable
fun walkMap(){
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
            .background(Purple700),
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
            .background(Purple700),
            Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        controller.setCenter(GeoPoint(52.5200, 13.4050))
                        controller.setZoom(14)
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    val polyline = Polyline().apply {
                        outlinePaint.color = android.graphics.Color.RED
                        outlinePaint.strokeWidth = 8f
                        setPoints(examplePoints)
                    }
                    mapView.overlays.add(polyline)

                    mapView.invalidate()
                }
            )
        }
    }
}

@Composable
fun elevationChange(){
    Row{
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Purple700),
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
            .background(Purple700),
            Alignment.Center
        ) {
            pointsDataPreview()
        }
    }
}


@Composable
fun hourlyMovement(){
    Row{
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Purple700),
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
    Row{
        //Chart offset is not working in preview
        Box(modifier = Modifier
            .fillMaxWidth()
            .size(300.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Purple700),
            Alignment.Center
        ) {
            pointsDataPreview()
        }
    }
}

@Preview
@Composable
fun pointsDataPreview(){
    val pointsData: List<Point> =
        listOf(Point(0f, 40f,"40km"), Point(1f, 90f), Point(2f, 0f), Point(3f, 60f), Point(4f, 10f))
    pointsData(pointsData = pointsData)
}

@Composable
fun pointsData(pointsData: List<Point>){
    val xAxisData = AxisData.Builder()
        .axisStepSize(100.dp)
        .steps(pointsData.size - 1)
        .labelData { i -> i.toString() }
        .labelAndAxisLinePadding(10.dp)
        .bottomPadding(10.dp)
        .build()

    val steps = 5
    val yAxisData = AxisData.Builder()
        .steps(steps)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val yScale = 30f / steps
            (i * yScale).formatToSinglePrecision() + " km"
        }.build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    LineStyle(),
                    IntersectionPoint(),
                    SelectionHighlightPoint(),
                    ShadowUnderLine(),
                    SelectionHighlightPopUp()
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
