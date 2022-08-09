package com.example.hubspot.studybuddy.friendsMap.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.example.hubspot.auth.Auth
import com.example.hubspot.studybuddy.StudyBuddyFragment.Companion.LAST_KNOWN_LOCATION_KEY
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase

/**
 * A service that monitors the user's GPS location and updates the DB
 * with the new location everytime the location changes.
 */
class ShareLocationService: Service() {
    // minimum time interval between location updates in milliseconds
    val TIME_INTERVAL = 1000L

    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference
    private val currUserId = Auth.getCurrentUser()!!.id
    private lateinit var locationManager: LocationManager

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Set a GPS location update listener
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                TIME_INTERVAL, 10f, locationListenerGPS
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // initialize the location with the last know location
        val lastKnownLocation = intent!!.getParcelableExtra<Location>(LAST_KNOWN_LOCATION_KEY)
        onLocationChanged(lastKnownLocation!!)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the location listener and ser the location values in DB to none
        locationManager.removeUpdates(locationListenerGPS)
        dbReference.child("Users/${currUserId}/currentLocation/latitude").setValue("none")
        dbReference.child("Users/${currUserId}/currentLocation/longitude").setValue("none")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    // Update the DB when the location is changed
    private fun onLocationChanged(location: Location){
        val latitude = location.latitude
        val longitude = location.longitude
        val latLng = LatLng(latitude, longitude)
        dbReference.child("Users/${currUserId}/currentLocation").setValue(latLng)
    }

    private var locationListenerGPS: LocationListener = LocationListener { location ->
        // A callback executed once the user's location changed
        onLocationChanged(location)
    }
}