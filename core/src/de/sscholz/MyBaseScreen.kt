package de.sscholz

import de.sscholz.util.log
import ktx.app.KtxScreen

open class MyBaseScreen : KtxScreen {
    override fun pause() {
        super.pause()
        log("MyBaseScreen.pause()")
        Global.gpsTracker.pauseLocationUpdates()
    }

    override fun resume() {
        super.resume()
        log("MyBaseScreen.resume()")
        Global.gpsTracker.resumeLocationUpdates()
    }
}