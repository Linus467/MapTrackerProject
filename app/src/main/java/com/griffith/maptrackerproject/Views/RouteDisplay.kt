package com.griffith.maptrackerproject.Views

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import com.griffith.maptrackerproject.R
import com.griffith.maptrackerproject.Services.LocationService
import com.griffith.maptrackerproject.ui.theme.Screen
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.sql.Date
import javax.inject.Inject


var locationsTrackingActive: Boolean = false
@AndroidEntryPoint
class RouteDisplay : ComponentActivity() {

    @Inject
    lateinit var locationsDAO: LocationsDAO
    private lateinit var locationService: LocationService
    //Starting an pausing Locations recording on device
    private var isBound = false

    private var locationsDisplayed: List<Locations> = mutableListOf()


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DisplayRouteMain(sampleLiveLocationsPreview())
        }
        Intent(this,LocationService::class.java).also{
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        startLocationService()
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
fun DisplayRouteMain(liveLocations: Map<Date,GeoPoint>){
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(painterResource(id = R.drawable.baseline_map_24), contentDescription = "Map View") },
                    selected = navController.currentDestination?.route == Screen.MapView.route,
                    onClick = { navController.navigate(Screen.MapView.route) }
                )

                BottomNavigationItem(
                    icon = {
                        val conId = if (locationsTrackingActive){
                            R.drawable.pause_circle
                        }else{
                            R.drawable.play_circle_24
                        }
                        Icon(painterResource(id = conId), contentDescription = "Continue Tracking")
                           },
                    selected = false,
                    onClick = {
                        locationsTrackingActive = !locationsTrackingActive
                    }
                )

                BottomNavigationItem(
                    icon = { Icon(painterResource(id = R.drawable.baseline_history_24), contentDescription = "Map View") },
                    selected = navController.currentDestination?.route == Screen.History.route,
                    onClick = { navController.navigate(Screen.History.route) }
                )
            }
        }
    ) {innerPadding->
        NavHost(navController, startDestination = Screen.MapView.route, Modifier.padding(innerPadding)) {
            composable(Screen.MapView.route) { OsmMapView(liveLocations) }
            composable(Screen.History.route) { HistoryScreen() }
        }
    }
}


@Composable
fun OsmMapView(liveLocations: Map<Date,GeoPoint>) {
    val context = LocalContext.current
    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setCenter(GeoPoint(52.5200, 13.4050))
                controller.setZoom(9.5)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            Log.d("Locations","${liveLocations.size}")
            if (liveLocations.isNotEmpty()) {
                val polyline = Polyline(mapView).apply {
                    outlinePaint.color = android.graphics.Color.RED
                    outlinePaint.strokeWidth = 8f
                    setPoints(liveLocations.values.toList())
                }

                mapView.overlays.add(polyline)

                mapView.controller.setCenter(liveLocations.values.last())

                mapView.invalidate()
            }
        }
    )
}


//@Composable
//fun HistoryButton() {
//    var context = LocalContext.current
//    Button(modifier = Modifier.height(40.dp).background(Color.Black),
//        onClick = {
//        //switching to History view
//        val intent = Intent(context, History::class.java)
//        context.startActivity(intent)
//    }) {
//        Icon(
//            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_history_24),
//            contentDescription = "History"
//        )
//    }
//}

