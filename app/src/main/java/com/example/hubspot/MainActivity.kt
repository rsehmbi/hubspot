package com.example.hubspot

import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.hubspot.auth.Auth
import com.example.hubspot.databinding.ActivityMainBinding
import com.example.hubspot.friends.FriendsFragment
import com.example.hubspot.login.LoginActivity
import com.example.hubspot.models.User
import com.example.hubspot.profile.ProfileActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList
import com.example.hubspot.ratings.RatingsFragment
import com.example.hubspot.schedule.ScheduleFragment
import com.example.hubspot.schedule.ShowMySchedule
import com.example.hubspot.security.ui.SecurityFragment
import com.example.hubspot.studybuddy.StudyBuddyFragment
import com.example.hubspot.utils.Util
import com.google.android.material.navigation.NavigationView

/** The main activity for the application, which displays the different
 *  fragments for each feature. A side navigation drawer is used for
 *  navigation between the fragments and to logout or check user profiles.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding
    private var upStartPressTime = 0L
    private var upEndPressTime = 0L
    private var downStartPressTime = 0L
    private var downEndPressTime = 0L
    private var downButtonPressedCount = 0
    private var downButtonPressedCountDown = 5

    companion object {
        var friendsList = ArrayList<User>()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check internet connection
        if(!NetworkUtil.isOnline(this)) {
            Toast.makeText(this, "Please connect to the Internet", Toast.LENGTH_LONG).show()
            finish()
        }

        // Check if user is currently logged in, go to login screen if not
        if (Auth.getCurrentUser() == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Util.checkCameraAndStoragePermissions(this)

        replaceActionBarWithToolBar()

        // Setup drawer view
        val drawer = binding.nvView
        setupDrawerContent(drawer, savedInstanceState)

        // Setup app bar title to current fragment name
        setAppBarTitleToFragmentName(drawer)

        // Setup animated hamburger button
        setupHamburgerButton()

        // Setup toggle to display hamburger icon with animation
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()

        // Tie DrawerLayout events to the ActionBarToggle
        binding.drawerLayout.addDrawerListener(drawerToggle)
        
        // Setup friends list for quick push notification subscription in the security fragment
        initializeFriendsList()

        // Register a listener for the internet connection
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // Lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            Toast.makeText(this@MainActivity, "Please connect to the Internet", Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    private fun setupHamburgerButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            findViewById(R.id.toolbar),
            R.string.drawer_open,
            R.string.drawer_close
        )
    }

    private fun setAppBarTitleToFragmentName(drawer: NavigationView) {
        val checkedItem = drawer.checkedItem
        if (checkedItem != null) {
            title = checkedItem.title
        }
    }

    private fun replaceActionBarWithToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDrawerContent(navigationView: NavigationView, savedInstanceState: Bundle?) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }

        if (savedInstanceState == null) {
            // Use schedule fragment as default fragment on first app load
            val scheduleTitle = resources.getString(R.string.schedule_fragment_title)
            setCurrentFragment(ScheduleFragment::class.java, scheduleTitle)
        }
    }

    private fun setCurrentFragment(fragmentClass: Class<*>, appBarTitle: String) {
        var fragment: Fragment? = null
        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commit()
        // Set action bar title
        title = appBarTitle
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        // Handle menu options that aren't a fragment
        when (menuItem.itemId) {
            R.id.side_drawer_menu_item_profile -> {
                onProfileMenuOptionClicked()
                binding.drawerLayout.closeDrawers()
                return
            }
            R.id.side_drawer_menu_item_logout -> {
                onLogOutMenuOptionClicked()
                binding.drawerLayout.closeDrawers()
                return
            }
        }

        // Create a new fragment and specify the fragment to show based on nav item clicked
        val fragmentClass: Class<*> = when (menuItem.itemId) {
            R.id.side_drawer_menu_item_schedule -> ScheduleFragment::class.java
            R.id.side_drawer_menu_item_security -> SecurityFragment::class.java
            R.id.side_drawer_menu_item_studybuddy -> StudyBuddyFragment::class.java
            R.id.side_drawer_menu_item_friends -> FriendsFragment::class.java
            R.id.side_drawer_menu_item_ratings -> RatingsFragment::class.java
            R.id.side_drawer_menu_item_my_enrolled_schedule -> ShowMySchedule::class.java
            else -> ScheduleFragment::class.java
        }
        setCurrentFragment(fragmentClass, menuItem.title.toString())

        // Highlight the selected item has been done by NavigationView
        menuItem.isChecked = true
        // Close the navigation drawer
        binding.drawerLayout.closeDrawers()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    private fun initializeFriendsList() {
        val currentUser = Auth.getCurrentUser()!!
        val currentUserId = currentUser.id

        // Set up friend value listener
        val dbReference = FirebaseDatabase
            .getInstance("https://hubspot-629d4-default-rtdb.firebaseio.com/").reference
        dbReference.child("Users/${currentUserId}/friends").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // clear previous friends list value
                    friendsList.clear()
                    // create a list of friends users
                    var friendUser: User
                    for(friend in dataSnapshot.children) {
                        friendUser = User(friend.value.toString())
                        friendsList.add(friendUser)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("debug: $databaseError")
                }
            })
    }

    private fun sendVolumeBroadcast(action: Int?, keyCode: Int?, buttonType: String) {
        val intent = Intent("silentButtonPressed").putExtra("buttonPressType",buttonType)
        if (action!= null) {
            intent.putExtra("action", action)
        }
        if (keyCode != null) {
            intent.putExtra("keyCode", keyCode)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action: Int = event.action
        val keyCode: Int = event.keyCode
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    upStartPressTime = event.downTime
                } else if (action == KeyEvent.ACTION_UP) {
                    upEndPressTime = event.eventTime
                    val millisPressed = upEndPressTime - upStartPressTime
                    if (millisPressed >= 2000) {
                        sendVolumeBroadcast(action, keyCode, "pingLocationButton")
                    }
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    downStartPressTime = event.downTime
                }
                else if (action == KeyEvent.ACTION_UP) {
                    downEndPressTime = event.eventTime
                    val millisPressed = downEndPressTime - downStartPressTime
                    if (millisPressed >= 2000) {
                        resetEmergencyButtonQuickPress()
                        sendVolumeBroadcast(action, keyCode, "speechToTextButton")
                    } else if (millisPressed in 25..1500) {
                        if (SecurityFragment.securityViewModel.emergencyServicesToggle.value == true) {
                            countEmergencyButtonQuickPress()
                            displayEmergencyButtonCount()
                            if (downButtonPressedCount == 5) {
                                resetEmergencyButtonQuickPress()
                                sendVolumeBroadcast(action, keyCode, "emergencyButton")
                            }
                        }
                    }
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun onLogOutMenuOptionClicked() {
        Auth.signOutCurrentUser()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onProfileMenuOptionClicked() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun countEmergencyButtonQuickPress() {
        downButtonPressedCount++
        downButtonPressedCountDown--
    }

    private fun displayEmergencyButtonCount() {
        var emergencyDownPressedToast: Toast? = null
        if (emergencyDownPressedToast != null) {
            emergencyDownPressedToast.cancel()
        }
        if (downButtonPressedCountDown > 0 ) {
            emergencyDownPressedToast = Toast.makeText(this,
                "Press $downButtonPressedCountDown more times to call 911", Toast.LENGTH_SHORT)
            emergencyDownPressedToast.show()
        }
    }

    private fun resetEmergencyButtonQuickPress() {
        downButtonPressedCount = 0
        downButtonPressedCountDown = 5
    }
}