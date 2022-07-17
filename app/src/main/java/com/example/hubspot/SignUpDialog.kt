package com.example.hubspot

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

/** Dialog Fragment for signing up a user account */
class SignUpDialog() : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog

        val builder = AlertDialog.Builder(requireActivity())
        val view: View =
            requireActivity().layoutInflater.inflate(R.layout.dialog_signup, null)
        builder.setView(view)

        val email = view.findViewById<EditText>(R.id.dialog_signup_edittext_email).text.toString()
        val firstPassword =
            view.findViewById<EditText>(R.id.dialog_signup_edittext_first_password).text.toString()
        val secondPassword =
            view.findViewById<EditText>(R.id.dialog_signup_edittext_second_password).text.toString()

        builder.setPositiveButton(
            resources.getString(R.string.sign_up_dialog_positive_button)
        ) { _, _ ->
            onPositiveButtonClick(email, firstPassword, secondPassword)
        }

        builder.setNegativeButton(
            resources.getString(R.string.sign_up_dialog_negative_button),
            null
        )

        ret = builder.create()
        return ret
    }

    private fun onPositiveButtonClick(
        email: String,
        firstPassword: String,
        secondPassword: String
    ) {
        if (firstPassword != secondPassword) {
            val bothPasswordsMustMatchError =
                resources.getString(R.string.dialog_signup_toast_both_passwords_must_match)
            Toast.makeText(requireActivity(), bothPasswordsMustMatchError, Toast.LENGTH_LONG)
                .show()
            return
        }
        (requireActivity() as LoginActivity).onSignUpDialogFinishButtonClick(email, firstPassword)
    }
}