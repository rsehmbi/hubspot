package com.example.hubspot.auth

import androidx.lifecycle.MutableLiveData
import com.example.hubspot.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AuthRepository {

    fun signInUser(
        email: String,
        password: String,
        authResult: MutableLiveData<AuthResult>
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            authResult.value =
                AuthResult(AuthResultCode.EMAIL_PASSWORD_EMPTY, null)
            return
        }

        val auth = Firebase.auth
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Successfully signed in, check if email is verified
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.isEmailVerified) {
                    authResult.value = AuthResult(
                        AuthResultCode.SUCCESS,
                        User(currentUser.uid, currentUser.email!!)
                    )
                } else {
                    authResult.value =
                        AuthResult(AuthResultCode.EMAIL_NOT_VERIFIED, null)
                }
            } else {
                // Failed to sign in, tell the user why
                handleAuthError(task, authResult)
            }
        }
    }

    class AuthResult(val resultCode: AuthResultCode, val loggedInUser: User?)

    enum class AuthResultCode {
        SUCCESS, EMAIL_PASSWORD_EMPTY, EMAIL_NOT_VERIFIED, INVALID_EMAIL,
        UNKNOWN_ERROR, EMAIL_ALREADY_USED, WEAK_PASSWORD, WRONG_PASSWORD,
        USER_NOT_FOUND, NO_LOGIN_OR_SIGNUP, EMAIL_ALREADY_VERIFIED,
        FAILED_TO_SEND_VERIFICATION_EMAIL, TOO_MANY_REQUESTS_AT_ONCE
    }

    fun handleAuthError(
        task: Task<com.google.firebase.auth.AuthResult>,
        authResult: MutableLiveData<AuthResult>
    ) {
        if (task.exception is FirebaseTooManyRequestsException) {
            authResult.value = AuthResult(AuthResultCode.TOO_MANY_REQUESTS_AT_ONCE, null)
            return
        }

        lateinit var errorCode: String

        try {
            errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
        } catch (e: Exception) {
            authResult.value = AuthResult(AuthResultCode.UNKNOWN_ERROR, null)
            return
        }

        when (errorCode) {
            "ERROR_INVALID_EMAIL" -> {
                authResult.value = AuthResult(AuthResultCode.INVALID_EMAIL, null)
            }
            "ERROR_USER_NOT_FOUND" -> {
                authResult.value = AuthResult(AuthResultCode.USER_NOT_FOUND, null)
            }
            "ERROR_WRONG_PASSWORD" -> {
                authResult.value = AuthResult(AuthResultCode.WRONG_PASSWORD, null)
            }
            "ERROR_WEAK_PASSWORD" -> {
                authResult.value = AuthResult(AuthResultCode.WEAK_PASSWORD, null)
            }
            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                authResult.value =
                    AuthResult(AuthResultCode.EMAIL_ALREADY_USED, null)
            }
            else -> {
                authResult.value = AuthResult(AuthResultCode.UNKNOWN_ERROR, null)
            }
        }
    }

    fun signUpUser(
        email: String,
        password: String,
        signUpUserResult: MutableLiveData<AuthResult>
    ) {
        val auth = Firebase.auth
        if (email.isEmpty() || password.isEmpty()) {
            signUpUserResult.value = AuthResult(AuthResultCode.EMAIL_PASSWORD_EMPTY, null)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successfully created account, just need to activate by email
                    sendVerificationEmail(signUpUserResult)
                } else {
                    // Failed to create account, display error message
                    handleAuthError(task, signUpUserResult)
                }
            }
    }

    private fun sendVerificationEmail(authResult: MutableLiveData<AuthResult>) {
        // Successfully created account, now send verification email (or resend it)
        val user = Firebase.auth.currentUser
        if (user == null) {
            // No attempted login or signup, can't send verification email yet
            authResult.value = AuthResult(AuthResultCode.NO_LOGIN_OR_SIGNUP, null)
            return
        }

        if (user.isEmailVerified) {
            // User email is already verified, don't send again
            authResult.value = AuthResult(AuthResultCode.EMAIL_ALREADY_VERIFIED, null)
            return
        }

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // verification email successfully sent
                    authResult.value =
                        AuthResult(AuthResultCode.SUCCESS, User(user.uid, user.email!!))
                } else {
                    // Failed to send verification email
                    if (task.exception is FirebaseTooManyRequestsException) {
                        // User is sending emails too quickly, Firebase has throttled us
                        authResult.value =
                            AuthResult(AuthResultCode.TOO_MANY_REQUESTS_AT_ONCE, null)
                    } else {
                        authResult.value =
                            AuthResult(AuthResultCode.FAILED_TO_SEND_VERIFICATION_EMAIL, null)
                    }
                }
            }
    }

    fun resendVerificationEmail(resendVerificationEmailResult: MutableLiveData<AuthResult>) {
        sendVerificationEmail(resendVerificationEmailResult)
    }
}