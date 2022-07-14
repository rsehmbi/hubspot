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
     * Interface can be used to provide a callback function for getting the current location.
     * Example usage:
     *  LocationService.getCurrentLocation(activity,
     *       object : LocationCallback {
     *           override fun onCallback(result: Location?) {
     *               // Do something with location result
     *           }
     *       }
     *   })
     */
    interface LocationCallback {
        fun onCallback(result: Location?)
    }

    /**
     * Get the current location of the user with Google Location Services
     * [FusedLocationProviderClient]. Checks location action permissions before obtaining the user's
     * current location. Once the location has been successfully obtained, it calls the
     * [LocationCallback] passing in the result. Requires an [Activity] and [LocationCallback].
     */
    fun getCurrentLocation(activity: Activity, locationCallback: LocationCallback) {
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
            }
        }

        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                locationCallback.onCallback(location)
            }
    }
}