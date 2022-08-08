package com.example.hubspot.profile

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/** ViewModel used for updating an image view when a user image changes */
class ProfileViewModel: ViewModel() {
    val userImage = MutableLiveData<Bitmap>()
}