package com.griffith.maptrackerproject.Views

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.griffith.maptrackerproject.R
import com.griffith.maptrackerproject.Services.LocationService
import com.griffith.maptrackerproject.ui.theme.GreenPrimary
import org.osmdroid.views.MapView

class LocationServiceButton : ComponentActivity() {
    lateinit var locationService: LocationService
    //Starting an pausing Locations recording on device
    private var isBound = false

    var locationsTrackingActive: MutableState<Boolean> = mutableStateOf(false)

    //Connecting to Location Service
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            isBound = true
            if(locationsTrackingActive.value) {
                locationService.startLocationUpdates()
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request Location Permissions if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            //Always starts recording location on start must be changed !!
            startLocationService()
        }
        setContent {
            ActivateServicePage(locationsTrackingActive = locationsTrackingActive, locationService = locationService)
        }
    }

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
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }
}

@Composable
fun ActivateServicePage(locationsTrackingActive: MutableState<Boolean>, locationService: LocationService){
    val context = LocalContext.current
    val mapIntent = Intent(context, MapView::class.java)
    val historyIntent = Intent(context, History::class.java)
    val locationServiceIntent = Intent(context, LocationService::class.java)

    BottomBar(context = LocalContext.current, mapIntent = mapIntent, historyIntent = historyIntent, locationServiceIntent = locationServiceIntent) {
        ActivateServiceButton(locationsTrackingActive, locationService)
    }
}

@Composable
fun ActivateServiceButton(locationsTrackingActive: MutableState<Boolean>, locationService: LocationService) {
    val context = LocalContext.current
    TextButton(
        onClick = {
            // Activate map tracking or not
            locationsTrackingActive.value = !locationsTrackingActive.value
            if (locationsTrackingActive.value) {
                locationService.startLocationUpdates()
                Toast.makeText(context, "Tracking is activated", Toast.LENGTH_SHORT).show()
            } else {
                locationService.stopLocationUpdates()
                Toast.makeText(context, "Tracking is deactivated", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier
            .background(GreenPrimary)
    ) {
        val conId = if (locationsTrackingActive.value) {
            R.drawable.pause_circle
        } else {
            R.drawable.play_circle_24
        }
        Icon(
            painter = painterResource(id = conId),
            contentDescription = "Tracking on off",
            Modifier.background(GreenPrimary),
            Color.White,

        )
    }
}