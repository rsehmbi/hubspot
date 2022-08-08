package com.example.hubspot.security.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.hubspot.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import  com.example.hubspot.databinding.ActivityPushNotificationMapsBinding

class PushNotificationMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPushNotificationMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPushNotificationMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initializeBackButton()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latString = intent.getStringExtra("lat")
        var lat = 0.0
        if (!latString.isNullOrBlank()) {
            lat = latString.toDouble()
        }
        val longString = intent.getStringExtra("long")
        var long = 0.0
        if (!longString.isNullOrBlank()) {
            long = longString.toDouble()
        }
        // Add a marker to friend's location and move camera
        val friendsLocation = LatLng(lat, long)
        mMap.addMarker(MarkerOptions().position(friendsLocation).title("Friends Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendsLocation, 17f))
    }

    private fun initializeBackButton() {
        val backButton = findViewById<Button>(R.id.map_cancel_button)
        backButton.setOnClickListener {
            finish()
        }
    }
}