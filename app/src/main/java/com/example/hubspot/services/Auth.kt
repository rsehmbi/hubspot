package com.example.hubspot.services

/** Authentication service module to provide user authentication for the app. */
object Auth {
    // Will be removed, for mock navigation flow purposes.
    var initialLogin: Boolean = true

    /** Returns a User object for the currently logged in user.
     *  Returns null if there is no logged in user. */
    fun getCurrentUser(): User? {
        if (initialLogin) {
            initialLogin = false
            return null
        } else {
            return User(0, "testing@email.com")
        }
    }

    /** Sends an account activation email to the user. Once the user clicks
     *  on the account activation link in the email, they can log in.
     *  Returns a status code that describes the result of the operation. */
    fun sendAccountActivationEmail(
        email: String,
        password: String
    ): SendAccountActivationEmailResult {
        return SendAccountActivationEmailResult.SUCCESS
    }

    enum class SendAccountActivationEmailResult {
        SUCCESS,
        FAILURE
    }

    /** Attempts to sign in the user with a given email and password.
     *  Returns an error code that describes the result of the operation. */
    fun signInUser(email: String, password: String): SignInUserResult {
        return SignInUserResult.SUCCESS
    }

    enum class SignInUserResult {
        SUCCESS,
        FAILURE
    }
}

class User(val id: Int, val email: String) {}