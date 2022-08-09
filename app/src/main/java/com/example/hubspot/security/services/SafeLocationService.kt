package com.example.hubspot.security.services

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.hubspot.security.viewModel.SecurityViewModel
import java.util.*


/**
 * SafeLocationService is a service that handles the continuous location system including setting
 * the location alarm that goes off in 15-minute intervals and turning the alarm on and off. When
 * the alarm goes off, a timing operation in the form of a [PendingIntent] broadcasts an [Intent]
 * for a [LocationReceiver].
 */
class SafeLocationService {

// Private methods --------------------------------------------------------------------------

    /**
     * Uses an alarm manager to broadcast an intent that will signal the app to get the user's
     * location every 15 minutes.
     */
    private fun setLocationAlarm(activity: Activity) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        // Obtain an alarm service from the system services to create an alarm manager
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Create a new intent of type LocationReceiver
        val intent = Intent(activity, LocationReceiver::class.java)
        // Set the new intent to a pending intent broadcast
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(activity, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT // Handles if the same PendingIntent is already created
            )
        }
        alarmManager.setRepeating( // Sets the alarm manager to be repeating
            AlarmManager.RTC,
            calendar.timeInMillis,
            10L,
            pendingIntent
        )
        Toast.makeText(activity,"Location Services ON.", Toast.LENGTH_SHORT).show()
    }

    /**
     * Turns of the alarm manager alarm to stop broadcasting the app to get the user's location.
     */
    private fun turnOffLocationAlarm(activity: Activity) {
        val intent = Intent(activity, LocationReceiver::class.java)

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(
                activity,
                0,
                intent,
                PendingIntent.FLAG_NO_CREATE
            )
        }
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager!!.cancel(pendingIntent)
        Toast.makeText(activity,"Location Services OFF.", Toast.LENGTH_SHORT).show()
    }

// Public methods ---------------------------------------------------------------------------

    /**
     * Observes if the continuous location system has been activated, sets the location alarm if it
     * has, or turns the location alarm off when the system is deactivated.
     */
    fun handleContinuousLocationSystemActivation(
        activity: Activity,
        owner: LifecycleOwner,
        securityViewModel: SecurityViewModel,
    ) {
        securityViewModel.locationServiceSystemActivated.observe(owner) {
            if (it == true) {
                setLocationAlarm(activity)
            } else {
                turnOffLocationAlarm(activity)
            }
        }
    }
}