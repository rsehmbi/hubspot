package com.example.hubspot.security.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hubspot.R
import com.example.hubspot.databinding.ActivityPushNotificationMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * This activity shows a map of a user's friends geographical location as well as their friend's
 * coordinates and friend's name.
 */
class PushNotificationMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPushNotificationMapsBinding
    private var latString = ""
    private var longString = ""
    private var friendsName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPushNotificationMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up views on map activity
        getExtras()
        initializeTextView()
        initializeBackButton()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        var lat = 0.0
        if (!latString.isNullOrBlank()) {
            lat = latString.toDouble()
        }
        var long = 0.0
        if (!longString.isNullOrBlank()) {
            long = longString.toDouble()
        }
        // Add a marker to friend's location and move camera
        val friendsLocation = LatLng(lat, long)
        mMap.addMarker(MarkerOptions().position(friendsLocation).title("Friends Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendsLocation, 18f))
    }

    private fun initializeBackButton() {
        val backButton = findViewById<Button>(R.id.map_cancel_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun initializeTextView() {
        val mapTextView = findViewById<TextView>(R.id.notification_text_view)
        mapTextView.text = "Friend: $friendsName\nLatitude: $latString, Longitude: $longString"
    }

    private fun getExtras() {
        latString = intent.getStringExtra("lat")!!
        longString = intent.getStringExtra("long")!!
        friendsName = intent.getStringExtra("name")!!
    }
}