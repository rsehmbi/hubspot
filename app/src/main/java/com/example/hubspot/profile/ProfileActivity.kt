package com.example.hubspot.profile

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.auth.AuthRepository
import com.example.hubspot.auth.AuthViewModel
import com.example.hubspot.login.LoginActivity


class ProfileActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // enable action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // set activity title
        val appBarTitle = resources.getString(R.string.activity_profile_appbar_title)
        supportActionBar?.title = appBarTitle;

        refreshDisplayNameText()
        initAuthViewModel()
    }

    fun refreshDisplayNameText() {
        val displayNameTextView =
            findViewById<TextView>(R.id.activity_profile_textview_display_name)
        val user = Auth.getCurrentUser()
        displayNameTextView.text = user?.displayName
    }

    private fun initAuthViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Wait for and handle password reset email result
        authViewModel.sendPasswordResetEmailResult.observe(this) { result ->
            // if statement is used to stop code from executing on rotation change
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (result.resultCode == AuthRepository.AuthResultCode.SUCCESS) {
                    displayResetPasswordSuccessMessage()

                    // Go back to login activity
                    Auth.signOutCurrentUser()
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    finishAffinity()
                    startActivity(loginIntent)
                } else {
                    displayResetPasswordErrorMessage()
                }
                setLoading(false)
            }
        }
    }

    private fun displayResetPasswordSuccessMessage() {
        val successMessage =
            resources.getString(R.string.activity_profile_toast_reset_password_success)
        Toast.makeText(this, successMessage, Toast.LENGTH_LONG)
            .show()
    }

    private fun displayResetPasswordErrorMessage() {
        val errorMessage =
            resources.getString(R.string.activity_profile_toast_reset_password_error)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG)
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            val loadingSpinner = findViewById<ProgressBar>(R.id.activity_profile_loading_spinner)
            loadingSpinner.visibility = View.VISIBLE
            findViewById<Button>(R.id.activity_profile_button_change_name).isEnabled = false
            findViewById<Button>(R.id.activity_profile_button_reset_password).isEnabled = false
        } else {
            val loadingSpinner = findViewById<ProgressBar>(R.id.activity_profile_loading_spinner)
            loadingSpinner.visibility = View.GONE
            findViewById<Button>(R.id.activity_profile_button_change_name).isEnabled = true
            findViewById<Button>(R.id.activity_profile_button_reset_password).isEnabled = true
        }
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

    fun onResetPasswordButtonClick(view: View) {
        setLoading(true)
        val user = Auth.getCurrentUser()
        authViewModel.sendPasswordResetEmail(user!!.email!!)
    }

}