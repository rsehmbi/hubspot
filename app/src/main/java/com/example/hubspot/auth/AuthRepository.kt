package com.example.hubspot.auth

import androidx.lifecycle.MutableLiveData
import com.example.hubspot.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

/** Layer of abstraction between the AuthViewModel and Firebase Auth.
 *  Handles Firebase implementation to fulfill the AuthViewModel functionality
 *  and Auth helper object functionality */
class AuthRepository {

    companion object {
        fun getCurrentUser(): User? {
            val firebaseUser = Firebase.auth.currentUser
            if (firebaseUser != null && firebaseUser.isEmailVerified) {
                return User(firebaseUser.uid, firebaseUser.email, firebaseUser.displayName)
            }
            return null
        }
    }

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
                // Successfully signed in, check if email is verified (and thus acc activated)
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.isEmailVerified) {
                    val user = User(currentUser.uid, currentUser.email, currentUser.email)
                    createDatabaseUserIfNotExists(user, authResult)
                } else {
                    authResult.value =
                        AuthResult(AuthResultCode.ACCOUNT_NOT_ACTIVATED, null)
                }
            } else {
                // Failed to sign in, tell the user why
                handleAuthError(task, authResult)
            }
        }
    }

    class AuthResult(val resultCode: AuthResultCode, val loggedInUser: User?)

    enum class AuthResultCode {
        SUCCESS, EMAIL_PASSWORD_EMPTY, ACCOUNT_NOT_ACTIVATED, INVALID_EMAIL,
        UNKNOWN_ERROR, EMAIL_ALREADY_USED, WEAK_PASSWORD, WRONG_PASSWORD,
        USER_NOT_FOUND, NO_LOGIN_OR_SIGNUP, ACCOUNT_ALREADY_ACTIVATED,
        FAILED_TO_SEND_ACTIVATION_EMAIL, TOO_MANY_REQUESTS_AT_ONCE,
        FAILED_TO_READ_DATABASE, FAILED_TO_WRITE_USER_TO_DATABASE, FAILED_TO_SET_AUTH_DISPLAY_NAME
    }

    private fun createDatabaseUserIfNotExists(user: User, authResult: MutableLiveData<AuthResult>) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("Users")

        val getUserByIdQuery = usersRef.orderByChild("id").equalTo(user.id)
        val getUserListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get User object by id (should only be one in list or null)
                val foundUsers = dataSnapshot.value
                if (foundUsers == null || (foundUsers as HashMap<*, *>).isEmpty()) {
                    // no user in db exists, so create one
                    createUserInDatabase(user, authResult)
                } else {
                    // user already exists, just log in already
                    authResult.value = AuthResult(
                        AuthResultCode.SUCCESS,
                        user
                    )
                }

                // Prevent this listener from firing even after logging in the app
                getUserByIdQuery.removeEventListener(this)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting User failed, return error to view model to update UI
                authResult.value = AuthResult(AuthResultCode.FAILED_TO_READ_DATABASE, null)
            }
        }

        getUserByIdQuery.addValueEventListener(getUserListener)
    }

    private fun createUserInDatabase(user: User, authResult: MutableLiveData<AuthResult>) {
        // Update display name to email default first
        val updateDisplayName = UserProfileChangeRequest.Builder()
            .setDisplayName(user.email).build()
        val firebaseUser = Firebase.auth.currentUser
        firebaseUser!!.updateProfile(updateDisplayName).addOnCompleteListener { updateTask ->
            if (updateTask.isSuccessful) {
                // Then, create user in database
                val database = FirebaseDatabase.getInstance()
                val usersRef = database.getReference("Users")

                usersRef.child(user.id!!).setValue(user).addOnCompleteListener { createUserTask ->
                    if (createUserTask.isSuccessful) {
                        authResult.value = AuthResult(
                            AuthResultCode.SUCCESS,
                            user
                        )
                    } else {
                        authResult.value =
                            AuthResult(AuthResultCode.FAILED_TO_WRITE_USER_TO_DATABASE, null)
                    }
                }
            } else {
                authResult.value = AuthResult(AuthResultCode.FAILED_TO_SET_AUTH_DISPLAY_NAME, null)
            }
        }
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
                    sendActivationEmail(signUpUserResult)
                } else {
                    // Failed to create account, display error message
                    handleAuthError(task, signUpUserResult)
                }
            }
    }

    private fun sendActivationEmail(authResult: MutableLiveData<AuthResult>) {
        // Successfully created account, now send activation email (or resend it)
        val user = Firebase.auth.currentUser
        if (user == null) {
            // No attempted login or signup, can't send activation email yet
            authResult.value = AuthResult(AuthResultCode.NO_LOGIN_OR_SIGNUP, null)
            return
        }

        if (user.isEmailVerified) {
            // User email is already verified, don't send again
            authResult.value = AuthResult(AuthResultCode.ACCOUNT_ALREADY_ACTIVATED, null)
            return
        }

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // activation email successfully sent
                    authResult.value =
                        AuthResult(
                            AuthResultCode.SUCCESS,
                            User(user.uid, user.email, user.displayName)
                        )
                } else {
                    // Failed to send activation email
                    if (task.exception is FirebaseTooManyRequestsException) {
                        // User is sending emails too quickly, Firebase has throttled us
                        authResult.value =
                            AuthResult(AuthResultCode.TOO_MANY_REQUESTS_AT_ONCE, null)
                    } else {
                        authResult.value =
                            AuthResult(AuthResultCode.FAILED_TO_SEND_ACTIVATION_EMAIL, null)
                    }
                }
            }
    }

    fun resendActivationEmail(resendActivationEmailResult: MutableLiveData<AuthResult>) {
        sendActivationEmail(resendActivationEmailResult)
    }
}