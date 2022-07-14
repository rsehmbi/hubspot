package com.example.hubspot.services

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener


/**
 * LocationService acts as a singleton service to provide the application with services pertaining
 * to user location such as getting the current location.
 */
object LocationService {

    /**
     * Get the current location of the user with Google Location Services
     * FusedLocationProviderClient. Checks location action permissions before obtaining the user's
     * current location. In the event where the user denys permissions or a location cannot be
     * found it will return null, otherwise it will return a location of type Location.
     * Takes in an activity.
     */
    fun getCurrentLocation(activity: Activity) : Location? {
        var currentLocation: Location? = null
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            var fineLocationResult = -1
            var coarseLocationResult = -1
            ActivityCompat.OnRequestPermissionsResultCallback { requestCode, permissions, grantResults ->
                fineLocationResult = grantResults[0]
                coarseLocationResult = grantResults[1]
            }
            if (fineLocationResult == PERMISSION_DENIED || coarseLocationResult == PERMISSION_DENIED) {
                return null
            }
        }

        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                currentLocation = location
            }
        return currentLocation
    }
}