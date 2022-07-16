package com.example.hubspot.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.hubspot.auth.AuthRepository.AuthResult

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val loginRepository: AuthRepository = AuthRepository()

    var signInUserResult: MutableLiveData<AuthResult> = MutableLiveData()
    var signUpUserResult: MutableLiveData<AuthResult> = MutableLiveData()
    var resendVerificationEmailResult: MutableLiveData<AuthResult> = MutableLiveData()

    fun signInUser(email: String, password: String) {
        loginRepository.signInUser(email, password, signInUserResult)
    }

    fun signUp(email: String, password: String) {
        loginRepository.signUpUser(email, password, signUpUserResult)
    }

    fun resendVerificationEmail() {
        loginRepository.resendVerificationEmail(resendVerificationEmailResult)
    }
}