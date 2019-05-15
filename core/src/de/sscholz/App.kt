package de.sscholz

import com.badlogic.gdx.Screen
import de.sscholz.ai.aiTest
import de.sscholz.util.UiUtil
import de.sscholz.util.quit
import ktx.app.KtxGame
import ktx.scene2d.Scene2DSkin

class App : KtxGame<Screen>() {

    private fun doExperiments() {
        aiTest()
        quit()
    }

    override fun create() {
//        doExperiments()
        Scene2DSkin.defaultSkin = UiUtil.createMyDefaultSkin()

        val game = GameScreen(this)
        val mainMenu = MainMenuScreen(this)
        val settings = SettingsScreen(this)
        val intro = IntroScreen(this)
        addScreen(game)
        addScreen(mainMenu)
        addScreen(intro)
        addScreen(settings)
        if (editMode) {
            Global.switchToGameOrMenuScreen()
//            setScreen<IntroScreen>()
        } else {
            setScreen<IntroScreen>()
        }
    }

}
