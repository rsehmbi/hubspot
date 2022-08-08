package com.example.hubspot.security.ui

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.hubspot.MainActivity
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.models.User
import com.example.hubspot.models.UserLocation
import com.example.hubspot.security.services.PushNotificationService
import com.example.hubspot.security.services.SafeLocationService
import com.example.hubspot.security.services.SilentButtonReceiver
import com.example.hubspot.security.viewModel.SecurityViewModel
import com.example.hubspot.services.LocationService
import com.example.hubspot.services.LocationService.LocationCallback
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*


/**
 * SecurityFragment is a [Fragment] that handles user security features on the HubSpot application.
 * It allows the user to turn on/off continuous location capturing. It also allows users to toggle
 * whether they would like to enable the emergency services alert button presses. Contains a
 * companion object that holds a reference to the SecurityFragment's [SecurityViewModel] for
 * referencing locational and [KeyEvent] data. Handles toggle for the silent button service of
 * pinging a friend the current user's location.
 *
 * Various code adapted from:
 * https://medium.com/@mendhie/send-device-to-device-push-notifications-without-server-side-code-238611c143
 * https://developer.android.com/codelabs/advanced-android-kotlin-training-notifications-fcm#4
 */
class SecurityFragment : Fragment() {
    private lateinit var securityViewModel: SecurityViewModel
    private lateinit var locationServicesButton: Button
    private lateinit var safeLocationService: SafeLocationService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var emergencySilentSwitch: Switch
    private lateinit var pingLocationSwitch: Switch
    private lateinit var silentButtonReceiver: SilentButtonReceiver
    private lateinit var notificationManager: NotificationManager
    private var pingLocationIsOn = false
    private var emergencySilentSystemIsOn = false
    private val bindStatusKey = "bind_status_key"
    private var isBind = false
    private var downButtonPressedCount = 0
    private var downButtonPressedCountDown = 5
    private val callPermissionRequestCode = 119
    private val callPermissionToggleRequestCode = 99
    private val notifyId = 1
    private var friendsList = ArrayList<User>()


    companion object CompanionObject {
        lateinit var securityViewModel: SecurityViewModel
    }

// Lifecycle methods ------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_security, container, false)
        initializeContinuousLocationServicesButtons(view)
        initializeSilentEmergencyButton(view)
        initializeSilentPingLocationButton(view)
        setLocationTextView(view)
        handleLocationUpdates(view)
        handleSilentButtonPresses()
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
        initializeSafeLocationService()
        initializePingLocationService()
        initializeSharedPreferences()
        initializeFriendsList()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            callPermissionRequestCode -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(requireContext(),"Emergency services system activated. " +
                            "Calling '911'.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(),"Call permission denied.",
                        Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(bindStatusKey, isBind)
    }

// Private methods --------------------------------------------------------------------------

    private fun closePingLocationNotification() {
        if(this::notificationManager.isInitialized) {
            notificationManager.cancel(notifyId)
        }
    }

