package com.example.hubspot.profile

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.hubspot.R
import com.example.hubspot.auth.AuthRepository
import com.example.hubspot.auth.AuthViewModel
import com.example.hubspot.login.LoginActivity


/** Dialog Fragment for signing up a user account */
class NameDialog() : DialogFragment() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var dialogView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog

        val builder = AlertDialog.Builder(requireActivity())
        dialogView = requireActivity().layoutInflater.inflate(R.layout.dialog_update_name, null)
        builder.setView(dialogView)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Wait for and handle update name result
        authViewModel.updateDisplayNameResult.observe(this) { result ->
            // if statement is used to stop code from executing on rotation change
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (result.resultCode == AuthRepository.AuthResultCode.SUCCESS) {
                    displaySuccessMessage()
                    (requireActivity() as ProfileActivity).refreshDisplayNameText()
                    this@NameDialog.dismiss()
                } else {
                    displayErrorMessage()
                }
                setLoading(false)
            }
        }

        // Show positive button, will override behavior later
        builder.setPositiveButton(
            resources.getString(R.string.sign_up_dialog_positive_button), null
        )

        builder.setNegativeButton(
            resources.getString(R.string.sign_up_dialog_negative_button),
            null
        )

        ret = builder.create()
        ret.setOnShowListener {
            val button: Button =
                ret.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                // disable button to stop accidental multiple presses, re-enable in login activity
                button.isEnabled = false
                setLoading(true)

                val newName =
                    dialogView.findViewById<EditText>(R.id.dialog_update_name_edittext).text.toString()
                onPositiveButtonClick(newName, ret)
            }
        }
        return ret
    }

    private fun displayErrorMessage() {
        val errorMessage =
            resources.getString(R.string.dialog_update_name_toast_error)
        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_LONG)
            .show()
    }

    private fun displaySuccessMessage() {
        val successMessage =
            resources.getString(R.string.dialog_update_name_toast_success)
        Toast.makeText(requireActivity(), successMessage, Toast.LENGTH_LONG)
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            val loadingSpinner =
                dialogView.findViewById<ProgressBar>(R.id.dialog_update_name_loading_spinner)
            loadingSpinner.visibility = View.VISIBLE
        } else {
            val loadingSpinner =
                dialogView.findViewById<ProgressBar>(R.id.dialog_update_name_loading_spinner)
            loadingSpinner.visibility = View.GONE
        }
    }

    private fun onPositiveButtonClick(
        newName: String,
        dialog: Dialog,
    ) {
        if (newName.length > 50) {
            displayNameTooLongError()

            // re-enable positive dialog button to allow user to try again now
            val positiveButton: Button =
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = true

            return
        }

        authViewModel.updateUserDisplayName(newName)
    }

    private fun displayNameTooLongError() {
        val errorMessage =
            resources.getString(R.string.dialog_update_name_toast_name_too_long)
        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_LONG)
            .show()
    }
}