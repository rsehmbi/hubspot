package com.example.hubspot.security.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.hubspot.security.ui.SecurityFragment

/**
 * LocationReceiver extends the abstract [BroadcastReceiver] class. It overrides [onReceive] and
 * sets the [SecurityFragment]'s view model location value to true. Setting this flag via the view
 * model allows the observer of the flag to understand that it is time to get a location update.
 */
class LocationReceiver() : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        SecurityFragment.securityViewModel.getLocationNow.value = true
    }
}