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
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import de.sscholz.Global.settings
import de.sscholz.extensions.mySetBackgroundColor
import de.sscholz.util.*
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton
import ktx.scene2d.textField


class SettingsScreen(private val application: App) : MyBaseScreen(), KtxInputAdapter {
    companion object {
        val buttonPadding = 30f
    }

    init {
        Gdx.input.isCatchBackKey = true
    }

    private val uiStage = Stage()
    private val backgroundImage = Texture("bg-main-menu.png")
    private lateinit var root: Table
    private lateinit var meterField: TextField
    private var cheatCounter = 0

    private fun createRootTable() {
        val buttonWidth = screenWidth * 0.4f
        root = table {
            setFillParent(true)

            background = TextureRegionDrawable(TextureRegion(backgroundImage))
            touchable = Touchable.enabled

            align(Align.top)

            label(text = "Settings\n", style = "large") {
                color = Color.GOLD
            }.cell(row = true, pad = 20f)

            table {
                val muteStrings = arrayOf("Sound: On", "Sound: Off")
                table {
                    textButton(muteStrings[Preferences.mute.get().i]) {
                        pad(buttonPadding)
                        this.color = settings.defaultButtonBgColor
                        onClick {
                            Preferences.mute.set(!Preferences.mute.get())
                            Global.switchGpsTrackerImplementation()
                            cheatCounter += 1
                            if (cheatCounter == 10) {
                                Preferences.unlockedLevels.set(100)
                            }
                            this@textButton.setText(muteStrings[Preferences.mute.get().i])
                        }
                    }.cell(width = buttonWidth)

                    val aiPlayStrings = arrayOf("Player VS Player", "Player VS AI")
                    textButton(aiPlayStrings[Preferences.vsAiMode.get().i]) {
                        pad(buttonPadding)
                        this.color = settings.defaultButtonBgColor
                        onClick {
                            Preferences.vsAiMode.set(!Preferences.vsAiMode.get())
                            this@textButton.setText(aiPlayStrings[Preferences.vsAiMode.get().i])
                        }
                    }.cell(row = true, width = buttonWidth)
                }.cell(row = true)

                val modeStrings = arrayOf("Normal GPS mode", "Training mode")
                textButton(modeStrings[Preferences.trainingMode.get().i]) {
                    pad(buttonPadding)
                    this.color = settings.defaultButtonBgColor
                    onClick {
                        Preferences.trainingMode.set(!Preferences.trainingMode.get())
                        this@textButton.setText(modeStrings[Preferences.trainingMode.get().i])
                    }
                }.cell(row = true, width = buttonWidth)


                label("Walking distance per \nsquare in meter:") {
                    height = 200f
                    width = 400f
                    mySetBackgroundColor(Color.DARK_GRAY.apply { a = 0.5f }, padding = 20f)
                }.cell(row = true, padTop = 30f)

                val meterField = textField("${Preferences.cellMeterWidth.get()}") {
                    textFieldFilter = TextField.TextFieldFilter.DigitsOnlyFilter()
                    color = settings.defaultButtonBgColor
                }.cell(row = true, pad = 10f)

                textButton("Save new distance") {
                    pad(buttonPadding)
                    this.color = settings.defaultButtonBgColor
                    onClick {
                        val t = meterField.text.i
                        if (t == null) {
                            Toasts.showToast("Invalid input")
                            meterField.text = Preferences.cellMeterWidth.get().toString()
                        } else {
                            if (t < Preferences.cellMeterWidth.get()) {
                                UiUtil.myDialog(uiStage,
                                        "Are you sure?", """
                                            The new distance is smaller than before.
                                            Are you positive that you want to walk less?
                                        """.trimIndent(), listOf("Yes, I am lazy", "Nope, I'll manage")) {
                                    if (it == 0) {
                                        changeCellDistance(t)
                                    } else {
                                        meterField.text = Preferences.cellMeterWidth.get().toString()
                                    }
                                }
                            } else {
                                changeCellDistance(t)
                            }
                        }
                    }
                }.cell(row = true, width = buttonWidth, padTop = 10f)

                textButton("Back") {
                    pad(buttonPadding)
                    this.color = settings.defaultButtonBgColor
                    onClick {
                        application.setScreen<MainMenuScreen>()
                    }
                }.cell(row = true, width = buttonWidth, padTop = screenHeight * 0.08f)

            }.cell(fill = true, grow = true, expand = true)

        }
    }

    private fun changeCellDistance(t: Int) {
        Preferences.cellMeterWidth.set(t)
        Toasts.showToast("Distance changed")
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE, Input.Keys.BACK -> {
                application.setScreen<MainMenuScreen>()
            }
        }
        return true
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