package com.example.hubspot.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.hubspot.R
import com.example.hubspot.login.LoginActivity


/** Dialog Fragment for signing up a user account */
class SignUpDialog() : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog

        val builder = AlertDialog.Builder(requireActivity())
        val view: View =
            requireActivity().layoutInflater.inflate(R.layout.dialog_signup, null)
        builder.setView(view)

        val newName =
            view.findViewById<EditText>(R.id.dialog_signup_edittext_email).text.toString()

        // Show positive button, will override behavior later
        builder.setPositiveButton(
            resources.getString(R.string.sign_up_dialog_positive_button)
        ) { _, _ ->

        }

        builder.setNegativeButton(
            resources.getString(R.string.sign_up_dialog_negative_button),
            null
        )

        ret = builder.create()
        ret.setOnShowListener {
            val button: Button =
                ret.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                // Set signUpDialog reference to allow view model observer in login activity
                // to dismiss dialog once sign up is successful to avoid closing dialog on errors
                (requireActivity() as LoginActivity).signUpDialog = ret

                val email =
                    view.findViewById<EditText>(R.id.dialog_signup_edittext_email).text.toString()
                val firstPassword =
                    view.findViewById<EditText>(R.id.dialog_signup_edittext_first_password).text.toString()
                val secondPassword =
                    view.findViewById<EditText>(R.id.dialog_signup_edittext_second_password).text.toString()

                // disable button to stop accidental multiple presses, re-enable in login activity
                button.isEnabled = false

                onPositiveButtonClick(email, firstPassword, secondPassword, ret)
            }
        }
        return ret
    }

    private fun onPositiveButtonClick(
        email: String,
        firstPassword: String,
        secondPassword: String,
        dialog: Dialog,
    ) {
        if (firstPassword != secondPassword) {
            val bothPasswordsMustMatchError =
                resources.getString(R.string.dialog_signup_toast_both_passwords_must_match)
            Toast.makeText(requireActivity(), bothPasswordsMustMatchError, Toast.LENGTH_LONG)
                .show()

            // re-enable positive dialog button to allow user to try again now
            val positiveButton: Button =
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = true

            return
        }
        (requireActivity() as LoginActivity).onSignUpDialogFinishButtonClick(email, firstPassword)
    }
}