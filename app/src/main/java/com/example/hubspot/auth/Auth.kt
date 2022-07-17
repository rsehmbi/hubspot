package com.example.hubspot.auth

import com.example.hubspot.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/** Contains helper methods related to authentication. */
object Auth {
    /** Returns the currently logged in user or null if there is none. */
    fun getCurrentUser(): User? {
        return AuthRepository.getCurrentUser()
    }

    fun signOutCurrentUser() {
        Firebase.auth.signOut()
    }
}