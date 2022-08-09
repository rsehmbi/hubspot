package com.example.hubspot.security.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
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
import com.example.hubspot.security.models.Speech
import com.example.hubspot.security.services.PushNotificationService
import com.example.hubspot.security.services.SafeLocationService
import com.example.hubspot.security.services.SilentButtonReceiver
import com.example.hubspot.security.viewModel.SecurityViewModel
import com.example.hubspot.services.LocationService
import com.example.hubspot.services.LocationService.LocationCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
 * pinging a friend the current user's location. Also handles toggle for the silent button service
 * of speech-to-text.
 *
 * Various code adapted from:
 * https://medium.com/@mendhie/send-device-to-device-push-notifications-without-server-side-code-238611c143
 * https://developer.android.com/codelabs/advanced-android-kotlin-training-notifications-fcm#4
 */
class SecurityFragment : Fragment() {
    private lateinit var securityViewModel: SecurityViewModel
    private lateinit var locationServicesButton: Button
    private lateinit var safeLocationService: SafeLocationService
    private lateinit var emergencySilentSwitch: Switch
    private lateinit var pingLocationSwitch: Switch
    private lateinit var speechToTextSwitch: Switch
    private lateinit var silentButtonReceiver: SilentButtonReceiver
    private lateinit var notificationManager: NotificationManager
    private lateinit var savedSpeechTextViewButton: TextView
    private lateinit var speechObject: Speech
    private var friendsList = ArrayList<User>()
    private var pingLocationIsOn = false
    private var emergencySilentSystemIsOn = false
    private var speechToTextIsOn = false
    private var isBind = false
    private val callPermissionRequestCode = 119
    private val callPermissionToggleRequestCode = 99
    private val notifyId = 1
    private var speechToTextRequestCode = 55
    private val bindStatusKey = "bind_status_key"
    private var initialLocationServicesButtonText = ""


    companion object CompanionObject {
        lateinit var securityViewModel: SecurityViewModel
    }

// Lifecycle methods ------------------------------------------------------------------------

    /**
     * Used for getting recognizer intent extras from the speech-to-text system
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == speechToTextRequestCode && resultCode == RESULT_OK) {
            val extras = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
            val speech = extras[0]

            // Save speech to DB
            saveSpeechToDataBase(speech)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
        initializeSilentButtonReceiver()
        initializeSafeLocationService()
        initializePingLocationService()
        initializeFriendsList()
        initializeObserveSpeechObject()
        getSpeechChangesFromFirebase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_security, container, false)
        setSavedInstanceVariables(savedInstanceState)
        initializeContinuousLocationServicesButtons(view)
        initializeSilentEmergencyButton(view)
        initializeSilentPingLocationButton(view)
        initializeSpeechToTextButton(view)
        initializeSavedSpeechTextViewButton(view)
        setLocationTextView(view)
        handleLocationUpdates(view)
        handleSilentButtonPresses()
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUpSystems()
    }

    /**
     * Checks call permissions at runtime due to the nature of android call permissions
     */
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
        outState.putString("securityButtonText", locationServicesButton.text.toString())
    }

