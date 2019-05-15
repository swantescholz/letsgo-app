package de.sscholz

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import de.sscholz.Global.appName
import de.sscholz.util.LatLong

object DesktopLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        val cfg = LwjglApplicationConfiguration()
        cfg.title = appName
        cfg.height = 1000
        cfg.width = 800
        Global.gpsTrackers.add(object : IGpsTracker {
            override fun resumeLocationUpdates() {
            }

            override fun pauseLocationUpdates() {
            }

            private var myLocation = LatLong(0.0, 0.0)
            override val currentLocation: LatLong
                get() = myLocation

            override fun manuallyChangeCurrentLocation(newLocation: LatLong) {
                myLocation = newLocation
            }
        })
        LwjglApplication(App(), cfg)
    }
}
