package com.griffith.maptrackerproject.Services

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.griffith.maptrackerproject.DB.Locations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.sql.Date

class LocationService : Service(),LocationListener {
    private lateinit var locationManager: LocationManager
    private val locationUpdateInterval: Long = 5000 // 5 seconds
    private val locationUpdateDistance: Float = 10f // 10 meters

    var locationsUpdatesActive: Boolean = false

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdateInterval, locationUpdateDistance, this)
            locationsUpdatesActive = true
            Log.d("LocationUpdates", "active")
        }
    }

    fun stopLocationUpdates(){
        locationManager.removeUpdates(this)
        locationsUpdatesActive
        Log.d("LocationUpdates", "paused")
    }
    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        val time = Calendar.getInstance().time
        saveGeoPoint(geoPoint, time as Date)
    }

    fun saveGeoPoint(geoPoint: GeoPoint, date: Date){
        val locations = Locations(geoPoint.latitude,geoPoint.longitude, geoPoint.altitude, date);
        CoroutineScope(Dispatchers.IO).launch {
            //locationsDAO.insert(locations)
        }
    }

    private val binder = LocalBinder();

    inner class LocalBinder : Binder(){
        fun getService(): LocationService = this@LocationService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

}
