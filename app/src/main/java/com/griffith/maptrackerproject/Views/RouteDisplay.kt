package com.griffith.maptrackerproject.Views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Location
import android.os.Bundle
import android.location.LocationListener
import android.location.LocationManager
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.griffith.maptrackerproject.DB.AppDatabase
import com.griffith.maptrackerproject.DB.Locations
import com.griffith.maptrackerproject.DB.LocationsDAO
import com.griffith.maptrackerproject.R
import com.griffith.maptrackerproject.Views.History
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.sql.Date


class RouteDisplay : ComponentActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private val locationUpdateInterval: Long = 5000 // 5 seconds
    private val locationUpdateDistance: Float = 10f // 10 meters

    private val liveLocations = mutableMapOf<Date,GeoPoint>()
    //TODO install HILT for global database instance distribution
    /*val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java,
        "Locations_Data"
    ).build()

    val locationsDAO = db.locationDAO()*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box {
                OsmMapView(liveLocations)
                HistoryButton()
            }
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdateInterval, locationUpdateDistance, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        val time = Calendar.getInstance().time
        //saveGeoPoint(geoPoint, time as Date)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    fun saveGeoPoint(geoPoint: GeoPoint, date: Date ){
        val locations = Locations(geoPoint.latitude,geoPoint.longitude, geoPoint.altitude, date);
        CoroutineScope(Dispatchers.IO).launch {
            //locationsDAO.insert(locations)
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
            if (liveLocations.isNotEmpty()) {
                mapView.controller.setCenter(liveLocations.values.last())
            }
        }
    )
}

@Composable
fun HistoryButton() {
    var context = LocalContext.current
    Button(onClick = {
        //switching to History view
        val intent = Intent(context, History::class.java)
        context.startActivity(intent)
    }) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_history_24),
            contentDescription = "History"
        )
    }
}

