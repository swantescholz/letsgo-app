package de.sscholz

import android.content.pm.PackageManager
import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editMode = false
        androidMode = true
        val config = AndroidApplicationConfiguration()
//        config.useGyroscope = true  //default is false
//        config.useAccelerometer = true
//        config.useCompass = true
        Global.gpsTrackers.add(MyAndroidGpsTrackerFused(this))
        Global.gpsTrackers.add(MyAndroidGpsTrackerGpsOnly(this))
//        Global.gpsTracker = object : IGpsTracker{}
        Global.gpsTracker.resumeLocationUpdates()
        initialize(App(), config)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MyGpsTrackerBase.MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


}
