package de.sscholz

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import de.sscholz.Global.isGpsEnabled
import de.sscholz.util.LatLong
import de.sscholz.util.Toasts
import de.sscholz.util.log
import de.sscholz.util.printl

class MyAndroidGpsTrackerFused(androidActivity: Activity) : MyGpsTrackerBase(androidActivity) {

    override var currentLocation: LatLong = LatLong(0.0, 0.0)
        protected set

    val locationRequest = LocationRequest.create().apply {
        interval = MIN_TIME_FOR_UPDATE * 2
        fastestInterval = MIN_TIME_FOR_UPDATE
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            val myPreviousLocation = currentLocation
            currentLocation = locationResult.lastLocation.let { LatLong(it.latitude, it.longitude) }
            var msg = "location update: $currentLocation"
            if (currentLocation == myPreviousLocation) {
                msg = "location updated, but unchanged"
            }
            printl(msg)
//            Toasts.showToast(msg, Toasts.Duration.SHORT)
        }
    }

    init {
        requestPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(androidActivity)
        prepareLocationUpdates()
    }

    override fun pauseLocationUpdates() {
        log("location updates paused")
        if (isGpsEnabled) {
            log("location updates paused <if true>")
            removeCallback()
        }
    }

    private fun removeCallback() {
        log("location updates removed callback")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isGpsEnabled = false
    }

    override fun resumeLocationUpdates() {
        log("location updates resume")
        if (!isGpsEnabled) {
            log("location updates resume <enabled if>")
            addCallback()
        }
    }

    @SuppressLint("MissingPermission")
    private fun addCallback() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        isGpsEnabled = true
//        currentLocation = fusedLocationClient.
    }

    private fun prepareLocationUpdates() {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(androidActivity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            Toasts.showToast("=== Location Settings Success ===", Toasts.Duration.LONG)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                Toasts.showToast("Location Settings failure. Please enable GPS in your device settings.", Toasts.Duration.LONG)
            }
        }
    }

    override fun toString(): String {
        return "Normal GPS Tracker"
    }


}