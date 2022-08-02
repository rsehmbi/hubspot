package com.example.hubspot.profile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.auth.AuthRepository
import com.example.hubspot.auth.AuthViewModel


class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // enable action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile";

        refreshDisplayNameText()
    }

    fun refreshDisplayNameText() {
        val displayNameTextView =
            findViewById<TextView>(R.id.activity_profile_textview_display_name)
        val user = Auth.getCurrentUser()
        displayNameTextView.text = user?.displayName
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // back button pressed
        finish()
        return super.onOptionsItemSelected(item)
    }

    fun onChangeNameButtonClick(view: View) {
        val updateNameDialog = NameDialog()
        updateNameDialog.show(supportFragmentManager, "update_name_dialog")
    }


}