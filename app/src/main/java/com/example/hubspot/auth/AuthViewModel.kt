package com.example.hubspot.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.hubspot.auth.AuthRepository.AuthResult

/** Allows app pages that deal with authentication to follow the MVVM architecture */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository: AuthRepository = AuthRepository()

    var signInUserResult: MutableLiveData<AuthResult> = MutableLiveData()
    var signUpUserResult: MutableLiveData<AuthResult> = MutableLiveData()
    var resendActivationEmailResult: MutableLiveData<AuthResult> = MutableLiveData()

    fun signInUser(email: String, password: String) {
        authRepository.signInUser(email, password, signInUserResult)
    }

    fun signUpUser(email: String, password: String) {
        authRepository.signUpUser(email, password, signUpUserResult)
    }

    fun resendActivationEmail() {
        authRepository.resendActivationEmail(resendActivationEmailResult)
    }
}