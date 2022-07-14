package com.example.hubspot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
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
        val auth = Firebase.auth
        if (email.isEmpty() || password.isEmpty()) {
            displayEmptyEmailOrPasswordError()
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
        }
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
        val auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Successfully created account, just need to activate by email
                    sendAccountActivationEmail()
                } else {
                    // Failed to create account, display error message
                    displayAuthError(task)
                }
            }
    }

    private fun sendAccountActivationEmail() {
        // Successfully created account, now send activation email
        val user = Firebase.auth.currentUser

        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Activation email successfully sent
                    displaySignUpSuccessMessage()
                } else {
                    // Failed to send activation email
                    displaySendEmailVerificationError(task)
                }
            }
    }

    private fun displaySignUpSuccessMessage() {
        val successMessage = resources.getString(R.string.login_toast_sign_up_success)
        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
    }

    private fun displaySendEmailVerificationError(task: Task<Void>) {
        val errorCode =
            (task.exception as FirebaseAuthException?)!!.errorCode
        val errorMessage = resources.getString(R.string.login_toast_send_email_fail)
        Toast.makeText(this, errorMessage + errorCode, Toast.LENGTH_LONG).show()
    }

    fun onResendActivationEmailButtonClick(view: View) {
        sendAccountActivationEmail()
    }
}