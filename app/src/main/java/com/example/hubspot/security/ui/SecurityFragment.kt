package com.example.hubspot.security.ui

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.models.UserLocation
import com.example.hubspot.security.services.SafeLocationService
import com.example.hubspot.security.viewModel.SecurityViewModel
import com.example.hubspot.services.LocationService
import com.example.hubspot.services.LocationService.LocationCallback
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.*


/**
 * SecurityFragment is a [Fragment] that handles user security features on the HubSpot application.
 * It allows the user to turn on/off continuous location capturing. Contains a companion object that
 * holds a reference to the SecurityFragment's [SecurityViewModel] for referencing locational data.
 * TODO: Add features as they are finished
 */
class SecurityFragment : Fragment() {
    private lateinit var securityViewModel: SecurityViewModel
    private lateinit var locationServicesButton: Button
    private lateinit var safeLocationService: SafeLocationService
    private lateinit var sharedPreferences: SharedPreferences

    companion object CompanionObject {
        lateinit var securityViewModel: SecurityViewModel
    }

// Lifecycle methods ------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_security, container, false)
        initializeSecurityButtons(view)
        setLocationTextView(view)
        handleLocationUpdates(view)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
        initializeSafeLocationService()
        initializeSharedPreferences()
    }

// Private methods --------------------------------------------------------------------------

    private fun initializeSharedPreferences() {
        sharedPreferences = requireActivity()
            .getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
    }
    private fun handleLocationUpdates(view: View) {
        securityViewModel.getLocationNow.observe(viewLifecycleOwner){
            if (securityViewModel.getLocationNow.value == true) {
                setLocationTextView(view)
                // Create path for database
                val currentUser = Auth.getCurrentUser()
                val currentUserUid = currentUser!!.id
                val path = "Users/$currentUserUid/Locations"
                try {
                    // Get values location values
                    val latitude = securityViewModel.latitude.value
                    val longitude = securityViewModel.longitude.value
                    val location = "$latitude,$longitude"
                    val dateTime = securityViewModel.lastLocationDateTime.value

                    // Get reference
                    val firebaseDatabase = FirebaseDatabase.getInstance()
                    val userLocationsReference = firebaseDatabase.getReference(path)

                    // Add new location to Locations list
                    val newUserLocation = UserLocation(dateTime!!,location)
                    val newPostRef = userLocationsReference.push()  // Creates a chronological UID for each list item
                    newPostRef.setValue(newUserLocation)
                    // TODO: add max locations
                }catch (er: Exception) {
                    println(er.toString())
                }
                // TODO: Broadcast to friends
                securityViewModel.getLocationNow.value = false
            }
        }
    }

    private fun initializeSafeLocationService() {
        safeLocationService = SafeLocationService()
    }

    private fun initializeSecurityButtons(view: View) {
        locationServicesButton = view.findViewById(R.id.location_services_button)

        val securityButtonText = sharedPreferences.getString("security_button_text", "TURN ON")
        locationServicesButton.text = securityButtonText
        locationServicesButton.setOnClickListener {
            if (locationServicesButton.text == "TURN ON") {
                securityViewModel.locationServiceSystemActivated.value = true
                // Activates the continuous location system
                safeLocationService.handleContinuousLocationSystemActivation(
                    requireActivity(),
                    viewLifecycleOwner,
                    securityViewModel,
                )
                locationServicesButton.text = "TURN OFF"
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("security_button_text", "TURN OFF")
                editor.apply()
            } else {
                securityViewModel.locationServiceSystemActivated.value = false
                locationServicesButton.text = "TURN ON"
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("security_button_text", "TURN ON")
                editor.apply()
            }
        }
    }

    private fun initializeViewModel() {
        securityViewModel = SecurityViewModel()
        CompanionObject.securityViewModel = securityViewModel

    }

    private fun setLocationTextView(view: View) {
        CoroutineScope(IO).launch {
            val locationTextView: TextView = view.findViewById(R.id.location_text_view)
            LocationService.getCurrentLocation(requireActivity(),
                object : LocationCallback { // Creates a callback for handling resulting location data
                    override fun onCallback(result: Location?) {
                        val currentLocationString = "Latitude: ${result?.latitude}, Longitude: " +
                                "${result?.longitude}"
                        CoroutineScope(Main).launch {
                            locationTextView.text = currentLocationString
                            securityViewModel.latitude.value = result!!.latitude
                            securityViewModel.longitude.value = result.longitude
                            securityViewModel.lastLocationDateTime.value =
                                Calendar.getInstance().timeInMillis
                        }
                    }
                }
            )
        }
    }
}