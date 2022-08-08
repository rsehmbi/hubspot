package com.example.hubspot

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hubspot.auth.Auth
import com.example.hubspot.databinding.ActivityMainBinding
import com.example.hubspot.login.LoginActivity
import com.example.hubspot.models.User
import com.example.hubspot.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var startPressTime = 0L
    private var endPressTime = 0L

    companion object {
        var friendsList = ArrayList<User>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is currently logged in, go to login screen if not
        if (Auth.getCurrentUser() == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.bottomNavigationView
        val navController = findNavController(R.id.flFragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_schedule, R.id.navigation_ratings, R.id.navigation_studdybuddy,
                R.id.navigation_security, R.id.navigation_friends
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        initializeFriendsList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
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
        val intent = Intent("silentButtonPressed").putExtra("buttonType",buttonType)
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
                    startPressTime = event.downTime
                } else if (action == KeyEvent.ACTION_UP) {
                    endPressTime = event.eventTime
                    val millisPressed = endPressTime - startPressTime
                    if (millisPressed >= 2000) {
                        sendVolumeBroadcast(action, keyCode, "pingLocationButton")
                    }
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    sendVolumeBroadcast(action, keyCode, "emergencyButton")
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    fun onLogOutMenuOptionClicked(item: MenuItem) {
        Auth.signOutCurrentUser()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onProfileMenuOptionClicked(item: MenuItem) {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
}