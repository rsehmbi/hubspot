package com.example.hubspot.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.auth.AuthRepository
import com.example.hubspot.auth.AuthViewModel
import com.example.hubspot.login.LoginActivity
import com.example.hubspot.utils.Util


/** An activity which allows the user to display and update
 *  their personal profile information.
 */
class ProfileActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var tempImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // enable action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // set activity title
        val appBarTitle = resources.getString(R.string.activity_profile_appbar_title)
        supportActionBar?.title = appBarTitle

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

        // Set up view model to automatically update profile image view on userImage change
        val profilePicture = findViewById<ImageView>(R.id.activity_profile_imageview_picture)
        val profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.userImage.observe(this, Observer { it ->
            // authviewmodel profilePicture.setImageBitmap(it)
        })

        // Set up activity result for changing profile photo from camera
        cameraResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    try {
                        val tempImgBitmap = Util.getBitmap(this, tempImageUri)
                        authViewModel.
                    } catch (e: Exception) {
                        val errorText =
                            resources.getString(R.string.activity_profile_toast_camera_fail)
                        Toast.makeText(
                            this,
                            errorText,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        // Set up activity result for changing profile photo from gallery
        galleryResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedImageUri = result.data?.data
                    if (selectedImageUri != null) {
                        try {
                            val bitmap = Util.getBitmap(this, selectedImageUri)
                            profileViewModel.userImage.value = bitmap
                        } catch (e: Exception) {
                            val errorText =
                                resources.getString(R.string.activity_profile_gallery_error)
                            Toast.makeText(
                                this,
                                errorText,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

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

    fun onChangePictureButtonClick(view: View) {

    }

}