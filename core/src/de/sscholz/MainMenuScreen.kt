package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import de.sscholz.Global.appName
import de.sscholz.Global.settings
import de.sscholz.util.*
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton


class MainMenuScreen(private val application: App) : MyBaseScreen(), KtxInputAdapter {
    companion object {
        val buttonPadding = 30f
    }

    init {
        Gdx.input.isCatchBackKey = true
    }

    private val uiStage = Stage()
    private val backgroundImage = Texture("bg-main-menu.png")
    private lateinit var root: Table
    private var cheatCounter = 0

    private fun createRootTable() {
        root = table {
            setFillParent(true)

            background = TextureRegionDrawable(TextureRegion(backgroundImage))
            touchable = Touchable.enabled

            align(Align.center)
//            val muteStrings = arrayOf("Mute", "Unmute")
//            textButton(muteStrings[Preferences.mute.get().i]) {
//                pad(buttonPadding)
//                this.color = settings.defaultButtonBgColor
//                onClick {
//                    Preferences.mute.set(!Preferences.mute.get())
//                    cheatCounter += 1
//                    if (cheatCounter == 10) {
//                        Preferences.unlockedLevels.set(100)
//                        this@MainMenuScreen.hide()
//                        this@MainMenuScreen.show()
//                    }
//                    this@textButton.setText(muteStrings[Preferences.mute.get().i])
//                }
//            }.cell(row = true, align = Align.topRight, width = 200f)

            align(Align.center)
            label(text = "$appName - Menu\n", style = "large") {
                height = 100f
                color = Color.GOLD
            }.cell(row = true, pad = 20f)
            table {
                textButton("How to play", style = "large") {
                    pad(buttonPadding)
                    this.color = settings.defaultButtonBgColor
                    onClick { showHowToPlayDialog() }
                }.cell()
                textButton("Settings", style = "large") {
                    pad(buttonPadding)
                    this.color = settings.defaultButtonBgColor
                    onClick { application.setScreen<SettingsScreen>() }
                }.cell()
                textButton("Credits", style = "large") {
                    pad(buttonPadding)
                    this.color = settings.defaultButtonBgColor
                    onClick { showCreditsDialog() }
                }.cell()
            }.cell(row = true, padBottom = 40f)
            table {
                align(Align.top)

                for (levelIndex in 0 until Level.values().size) {
                    val level = Level.values()[levelIndex]
                    val button = textButton(level.title, style = "default") {
                        this.color = settings.defaultButtonBgColor
                        if (levelIndex >= Preferences.unlockedLevels.get()) {
                            this.touchable = Touchable.disabled
                            this.color = disabledButtonColor
                            setText("<locked>")
                        }
                        onClick {
                            try {
                                Preferences.lastPlayedLevelId.set(levelIndex)
                                application.setScreen<GameScreen>()
                            } catch (t: Throwable) {
                                application.setScreen<MainMenuScreen>()
                                log(t)
                                t.printStackTrace()
                                Toasts.showToast("Level could not be loaded.")
                            }
                        }
                        pad(buttonPadding)
                    }
                    button.cell(row = (levelIndex + 1) % 2 == 0, grow = true, expand = true, fill = true)
                }
            }.cell(padBottom = 50f, expand = true)
        }
    }

    private fun showHowToPlayDialog() {
        UiUtil.myDialog(uiStage,
                "How to play", LongTexts.howToPlay, listOf("Close"),
                closeOnClickOutside = true, scrollable = true)
    }

    private fun showCreditsDialog() {
        UiUtil.myDialog(uiStage,
                "Credits", LongTexts.credits, listOf("Close"),
                closeOnClickOutside = true, scrollable = true)
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE, Input.Keys.BACK -> quit()
        }
        return false
    }

    override fun show() {
        createRootTable()
        uiStage.addActor(root)
        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(uiStage)
        inputMultiplexer.addProcessor(this)
        Gdx.input.inputProcessor = inputMultiplexer
        cheatCounter = 0
    }

    override fun render(delta: Float) {
        uiStage.act(delta)
        uiStage.draw()
        Toasts.render()
    }

    override fun hide() {
        root.remove()
    }
}