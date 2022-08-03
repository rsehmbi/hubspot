package com.example.hubspot.security.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.hubspot.security.ui.SecurityFragment

/**
 * SilentButtonReceiver extends the abstract [BroadcastReceiver] class. It overrides [onReceive] and
 * sets the [SecurityFragment]'s view model key button action, key button keycode, and silent button
 * pressed values. Setting the silent button pressed flag to true allows the observer of the flag to
 * understand that it is time to get the key button action and key button keycode updates.
 */
class SilentButtonReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val action = intent.getIntExtra("action", -1)
        val keycode = intent.getIntExtra("keyCode", -1)
        SecurityFragment.securityViewModel.keyEventButtonAction.value = action
        SecurityFragment.securityViewModel.keyEventButtonKeyCode.value = keycode
        SecurityFragment.securityViewModel.silentButtonPressed.value = true
    }
}