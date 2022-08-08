package com.example.hubspot

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
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
import com.example.hubspot.profile.ProfileActivity
import com.example.hubspot.ratings.RatingsFragment
import com.example.hubspot.schedule.ScheduleFragment
import com.example.hubspot.schedule.ShowMySchedule
import com.example.hubspot.security.ui.SecurityFragment
import com.example.hubspot.studybuddy.StudyBuddyFragment
import com.google.android.material.navigation.NavigationView

/** The main activity for the application, which displays the different
 *  fragments for each feature. A side navigation drawer is used for
 *  navigation between the fragments and to logout or check user profiles.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

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