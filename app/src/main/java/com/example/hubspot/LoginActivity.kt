package com.example.hubspot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hubspot.services.Auth


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onLogInButtonClick(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onSignUpButtonClick(view: View) {
        val emailEditText = findViewById<EditText>(R.id.login_edittext_email)
        val passwordEditText = findViewById<EditText>(R.id.login_edittext_password)
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        val result = Auth.sendAccountActivationEmail(email, password)

        if (result == Auth.SendAccountActivationEmailResult.SUCCESS) {
            Toast.makeText(this, "Check your email to activate your account!", Toast.LENGTH_LONG)
                .show()
        } else {
            Toast.makeText(
                this,
                "Failed to send account creation email. Error: $result",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }
}