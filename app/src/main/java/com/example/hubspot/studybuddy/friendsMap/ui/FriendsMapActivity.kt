package com.example.hubspot.studybuddy.friendsMap.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.studybuddy.friendsMap.services.FriendsMapService
import com.example.hubspot.services.LocationService
import com.example.hubspot.studybuddy.friendsMap.services.FriendLocation
import com.example.hubspot.studybuddy.friendsMap.viewModel.FriendsMapViewModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FriendsMapActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
    private var map: GoogleMap? = null
    private var  markerOptions: MarkerOptions? = null
    private var cameraPosition: CameraPosition? = null
    // A default location (Sydney, Australia)
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private lateinit var mapViewModel: FriendsMapViewModel
    private var friendsLocations: ArrayList<FriendLocation> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_friends_maps)
        // Build the map.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        mapViewModel = ViewModelProvider(this).get(FriendsMapViewModel::class.java)
        mapViewModel.friendsLocations.observe(this) {
            friendsLocations = it
            updateFriendsLocationsMarkers()
        }
        val intentTrackingService = Intent(this, FriendsMapService::class.java)
        applicationContext.startService(intentTrackingService)
        applicationContext.bindService(intentTrackingService, mapViewModel, Context.BIND_AUTO_CREATE)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        markerOptions = MarkerOptions()

        // Prompt the user for permission.
        getCurrentLocationPermission()
        // Turn on the My Location layer and the related control on the map.
        addCurrentLocationButtons()
        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
        // put markers at the friends' locations
        updateFriendsLocationsMarkers()
    }

    private fun getDeviceLocation() {
        if (locationPermissionGranted) {
            try {
                LocationService.getCurrentLocation(this, object : LocationService.LocationCallback {
                    override fun onCallback(result: Location?) {
                        lastKnownLocation = result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        } else {
                            map?.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                            )
                            map?.uiSettings?.isMyLocationButtonEnabled = false
                        }
                    }
                })
            } catch (e: SecurityException) {
                println("debug: $e")
            }
        }
    }

    private fun getCurrentLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        addCurrentLocationButtons()
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun addCurrentLocationButtons() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getCurrentLocationPermission()
            }
        } catch (e: SecurityException) {
            println("debug: $e")
        }
    }

    private fun updateFriendsLocationsMarkers(){
        map?.clear()
        for (friend in friendsLocations) {
            if (friend.location != null) {
                map?.addMarker(MarkerOptions()
                    .position(friend.location!!)
                    .title(friend.friendId.toString())
                    .snippet("Join me!"))
            }
        }
    }
}