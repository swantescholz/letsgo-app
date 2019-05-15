package de.sscholz

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import de.sscholz.util.LatLong
import de.sscholz.util.Toasts
import de.sscholz.util.printl

class MyAndroidGpsTrackerGpsOnly(androidActivity: Activity) : MyGpsTrackerBase(androidActivity), LocationListener {

    override fun pauseLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    override fun resumeLocationUpdates() {
        printl("resumeLocationUpdates")
        locationManager.requestLocationUpdates(provider,
                MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE.toFloat(), this)
    }

    protected var locationManager: LocationManager = androidActivity.applicationContext
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val provider = LocationManager.GPS_PROVIDER

    init {
        requestPermissions()
        if (!locationManager.isProviderEnabled(provider)) {
            Toasts.showToast("GPS Provider not enabled.", Toasts.Duration.LONG)
        }
    }

    override val currentLocation: LatLong
        @SuppressLint("MissingPermission")
        get() {
            if (locationManager.isProviderEnabled(provider)) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    return LatLong(location.latitude, location.longitude)
                }
            }
            return LatLong(0.0, 0.0)
        }

    override fun onLocationChanged(location: Location) {
        printl("location changed: $location")
    }

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun toString(): String {
        return "GPS Only Tracker"
    }
}