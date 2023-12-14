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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import com.griffith.maptrackerproject.DB.calculateAveragePosition
import com.griffith.maptrackerproject.DB.groupLocationsWithin30Seconds
import com.griffith.maptrackerproject.DB.toGeoPoint
import com.griffith.maptrackerproject.DB.toGeoPoints
import com.griffith.maptrackerproject.Interface.LocationUpdateController
import com.griffith.maptrackerproject.R
import com.griffith.maptrackerproject.Services.LocationService
import com.griffith.maptrackerproject.ui.theme.GreenPrimary
import dagger.hilt.android.AndroidEntryPoint
import getCurrentStartEndTime
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class RouteDisplay : ComponentActivity(), LocationUpdateController {

    //Using hilt to manage Data Access Objects
    @Inject
    lateinit var locationsDAO: LocationsDAO
    //Getting a location Service
    private lateinit var locationService: LocationService
    //Starting an pausing Locations recording on device
    private var isBound = false

    //Connecting to Location Service
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            isBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    //Helper methods to start Location Updates from Composable
    override fun startLocationUpdates() {
        if (isBound) {
            locationService.startLocationUpdates()
        }
    }

    //Helper methods to stop Location Updates from Composable
    override fun stopLocationUpdates() {
        if (isBound) {
            locationService.stopLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request Location Permissions if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            //Always starts recording location on start must be changed !!
            startLocationService()

            Intent(this,LocationService::class.java).also{
                bindService(it, connection, Context.BIND_AUTO_CREATE)
            }
        }

        setContent {
            DisplayRouteMain(locationsDAO,this,this)
        }

    }

    //When app the closed by android the connection to the Service is unbound
    override fun onDestroy() {
        super.onDestroy()
        //Disconnection from Location Service
        if(isBound){
            locationService.stopLocationUpdates()
            unbindService(connection)
            isBound = false
        }
    }
    private fun startLocationService(){
        val serviceIntent = Intent(this, LocationService::class.java)
        startService(serviceIntent)
    }
}

//Main Page with MapView
@Composable
fun DisplayRouteMain(
    locationsDAO: LocationsDAO,
    locationsUpdateController: LocationUpdateController,
    context: Context
    ) {

    var locationsTrackingActive by remember { mutableStateOf(false) }
    val mapIntent = Intent(context, RouteDisplay::class.java)
    val historyIntent = Intent(context, History::class.java)

    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth()
            .shadow(0.dp)
            .background(GreenPrimary),
        bottomBar = {
            BottomNavigation(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(45.dp)
                    .background(GreenPrimary)
                    .shadow(2.dp),
                backgroundColor = Color.Transparent

            ) {
                //Box that goes through the entire scaffold
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxWidth()
                        .background(GreenPrimary)
                ){
                    TextButton(
                        onClick = { context.startActivity(mapIntent) },
                        modifier = Modifier.background(GreenPrimary)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_map_24),
                            contentDescription = "Map View",
                            Modifier.background(GreenPrimary),
                            Color.White
                        )
                    }
                    TextButton(
                        onClick = {
                            //Activate map tracking or not
                            locationsTrackingActive = !locationsTrackingActive
                            if (locationsTrackingActive) {
                                Toast.makeText(context, "Tracking Started", Toast.LENGTH_SHORT).show()
                                locationsUpdateController.startLocationUpdates()
                            } else {
                                Toast.makeText(context, "Tracking Paused", Toast.LENGTH_SHORT).show()
                                locationsUpdateController.stopLocationUpdates()
                            }
                        },
                        modifier = Modifier.background(GreenPrimary)
                            .align(Alignment.Center)
                    ) {
                        val conId = if (locationsTrackingActive) {
                            R.drawable.pause_circle
                        } else {
                            R.drawable.play_circle_24
                        }
                        Icon(
                            painter = painterResource(id = conId),
                            contentDescription = "Tracking on off",
                            Modifier.background(GreenPrimary),
                            Color.White
                        )
                    }

                    //Go to the History activity
                    TextButton(onClick = {
                        context.startActivity(historyIntent)
                    },
                        modifier = Modifier.background(GreenPrimary)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_history_24),
                            contentDescription = "Map View",
                            Modifier.background(GreenPrimary),
                            Color.White
                        )
                    }
                }

            }
        } )
    { innerPadding->
        OsmMapView(locationsDAO, Modifier.padding(innerPadding ))
    }
}


//Shows the Map
@Composable
fun OsmMapView(locationsDAO: LocationsDAO, modifier: Modifier) {
    val context = LocalContext.current
    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Format for displaying time

    val liveLocations = remember { mutableStateOf<List<Locations>>(listOf()) }

    LaunchedEffect(key1 = Unit) {
        val (startOfDay, endOfDay) = getCurrentStartEndTime(Date())
        //get all Locations for day
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
                controller.setCenter(liveLocations.value.calculateAveragePosition())
                controller.setZoom(9.5)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            val polylineList = mutableListOf<Polyline>()

            liveLocations.value.ifNotEmpty { locations ->
                //create gaping between different path segments with grouping locations within 30 seconds
                for (filteredLocations in locations.groupLocationsWithin30Seconds()) {
                    val polyline = Polyline(mapView).apply {
                        outlinePaint.color = Color.Black.toArgb()
                        outlinePaint.strokeWidth = 8f
                        // Adding the locations from filteredLocations to the polyline
                        setPoints(filteredLocations.toGeoPoints())
                    }
                    polylineList.add(polyline)
                }
                // Adding all the polylines to the map
                for(polyline in polylineList){
                    mapView.overlays.add(polyline)
                }

                // Center the map on the last location
                mapView.controller.setCenter(locations.last().toGeoPoint())
            }
        },
        modifier = modifier

    )
}


