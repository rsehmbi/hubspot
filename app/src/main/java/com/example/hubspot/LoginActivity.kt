package com.example.hubspot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.auth.AuthRepository.AuthResultCode.*
import com.example.hubspot.auth.AuthRepository.AuthResult
import com.example.hubspot.auth.AuthViewModel


class LoginActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initAuthViewModel()
    }

    private fun initAuthViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel.signInUserResult.observe(this) { result ->
            if (result.resultCode == SUCCESS) {
                gotoMainActivity()
            } else {
                displayAuthError(result)
            }
            setScreenLoading(false)
        }
        authViewModel.signUpUserResult.observe(this) { result ->
            if (result.resultCode == SUCCESS) {
                displayVerificationEmailSentMessage()
            } else {
                displayAuthError(result)
            }
            setScreenLoading(false)
        }
        authViewModel.resendVerificationEmailResult.observe(this) { result ->
            if (result.resultCode == SUCCESS) {
                displayVerificationEmailSentMessage()
            } else {
                displayAuthError(result)
            }
            setScreenLoading(false)
        }
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun displayAuthError(authResult: AuthResult) {
        when (authResult.resultCode) {
            INVALID_EMAIL -> displayInvalidEmailError()
            USER_NOT_FOUND -> displayUserNotFoundError()
            WRONG_PASSWORD -> displayWrongPasswordError()
            WEAK_PASSWORD -> displayWeakPasswordError()
            EMAIL_ALREADY_USED -> displayEmailAlreadyUsedError()
            EMAIL_PASSWORD_EMPTY -> displayEmailOrPasswordEmptyError()
            EMAIL_NOT_VERIFIED -> displayEmailNotVerifiedError()
            TOO_MANY_REQUESTS_AT_ONCE -> displayTooManyRequestsAtOnceError()
            NO_LOGIN_OR_SIGNUP -> displayNoPreviousLogInOrSignUpError()
            else -> displayCatchAllAuthError()
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

    private fun displayVerificationEmailSentMessage() {
        val successMessage = resources.getString(R.string.login_toast_sign_up_success)
        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
    }

    private fun displayCatchAllAuthError() {
        val errorMessage =
            resources.getString(R.string.login_toast_auth_fail)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun displayNoPreviousLogInOrSignUpError() {
        val errorMessage = resources.getString(R.string.login_toast_try_logging_in_first)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun displayTooManyRequestsAtOnceError() {
        val successMessage = resources.getString(R.string.login_toast_too_many_requests_at_once)
        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
    }

    private fun displayEmailNotVerifiedError() {
        val errorMessage = resources.getString(R.string.login_toast_email_not_activated)
        Toast.makeText(
            this, errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayEmailOrPasswordEmptyError() {
        val errorMessage = resources.getString(R.string.login_toast_empty_email_or_password)
        Toast.makeText(
            this, errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayEmailAlreadyUsedError() {
        val errorMessage =
            resources.getString(R.string.login_toast_email_already_used)
        Toast.makeText(
            this,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayWeakPasswordError() {
        val errorMessage =
            resources.getString(R.string.login_toast_weak_password)
        Toast.makeText(
            this,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayWrongPasswordError() {
        val errorMessage =
            resources.getString(R.string.login_toast_invalid_password)
        Toast.makeText(
            this,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayUserNotFoundError() {
        val errorMessage =
            resources.getString(R.string.login_toast_user_not_found)
        Toast.makeText(
            this,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayInvalidEmailError() {
        val errorMessage =
            resources.getString(R.string.login_toast_invalid_email)
        Toast.makeText(
            this,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    fun onLogInButtonClick(view: View) {
        val email = findViewById<EditText>(R.id.login_edittext_email).text.toString()
        val password = findViewById<EditText>(R.id.login_edittext_password).text.toString()
        setScreenLoading(true)
        authViewModel.signInUser(email, password)
    }

    fun onSignUpButtonClick(view: View) {
        val emailEditText = findViewById<EditText>(R.id.login_edittext_email)
        val passwordEditText = findViewById<EditText>(R.id.login_edittext_password)
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        setScreenLoading(true)
        authViewModel.signUpUser(email, password)
    }

    fun onResendActivationEmailButtonClick(view: View) {
        setScreenLoading(true)
        authViewModel.resendVerificationEmail()
    }
}