package com.example.hubspot.auth

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.hubspot.auth.AuthRepository.AuthResult

/** Allows app pages that deal with authentication to follow the MVVM architecture.
 *  Features include sign in, sign up, resending account activation emails,
 *  and more. */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository: AuthRepository = AuthRepository()

    var signInUserResult: MutableLiveData<AuthResult> = MutableLiveData()
    var signUpUserResult: MutableLiveData<AuthResult> = MutableLiveData()
    var resendActivationEmailResult: MutableLiveData<AuthResult> = MutableLiveData()
    var updateDisplayNameResult: MutableLiveData<AuthResult> = MutableLiveData()
    var sendPasswordResetEmailResult: MutableLiveData<AuthResult> = MutableLiveData()
    var updateProfilePictureResult: MutableLiveData<AuthResult> = MutableLiveData()

    fun signInUser(email: String, password: String) {
        authRepository.signInUser(email, password, signInUserResult)
    }

    fun signUpUser(email: String, password: String) {
        authRepository.signUpUser(email, password, signUpUserResult)
    }

    fun resendActivationEmail() {
        authRepository.resendActivationEmail(resendActivationEmailResult)
    }

    fun updateUserDisplayName(newDisplayName: String) {
        authRepository.updateUserDisplayName(newDisplayName, updateDisplayNameResult)
    }

    fun sendPasswordResetEmail(email: String) {
        authRepository.sendPasswordResetEmail(email, sendPasswordResetEmailResult)
    }

    fun updateProfilePicture(picture: Bitmap) {
        authRepository.updateProfilePicture(picture, updateProfilePictureResult)
    }
}