    private fun handleEmergencyServicesSilentButtonPress() {
        val intent = Intent(Intent.ACTION_CALL) // This intent dials the number automatically
        intent.data = Uri.parse("tel: 119") // Obviously not using 911 for this project app
        if (ContextCompat.checkSelfPermission( // Checks call permissions at runtime
                requireActivity(),
                Manifest.permission.CALL_PHONE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.CALL_PHONE),
                callPermissionRequestCode
            )
        } else { // User already gave permission
            try {
                startActivity(intent)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleKeyEvent(): Boolean {
        val action = securityViewModel.keyEventButtonAction.value
        val keyCode = securityViewModel.keyEventButtonKeyCode.value
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (emergencySilentSystemIsOn) {
                    downButtonPressedCount++
                    downButtonPressedCountDown--
                    if (downButtonPressedCountDown > 0) {
                        Toast.makeText(requireContext(),
                            "Press Volume Down button $downButtonPressedCountDown more times" +
                                    " to call emergency services", Toast.LENGTH_SHORT).show()
                    }
                    if (downButtonPressedCount == 5) {
                        handleEmergencyServicesSilentButtonPress()
                        downButtonPressedCount = 0
                    }

                }
                return true
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (pingLocationIsOn) {
                    sendLocationPushNotification()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                // TODO: enable speech to text of attacker
            }
        }
        return false
    }

    private fun handleLocationUpdates(view: View) {
        securityViewModel.getLocationNow.observe(viewLifecycleOwner) {
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
                    val newUserLocation = UserLocation(dateTime!!, location)
                    val newPostRef =
                        userLocationsReference.push()  // Creates a chronological UID for each list item
                    newPostRef.setValue(newUserLocation)
                } catch (er: Exception) {
                    println(er.toString())
                }
                securityViewModel.getLocationNow.value = false
            }
        }
    }

    private fun handleSilentButtonPresses() {
        securityViewModel.silentButtonPressed.observe(viewLifecycleOwner) {
            if (it == true) {
                handleKeyEvent()
            }
        }
    }

    private fun initializeContinuousLocationServicesButtons(view: View) {
        // Continuous Location Services Button
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

    private fun initializeFriendsList() {
        securityViewModel.friendsList.value = MainActivity.friendsList
        friendsList = securityViewModel.friendsList.value!!
        friendsList.forEach {
            subscribeToFriendTopicPushNotifications(it.id!!)
        }
    }

    private fun initializePingLocationService() {
        notificationManager = requireActivity().getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun initializeSafeLocationService() {
        safeLocationService = SafeLocationService()
    }

    private fun initializeSilentPingLocationButton(view: View) {
        pingLocationSwitch = view.findViewById(R.id.ping_location_button_switch)
        pingLocationSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener
        { buttonView, isChecked ->
            if (isChecked) {
                pingLocationIsOn = true
                LocalBroadcastManager.getInstance(requireContext()).registerReceiver(silentButtonReceiver,
                    IntentFilter("silentButtonPressed")
                )
            } else {
                closePingLocationNotification()
                pingLocationIsOn = false
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(silentButtonReceiver)
                securityViewModel.silentButtonPressed.value = false
            }
        })
    }

    private fun initializeSharedPreferences() {
        sharedPreferences = requireActivity()
            .getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
    }

    private fun initializeSilentEmergencyButton(view: View) {
        silentButtonReceiver = SilentButtonReceiver()
        emergencySilentSwitch = view.findViewById(R.id.emergency_silent_button_switch)
        emergencySilentSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener
        { buttonView, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission( // Checks call permissions at runtime
                        requireActivity(),
                        Manifest.permission.CALL_PHONE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(Manifest.permission.CALL_PHONE),
                        callPermissionToggleRequestCode
                    )
                } else { // User already gave permission
                    println("Permissions already granted")
                    emergencySilentSystemIsOn = true
                }
                LocalBroadcastManager.getInstance(requireContext()).registerReceiver(silentButtonReceiver,
                    IntentFilter("silentButtonPressed")
                )
                emergencySilentSystemIsOn = true
            } else {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(silentButtonReceiver)
                emergencySilentSystemIsOn = false
                securityViewModel.silentButtonPressed.value = false
            }
        })
    }

    private fun subscribeToFriendTopicPushNotifications(friendTopic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(friendTopic)
            .addOnCompleteListener { task ->
                println("Subscribing to topic: $friendTopic")
                if (!task.isSuccessful) {
                    println("Failed to subscribe to topic: $friendTopic")
                }
            }
    }

    private fun initializeViewModel() {
        securityViewModel = ViewModelProvider(requireActivity())[SecurityViewModel::class.java]
        CompanionObject.securityViewModel = securityViewModel

    }

    private fun sendLocationPushNotification() {
        LocationService.getCurrentLocation(requireActivity(),
            object : LocationCallback { // Creates a callback for handling resulting location data
                override fun onCallback(result: Location?) {
                    val currentLocationString = "Latitude: ${result?.latitude}, Longitude: " +
                            "${result?.longitude}"
                    // send location data in push notification
                    val currentUser = Auth.getCurrentUser()!!
                    val topic = currentUser.id
                    val notification = JSONObject()
                    val notificationBody = JSONObject()
                    try {
                        notificationBody.put("title", "EMERGENCY LOCATION ALERT!!!")
                        notificationBody.put("message",
                            "Your friend ${currentUser.displayName} has pinged you their " +
                                    "location. $currentLocationString")
                        notificationBody.put("lat", "${result?.latitude}")
                        notificationBody.put("long", "${result?.longitude}")
                        notification.put("to", "/topics/$topic")
                        notification.put("data", notificationBody)
                    } catch (e: JSONException) {
                        println("onCreate: " + e.message)
                    }
                    val pushNotificationService = PushNotificationService()
                    pushNotificationService.sendNotification(notification, requireActivity())
                }
            }
        )
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