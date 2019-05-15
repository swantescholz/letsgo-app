package de.sscholz.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import de.sscholz.extensions.mySetBackgroundColor
import ktx.scene2d.label
import ktx.scene2d.table

// on the bottom of the screen, has to be added to UIStaage
object StatusBar {

    private lateinit var textLabel: Label
    private val root by lazy {
        table {
            setFillParent(true)
            align(Align.bottom)
            table {
                pad(0f)
                textLabel = label("Status: ", "withbg") {
                    mySetBackgroundColor(Color.DARK_GRAY.apply { a = 0.5f }, padding = uiLabelBorderPadding)
                }.cell(growX = true, height = hudStatusBarHeight)
            }.cell(growX = true)
        }
    }

    fun addToUiState(uiStage: Stage) {
        uiStage.addActor(root)
    }

    fun setStatusMessage(newStatusMessage: String) {
        root // init label
        textLabel.setText(newStatusMessage)
    }
}