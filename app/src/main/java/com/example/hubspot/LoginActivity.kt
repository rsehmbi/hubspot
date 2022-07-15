package com.example.hubspot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onLogInButtonClick(view: View) {
        val email = findViewById<EditText>(R.id.login_edittext_email).text.toString()
        val password = findViewById<EditText>(R.id.login_edittext_password).text.toString()
        signInUser(email, password)
    }

    private fun signInUser(email: String, password: String) {
        setScreenLoading(true)
        val auth = Firebase.auth
        if (checkIfEmailOrPasswordIsEmpty(email, password)) {
            setScreenLoading(false)
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Successfully signed in, check if email is verified
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.isEmailVerified) {
                    gotoMainActivity()
                } else {
                    displayEmailNotActivatedError()
                }
            } else {
                // Failed to sign in, tell the user why
                displayAuthError(task)
            }
            setScreenLoading(false)
        }
    }

    private fun checkIfEmailOrPasswordIsEmpty(
        email: String,
        password: String
    ): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            displayEmptyEmailOrPasswordError()
            return true
        }
        return false
    }

    private fun displayEmptyEmailOrPasswordError() {
        val errorMessage = resources.getString(R.string.login_toast_empty_email_or_password)
        Toast.makeText(
            this, errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun displayEmailNotActivatedError() {
        val errorMessage = resources.getString(R.string.login_toast_email_not_activated)
        Toast.makeText(
            this, errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayAuthError(task: Task<AuthResult>) {
        val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
        when (errorCode) {
            "ERROR_INVALID_EMAIL" -> {
                val errorMessage =
                    resources.getString(R.string.login_toast_invalid_email)
                Toast.makeText(
                    this,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            "ERROR_USER_NOT_FOUND" -> {
                val errorMessage =
                    resources.getString(R.string.login_toast_user_not_found)
                Toast.makeText(
                    this,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            "ERROR_WRONG_PASSWORD" -> {
                val errorMessage =
                    resources.getString(R.string.login_toast_invalid_password)
                Toast.makeText(
                    this,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            "ERROR_WEAK_PASSWORD" -> {
                val errorMessage =
                    resources.getString(R.string.login_toast_weak_password)
                Toast.makeText(
                    this,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                val errorMessage =
                    resources.getString(R.string.login_toast_email_already_used)
                Toast.makeText(
                    this,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                val errorMessage =
                    resources.getString(R.string.login_toast_sign_up_fail)
                Toast.makeText(this, errorMessage + errorCode, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun onSignUpButtonClick(view: View) {
        val emailEditText = findViewById<EditText>(R.id.login_edittext_email)
        val passwordEditText = findViewById<EditText>(R.id.login_edittext_password)
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        createUserAccount(email, password)
    }

    private fun createUserAccount(email: String, password: String) {
        setScreenLoading(true)

        val auth = Firebase.auth
        if (checkIfEmailOrPasswordIsEmpty(email, password)) {
            setScreenLoading(false)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Successfully created account, just need to activate by email
                    sendAccountActivationEmail()
                } else {
                    // Failed to create account, display error message
                    displayAuthError(task)
                    setScreenLoading(false)
                }
            }
    }

    private fun setScreenLoading(isLoading: Boolean) {
        val loadingSpinner = findViewById<ProgressBar>(R.id.login_loading_spinner)
        val logInButton = findViewById<Button>(R.id.login_button_login)
        val signUpButton = findViewById<Button>(R.id.login_button_signup)
        val resendButton = findViewById<Button>(R.id.login_button_resend_email)

        if (isLoading) {
            loadingSpinner.visibility = View.VISIBLE
            logInButton.isEnabled = false
            signUpButton.isEnabled = false
            resendButton.isEnabled = false
        } else {
            loadingSpinner.visibility = View.GONE
            logInButton.isEnabled = true
            signUpButton.isEnabled = true
            resendButton.isEnabled = true
        }
    }

    private fun sendAccountActivationEmail() {
        // Successfully created account, now send activation email
        val user = Firebase.auth.currentUser
        if (user == null) {
            // No attempted login or signup, can't send activation email yet
            displayTryLoggingInFirstMessage()
            setScreenLoading(false)
            return
        }

        if (user.isEmailVerified) {
            // User email is already verified, don't send again
            val errorMessage = resources.getString(R.string.login_toast_email_already_activated)
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            setScreenLoading(false)
            return
        }

        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Activation email successfully sent
                    displaySignUpSuccessMessage()
                } else {
                    // Failed to send activation email
                    displaySendEmailVerificationError(task)
                }
                setScreenLoading(false)
            }
    }

    private fun displayTryLoggingInFirstMessage() {
        val errorMessage = resources.getString(R.string.login_toast_try_logging_in_first)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun displaySignUpSuccessMessage() {
        val successMessage = resources.getString(R.string.login_toast_sign_up_success)
        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
    }

    private fun displaySendEmailVerificationError(task: Task<Void>) {
        if (task.exception is FirebaseTooManyRequestsException) {
            // User is sending emails too quickly, Firebase has throttled us
            displaySendingEmailsTooQuicklyError()
            return
        }

        val errorCode =
            (task.exception as FirebaseAuthException?)!!.errorCode
        val errorMessage = resources.getString(R.string.login_toast_send_email_fail)
        Toast.makeText(this, errorMessage + errorCode, Toast.LENGTH_LONG).show()
    }

    private fun displaySendingEmailsTooQuicklyError() {
        val successMessage = resources.getString(R.string.login_toast_sending_email_too_fast)
        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
    }

    fun onResendActivationEmailButtonClick(view: View) {
        setScreenLoading(true)
        sendAccountActivationEmail()
    }
}