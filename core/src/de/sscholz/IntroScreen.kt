package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable.enabled
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import de.sscholz.extensions.asFittingBackground
import de.sscholz.util.Toasts
import de.sscholz.util.quit
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.scene2d.label
import ktx.scene2d.table

class IntroScreen(private val application: App) : MyBaseScreen(), KtxInputAdapter {
    private val uiStage = Stage()
    private val backgroundImage = Texture("bg-intro.png").asFittingBackground()
    private lateinit var loadingLabel: Label
    private val root = table {
        setFillParent(true)

        background = TextureRegionDrawable(TextureRegion(backgroundImage))
        touchable = enabled
        onClick {
            Global.switchToGameOrMenuScreen()
        }
        align(Align.center)
        table {
            label(text = Global.appName, style = "huge") {
                color = Color.GOLD
            }.cell(row = true, pad = 25f)
            label(text = "Gomoku, Gobang, Capture Go\nand many other games to\nplay while walking", style = "large") {
                color = Color.GOLDENROD
            }.cell(row = true, pad = 15f)
            loadingLabel = label(text = "Tap to Go", style = "large") {
                color = Color.GOLDENROD
            }.cell(row = true, padTop = 100f)
        }.cell()
    }

    init {
        Gdx.input.isCatchBackKey = true
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE, Input.Keys.BACK -> quit()
        }
        return false
    }

    override fun show() {
        uiStage.addActor(root)
        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(uiStage)
        inputMultiplexer.addProcessor(this)
        Gdx.input.inputProcessor = inputMultiplexer
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


