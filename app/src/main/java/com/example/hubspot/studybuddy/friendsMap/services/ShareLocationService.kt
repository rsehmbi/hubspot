package com.example.hubspot.studybuddy.friendsMap.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.example.hubspot.auth.Auth
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase

class ShareLocationService: Service() {
    // minimum time interval between location updates in milliseconds
    val TIME_INTERVAL = 1000L // set to 300000L = 5 minutes in production
    private val dbReference =
        FirebaseDatabase.getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference
    private val currUserId = Auth.getCurrentUser()!!.id
    private lateinit var locationManager: LocationManager

    // This is called as soon as the services starts
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
            // set a GPS location update listener
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                TIME_INTERVAL, 10f, locationListenerGPS
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    private var locationListenerGPS: LocationListener = LocationListener { location ->
        val latitude = location.latitude
        val longitude = location.longitude
        val latLng = LatLng(latitude, longitude)
        dbReference.child("Users/${currUserId}/currentLocation").setValue(latLng)
    }
}