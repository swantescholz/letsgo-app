package de.sscholz

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import de.sscholz.util.Preferences
import de.sscholz.util.Toasts
import kotlinx.serialization.json.Json
import ktx.freetype.registerFreeTypeFontLoaders

var editMode: Boolean = true
var androidMode: Boolean = false
val releaseMode: Boolean = false
var debugLogging: Boolean = true

object Global {
    val appName = "Let's Go"
    val levelToLoad: Level
        get() = Level.values()[Preferences.lastPlayedLevelId.get()]
    val assetManager by lazy { AssetManager().apply { registerFreeTypeFontLoaders() } }
    val defaultSpriteBatch by lazy { SpriteBatch() }
    val shapeRenderer by lazy { ShapeRenderer() }
    val gpsTracker: IGpsTracker
        get() = gpsTrackers[currentGpsTrackerIndex]
    private var currentGpsTrackerIndex: Int = 0
    val gpsTrackers: ArrayList<IGpsTracker> = ArrayList()
    lateinit var application: App
    private var _settings: Settings? = null
    var isGpsEnabled = false
    var settings: Settings
        get() {
            if (_settings == null) {
                _settings = Settings.reloadFromConfigFile(false)
            }
            return _settings!!
        }
        set(value) {
            _settings = value
        }

    fun backToMenu() {
        Preferences.lastPlayedLevelId.set(-1)
        application.setScreen<MainMenuScreen>()
    }

    fun switchToGameOrMenuScreen() {
        if (Preferences.lastPlayedLevelId.get() == -1) {
            application.setScreen<MainMenuScreen>()
        } else {
            application.setScreen<GameScreen>()
        }
    }

    fun switchGpsTrackerImplementation() {
        gpsTracker.pauseLocationUpdates()
        currentGpsTrackerIndex++
        currentGpsTrackerIndex %= gpsTrackers.size
        gpsTracker.resumeLocationUpdates()
        Toasts.showToast("new GPS-Tracker: $gpsTracker")
    }

}

val myjson = Json(indented = true, indent = "\t", unquoted = true)