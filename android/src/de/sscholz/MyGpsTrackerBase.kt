package de.sscholz

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

abstract class MyGpsTrackerBase(val androidActivity: Activity) : IGpsTracker {

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 42
        val MIN_DISTANCE_FOR_UPDATE: Long = 10
        val MIN_TIME_FOR_UPDATE = (1000 * 2).toLong()
    }

    val isLocationPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(androidActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    protected fun requestPermissions() {
        // Here, androidActivity is the current activity
        if (!isLocationPermissionGranted) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(androidActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(androidActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)

                // MY_PERMISSIONS_REQUEST_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }
}