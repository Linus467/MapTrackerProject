package com.griffith.maptrackerproject.Views

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import com.griffith.maptrackerproject.DB.toGeoPoint
import com.griffith.maptrackerproject.DB.toGeoPoints
import com.griffith.maptrackerproject.Interface.LocationUpdateController
import com.griffith.maptrackerproject.R
import com.griffith.maptrackerproject.Services.LocationService
import com.griffith.maptrackerproject.ui.theme.Purple700
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class RouteDisplay : ComponentActivity(), LocationUpdateController {

    @Inject
    lateinit var locationsDAO: LocationsDAO
    private lateinit var locationService: LocationService
    //Starting an pausing Locations recording on device
    private var isBound = false

    //Connecting to Location Service
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            isBound = true
            locationService.startLocationUpdates()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun startLocationUpdates() {
        if (isBound) {
            locationService.startLocationUpdates()
        }
    }

    override fun stopLocationUpdates() {
        if (isBound) {
            locationService.stopLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request Location Permissions if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            startLocationService()

            Intent(this,LocationService::class.java).also{
                bindService(it, connection, Context.BIND_AUTO_CREATE)
            }
        }

        setContent {
            DisplayRouteMain(locationsDAO,this)
        }

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


    override fun onDestroy() {
        super.onDestroy()
        //Disconnection from Location Service
        if(isBound){
            unbindService(connection)
            isBound = false
        }
    }
    private fun startLocationService(){
        val serviceIntent = Intent(this, LocationService::class.java)
        startService(serviceIntent)
    }
}

@Composable
fun DisplayRouteMain(locationsDAO: LocationsDAO, locationsUpdateController: LocationUpdateController){
    val context = LocalContext.current

    var locationsTrackingActive by remember{ mutableStateOf(false) }
    val mapIntent = Intent(context, RouteDisplay::class.java)
    val historyIntent = Intent(context, History::class.java)

    Scaffold(
        bottomBar = {
            BottomNavigation(modifier = Modifier
                .fillMaxWidth()
                .size(45.dp)
                .background(Purple700)
                .shadow(2.dp)

            ) {
                Button(onClick = { context.startActivity(mapIntent) }){
                    Icon(painter = painterResource(id = R.drawable.baseline_map_24), contentDescription = "Map View")
                }
                Button(
                    onClick = {
                        locationsTrackingActive = !locationsTrackingActive
                        if (locationsTrackingActive) {
                            locationsUpdateController.startLocationUpdates()
                        } else {
                            locationsUpdateController.stopLocationUpdates()
                        }
                    }
                ){
                    val conId = if (locationsTrackingActive){
                        R.drawable.pause_circle
                    }else{
                        R.drawable.play_circle_24
                    }
                    Icon(painter = painterResource(id = conId), contentDescription = "Tracking on off")
                }

                Button(onClick = {

                    context.startActivity(historyIntent)
                    }
                ) {
                    Icon(painterResource(id = R.drawable.baseline_history_24), contentDescription = "Map View")
                }
            }
        }
    ) {innerPadding->
        OsmMapView(locationsDAO, Modifier.padding(innerPadding))

        /*NavHost(navController, startDestination = Screen.MapView.route, Modifier.padding(innerPadding)) {
            composable(Screen.MapView.route) {  }
            composable(Screen.History.route) { HistoryColumn(liveLocations = liveLocations, navController) }
            composable(
                route = Screen.DayStatistics.route,
                arguments = listOf(
                    navArgument("date") { type = NavType.StringType },
                    navArgument("mapVisible") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val dateString = backStackEntry.arguments?.getString("date")
                val mapVisible = backStackEntry.arguments?.getBoolean("mapVisible") ?: false
                val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val date = dateString?.let { formatter.parse(it) }
                if (date != null) {
                    DayStatisticsPage(DayStatisticsViewModel(locationsDAO),date, mapVisible)
                }

            }
        }*/
    }
}


@Composable
fun OsmMapView(locationsDAO: LocationsDAO, modifier: Modifier) {
    val context = LocalContext.current
    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

    val liveLocations = remember { mutableStateOf<List<Locations>>(listOf()) }

    LaunchedEffect(key1 = Unit) {
        locationsDAO.getAllLocations().collect { locations ->
            liveLocations.value = locations
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                isTilesScaledToDpi = true
                setMultiTouchControls(true)
                controller.setCenter(GeoPoint(52.5200, 13.4050))
                controller.setZoom(9.5)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            Log.d("Locations", "${liveLocations.value.size}")
            if (liveLocations.value.isNotEmpty()) {
                val polyline = Polyline(mapView).apply {
                    outlinePaint.color = android.graphics.Color.RED
                    outlinePaint.strokeWidth = 8f
                    setPoints(liveLocations.value.toGeoPoints())
                }
                mapView.overlays.add(polyline)
                mapView.controller.setCenter(liveLocations.value.first().toGeoPoint())

                mapView.invalidate()
            }
        },
        modifier = modifier

    )
}



