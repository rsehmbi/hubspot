package com.example.hubspot.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.auth.Auth
import com.example.hubspot.auth.AuthRepository
import com.example.hubspot.auth.AuthViewModel
import com.example.hubspot.login.LoginActivity
import com.example.hubspot.utils.Util
import com.squareup.picasso.Picasso
import java.io.File


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

        Util.checkCameraAndStoragePermissions(this)

        // Temporary file for storing changed images not saved yet
        tempImageUri = getFileUri("tempProfileImage.jpg", "com.example.hubspot")

        // enable action bar back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // set activity title
        val appBarTitle = resources.getString(R.string.activity_profile_appbar_title)
        supportActionBar?.title = appBarTitle

        refreshDisplayNameText()
        initAuthViewModel()
        initCameraAndGalleryIntents()
        loadProfilePicture()
    }

    private fun loadProfilePicture() {
        setLoading(true)
        authViewModel.getProfilePictureUri()
    }

    private fun initCameraAndGalleryIntents() {
        // Set up activity result for changing profile photo from camera
        cameraResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    try {
                        val tempImgBitmap = Util.getBitmap(this, tempImageUri)
                        setLoading(true)
                        authViewModel.updateProfilePicture(tempImgBitmap)
                    } catch (e: Exception) {
                        setLoading(false)
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
                            setLoading(true)
                            authViewModel.updateProfilePicture(bitmap)
                        } catch (e: Exception) {
                            setLoading(false)
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

    private fun getFileUri(fileName: String, authority: String): Uri {
        val tempImageFile = File(getExternalFilesDir(null), fileName)
        val fileUri = FileProvider.getUriForFile(this, authority, tempImageFile)
        return fileUri
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
        val picImageView = findViewById<ImageView>(R.id.activity_profile_imageview_picture)
        authViewModel.updateProfilePictureResult.observe(this) {
            if (it.resultCode == AuthRepository.UpdatePictureResultCode.FAILURE) {
                val errorMessage =
                    resources.getString(R.string.activity_profile_toast_upload_picture_fail)
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            } else {
                Picasso.with(this).load(it.updatedImageUri).into(picImageView)
            }
            setLoading(false)
        }

        authViewModel.getProfilePictureUriResult.observe(this) {
            if (it.resultCode == AuthRepository.GetProfilePictureUriResultCode.FAILURE) {
                val errorMessage =
                    resources.getString(R.string.activity_profile_toast_get_picture_fail)
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            } else {
                Picasso.with(this).load(it.imageUri).into(picImageView)
            }
            setLoading(false)
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
            findViewById<Button>(R.id.activity_profile_button_change_picture).isEnabled = false
            findViewById<Button>(R.id.activity_profile_button_change_name).isEnabled = false
            findViewById<Button>(R.id.activity_profile_button_reset_password).isEnabled = false
        } else {
            val loadingSpinner = findViewById<ProgressBar>(R.id.activity_profile_loading_spinner)
            loadingSpinner.visibility = View.GONE
            findViewById<Button>(R.id.activity_profile_button_change_picture).isEnabled = true
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
        val dialogTitle = resources.getString(R.string.activity_profile_change_title)
        val cameraOption = resources.getString(R.string.activity_profile_change_option_camera)
        val galleryOption = resources.getString(R.string.activity_profile_change_option_gallery)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val listItems = arrayOf(cameraOption, galleryOption)

        builder.setTitle(dialogTitle)
        builder.setItems(
            listItems
        ) { _, optionIdx ->
            val selectedOption = listItems[optionIdx]
            onChangePictureOptionSelected(selectedOption)
        }

        val ret = builder.create()
        ret.show()
    }

    private fun onChangePictureOptionSelected(selectedOption: String) {
        val cameraOption = resources.getString(R.string.activity_profile_change_option_camera)
        val galleryOption = resources.getString(R.string.activity_profile_change_option_gallery)

        when (selectedOption) {
            cameraOption -> takeProfilePhotoWithCamera()
            galleryOption -> takeProfilePhotoFromGallery()
        }
    }

    private fun takeProfilePhotoWithCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri)
        cameraResult.launch(intent)
    }

    private fun takeProfilePhotoFromGallery() {
        val pickGalleryPhotoIntent = Intent(Intent.ACTION_PICK)
        pickGalleryPhotoIntent.type = "image/*" // only pick images
        galleryResult.launch(pickGalleryPhotoIntent)
    }

}