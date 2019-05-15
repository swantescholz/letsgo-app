package de.sscholz

import de.sscholz.util.LatLong


// resume must be called at the beginning of the app if you want to use locations
interface IGpsTracker {

    val currentLocation: LatLong

    fun pauseLocationUpdates()
    fun resumeLocationUpdates()

    fun manuallyChangeCurrentLocation(newLocation: LatLong) {}
}