// Private methods --------------------------------------------------------------------------

    /**
     * Cleans up broadcasts
     */
    private fun cleanUpSystems() {
        // Clean up emergency services
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(silentButtonReceiver)
        emergencySilentSystemIsOn = false
        securityViewModel.silentButtonPressed.value = false

        // Clean up ping location notification services
        closePingLocationNotification()
        pingLocationIsOn = false
        securityViewModel.silentButtonPressed.value = false
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(silentButtonReceiver)

        // Clean up speech-to-text services
        speechToTextIsOn = false
        securityViewModel.silentButtonPressed.value = false
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(silentButtonReceiver)
    }

    /**
     * Handles notification closes
     */
    private fun closePingLocationNotification() {
        if(this::notificationManager.isInitialized) {
            notificationManager.cancel(notifyId)
        }
    }

    /**
     * Sets up an event listener for a user's speech attribute in firebase realtime database and
     * sets the view model speech to the changed user speech attribute.
     */
    private fun getSpeechChangesFromFirebase() {
        val currentUser = Auth.getCurrentUser()!!
        val currentUserId = currentUser.id
        val path = "/Users/$currentUserId/speech"
        val firebaseInstance = FirebaseDatabase.getInstance()
        val firebaseRef = firebaseInstance.getReference(path)

        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val speechObject = dataSnapshot.getValue(Speech::class.java) as Speech
                    securityViewModel.speech.value = speechObject
                } catch (exception: Exception) {
                    println("Error: $exception")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

    }

    /**
     * Checks runtime call permissions and starts the call activity if permissions are granted.
     */
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

    /**
     * Handles the a security subsystem activation associtated with a specific key event.
     */
    private fun handleKeyEvent(): Boolean {
        securityViewModel.silentButtonPressed.value = false
        securityViewModel.keyEventButtonAction.value = null
        securityViewModel.keyEventButtonKeyCode.value = null
        val buttonPressType = securityViewModel.buttonPressType.value
        securityViewModel.buttonPressType.value = null

        if (buttonPressType == "emergencyButton") {
            handleEmergencyServicesSilentButtonPress()
            return true
        } else if (buttonPressType == "speechToTextButton") {
            launchSpeechToText()
            return true
        } else if (buttonPressType == "pingLocationButton") {
            sendLocationPushNotification()
            return true
        }
        return false
    }

    /**
     * Observes when to get a new location, gets the user's location, and adds it the User's
     * location attribute in firebase realtime database.
     */
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

    /**
     * Observes when a silent button is pressed and calls [handleKeyEvent]
     */
    private fun handleSilentButtonPresses() {
        securityViewModel.silentButtonPressed.observe(viewLifecycleOwner) {
            if (it == true) {
                handleKeyEvent()
            }
        }
    }

    /**
     * Starts the continuous location service.
     */
    private fun initializeContinuousLocationServicesButtons(view: View) {
        // Continuous Location Services Button
        locationServicesButton = view.findViewById(R.id.location_services_button)

        if (initialLocationServicesButtonText != "") {
            locationServicesButton.text = initialLocationServicesButtonText
        } else {
            locationServicesButton.text = "TURN ON"
        }
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
            } else {
                securityViewModel.locationServiceSystemActivated.value = false
                locationServicesButton.text = "TURN ON"
            }
        }
    }

    /**
     * Gets the user's friend list and subscribes to their push notifications.
     */
    private fun initializeFriendsList() {
        securityViewModel.friendsList.value = MainActivity.friendsList
        friendsList = securityViewModel.friendsList.value!!
        friendsList.forEach {
            subscribeToFriendTopicPushNotifications(it.id!!)
        }
    }

    /**
     * Observes the speech object in the [securityViewModel] and sets the change to the view model's
     * speech object
     */
    private fun initializeObserveSpeechObject() {
        securityViewModel.speech.observe(this){
            speechObject = it
        }
    }

    /**
     * Starts the ping location push notification service
     */
    private fun initializePingLocationService() {
        notificationManager = requireActivity().getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Starts the safe location service
     */
    private fun initializeSafeLocationService() {
        safeLocationService = SafeLocationService()
    }

    /**
     * Sets an on click listener for the speech text button view an handles starting the
     * [SpeechActivity]
     */
    private fun initializeSavedSpeechTextViewButton(view: View) {
        savedSpeechTextViewButton = view.findViewById(R.id.saved_speech_button)
        savedSpeechTextViewButton.setOnClickListener {
                if (this::speechObject.isInitialized) {
                    val displaySpeechIntent = Intent(requireActivity(), SpeechActivity::class.java)
                        .putExtra("time", speechObject.time)
                        .putExtra("speech", speechObject.speech)
                    startActivity(displaySpeechIntent)
                } else {
                    Toast.makeText(requireContext(), "User has not saved any speech yet.",
                        Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun initializeSilentButtonReceiver() {
        silentButtonReceiver = SilentButtonReceiver()
    }

    /**
     * Determines what happens when the ping location switch is checked (register receiver) or
     * unchecked (unregister receiver)
     */
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
                securityViewModel.silentButtonPressed.value = false
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(silentButtonReceiver)
            }
        })
    }

    /**
     * Determines what happens when the emergency silent switch is checked (register receiver) or
     * unchecked (unregister receiver)
     */
    private fun initializeSilentEmergencyButton(view: View) {
        emergencySilentSwitch = view.findViewById(R.id.emergency_silent_button_switch)
        emergencySilentSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener
        { buttonView, isChecked ->
            if (isChecked) {
                securityViewModel.emergencyServicesToggle.value = true
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
                securityViewModel.emergencyServicesToggle.value = false
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(silentButtonReceiver)
                emergencySilentSystemIsOn = false
                securityViewModel.silentButtonPressed.value = false
            }
        })
    }

    /**
     * Determines what happens when the speech to text switch is checked (register receiver) or
     * unchecked (unregister receiver)
     */
    private fun initializeSpeechToTextButton(view: View) {
        speechToTextSwitch = view.findViewById(R.id.speech_to_text_switch)
        speechToTextSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener
        { buttonView, isChecked ->
            if (isChecked) {
                speechToTextIsOn = true
                LocalBroadcastManager.getInstance(requireContext()).registerReceiver(silentButtonReceiver,
                    IntentFilter("silentButtonPressed")
                )
            } else {
                speechToTextIsOn = false
                securityViewModel.silentButtonPressed.value = false
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(silentButtonReceiver)
            }
        })
    }

    /**
     * Intializes the security view model and campanion object
     */
    private fun initializeViewModel() {
        securityViewModel = ViewModelProvider(requireActivity())[SecurityViewModel::class.java]
        CompanionObject.securityViewModel = securityViewModel

    }

    /**
     * Launches the speech to text system and allows the user to install Google Quick Search Box if
     * they are missing it from their device.
     */
    private fun launchSpeechToText() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the situation or possible assailant")
            startActivityForResult(intent, speechToTextRequestCode)
        }
        catch(ActivityNotFoundException: Exception) {
            val appPackageName = "com.google.android.googlequicksearchbox"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (exception: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

    /**
     * Takes the text speech and saves it the firebase realtime database under the user's speech
     * attribute
     */
    private fun saveSpeechToDataBase(speech: String) {
        val currentUser = Auth.getCurrentUser()!!
        val currentUserUid = currentUser.id
        val path = "Users/$currentUserUid/speech"
        try {
            // Get speech reference and save to DB
            val firebaseDatabase = FirebaseDatabase.getInstance()
            val userSpeechReference = firebaseDatabase.getReference(path)
            val speechObject = Speech(System.currentTimeMillis(), speech)
            userSpeechReference.setValue(speechObject)
        } catch (er: Exception) {
            println(er.toString())
        }
    }

    /**
     * Sets variables that were saved when fragment was destroyed
     */
    private fun setSavedInstanceVariables(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val savedSecurityButtonText = savedInstanceState!!.getString("securityButtonText")
            if (savedSecurityButtonText != null) {
                initialLocationServicesButtonText = savedSecurityButtonText
            }
        }
    }

    /**
     * Upon receiving a location, will create and send a push notification containing the user's
     * location and name.
     */
    private fun sendLocationPushNotification() {
        LocationService.getCurrentLocation(requireActivity(),
            object : LocationCallback { // Creates a callback for handling resulting location data
                override fun onCallback(result: Location?) {
                    val currentLocationString = "Latitude: ${result?.latitude}, Longitude: " +
                            "${result?.longitude}"
                    // send location data in push notification
                    val currentUser = Auth.getCurrentUser()!!
                    val userName = currentUser.displayName
                    val topic = currentUser.id
                    val notification = JSONObject()
                    val notificationBody = JSONObject()
                    try {
                        notificationBody.put("title", "EMERGENCY LOCATION ALERT!!!")
                        notificationBody.put("message",
                            "Your friend $userName's location: " +
                                    "$currentLocationString")
                        notificationBody.put("lat", "${result?.latitude}")
                        notificationBody.put("long", "${result?.longitude}")
                        notificationBody.put("name","$userName")
                        notification.put("to", "/topics/$topic")
                        notification.put("data", notificationBody)

                        val pushNotificationService = PushNotificationService()
                        pushNotificationService.sendNotification(notification, requireActivity())
                        Toast.makeText(requireActivity(), "Location broadcasted to friends.", Toast.LENGTH_SHORT)
                    } catch (e: JSONException) {
                        println("onCreate: " + e.message)
                    }

                }
            }
        )
    }

    /**
     * Sets the text view that showcases the user's current location
     */
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

    /**
     * Allows a user to subscribe to a friend topic so that they may receive push notifications
     * from that friend.
     */
    private fun subscribeToFriendTopicPushNotifications(friendTopic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(friendTopic)
            .addOnCompleteListener { task ->
                println("Subscribing to topic: $friendTopic")
                if (!task.isSuccessful) {
                    println("Failed to subscribe to topic: $friendTopic")
                }
            }
    }
}