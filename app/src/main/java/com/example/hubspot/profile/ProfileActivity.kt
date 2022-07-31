package com.example.hubspot.profile

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.hubspot.R


class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // enable action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile";
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // back button pressed
        finish()
        return super.onOptionsItemSelected(item)
    }
}