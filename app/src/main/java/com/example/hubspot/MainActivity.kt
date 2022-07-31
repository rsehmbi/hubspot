package com.example.hubspot

import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hubspot.databinding.ActivityMainBinding
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.hubspot.auth.Auth
import com.example.hubspot.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        val navView: BottomNavigationView = binding.bottomNavigationView
        val navController = findNavController(R.id.flFragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_schedule, R.id.navigation_ratings, R.id.navigation_studdybuddy, R.id.navigation_security,R.id.navigation_friends
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    fun onLogOutMenuOptionClicked(item: MenuItem) {
        Auth.signOutCurrentUser()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}