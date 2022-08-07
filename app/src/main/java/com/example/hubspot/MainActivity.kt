package com.example.hubspot

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.hubspot.auth.Auth
import com.example.hubspot.databinding.ActivityMainBinding
import com.example.hubspot.friends.FriendsFragment
import com.example.hubspot.login.LoginActivity
import com.example.hubspot.profile.ProfileActivity
import com.example.hubspot.ratings.RatingsFragment
import com.example.hubspot.schedule.ScheduleFragment
import com.example.hubspot.security.ui.SecurityFragment
import com.example.hubspot.studybuddy.StudyBuddyFragment
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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

        replaceActionBarWithToolBar()

        // Setup drawer view
        val drawer = binding.nvView
        setupDrawerContent(drawer);

        // Setup animated hamburger button
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        val drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            findViewById(R.id.toolbar),
            R.string.drawer_open,
            R.string.drawer_close
        )

        // Setup toggle to display hamburger icon with nice animation
        drawerToggle.isDrawerIndicatorEnabled = true;
        drawerToggle.syncState();

        // Tie DrawerLayout events to the ActionBarToggle
        binding.drawerLayout.addDrawerListener(drawerToggle);
    }

    private fun replaceActionBarWithToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }

        // Use schedule fragment as default fragment
        setCurrentFragment(ScheduleFragment::class.java)
    }

    private fun setCurrentFragment(fragmentClass: Class<*>) {
        var fragment: Fragment? = null
        try {
            fragment = fragmentClass.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment!!).commit()
    }

    fun selectDrawerItem(menuItem: MenuItem) {
        // Handle menu options that aren't a fragment
        when (menuItem.itemId) {
            R.id.side_drawer_menu_item_profile -> {
                onProfileMenuOptionClicked()
                return
            }
            R.id.side_drawer_menu_item_logout -> {
                onLogOutMenuOptionClicked()
                return
            }
        }

        // Create a new fragment and specify the fragment to show based on nav item clicked
        var fragment: Fragment? = null
        val fragmentClass: Class<*> = when (menuItem.itemId) {
            R.id.side_drawer_menu_item_schedule -> ScheduleFragment::class.java
            R.id.side_drawer_menu_item_security -> SecurityFragment::class.java
            R.id.side_drawer_menu_item_studybuddy -> StudyBuddyFragment::class.java
            R.id.side_drawer_menu_item_friends -> FriendsFragment::class.java
            R.id.side_drawer_menu_item_ratings -> RatingsFragment::class.java
            else -> ScheduleFragment::class.java
        }
        setCurrentFragment(fragmentClass)

        // Highlight the selected item has been done by NavigationView
        menuItem.isChecked = true
        // Set action bar title
        title = menuItem.title
        // Close the navigation drawer
        binding.drawerLayout.closeDrawers()
    }

    private fun sendVolumeBroadcast(action: Int, keyCode: Int) {
        val intent = Intent("silentButtonPressed")
            .putExtra("action", action)
            .putExtra("keyCode", keyCode)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action: Int = event.action
        val keyCode: Int = event.keyCode
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    sendVolumeBroadcast(action, keyCode)
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    sendVolumeBroadcast(action, keyCode)
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    fun onLogOutMenuOptionClicked() {
        Auth.signOutCurrentUser()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onProfileMenuOptionClicked() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
}