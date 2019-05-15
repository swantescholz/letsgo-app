package de.sscholz.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import de.sscholz.Global.settings
import de.sscholz.extensions.asDrawable
import ktx.actors.onKey
import ktx.actors.onKeyDown
import ktx.assets.toInternalFile
import ktx.scene2d.Scene2DSkin
import ktx.style.*


object UiUtil {

    fun quitIfEscapeIsPressed() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            quit()
        }
    }


    // creates my default dialog, with wrapped message label
    // can also be scrollable
    // callback gets the object associated with the button, and the dialog
    fun myDialog(uiStage: Stage,
                 title: String, longWrappableMessage: String,
                 buttonNames: List<String>,
                 closeOnClickOutside: Boolean = false,
                 scrollable: Boolean = false, scrollPaneHeight: Float = howToPlayScrollPaneHeight,
                 onResult: (Int) -> Unit = { _ -> }) {
        object : Dialog(title, Scene2DSkin.defaultSkin) {
            override fun result(`object`: Any?) {
                this.remove()
                onResult(`object` as Int)
            }
        }.apply {
            this.contentTable.align(Align.left)
            this.buttonTable.align(Align.center)

            this.buttonTable.defaults().height(dialogButtonHeight)
            this.buttonTable.defaults().width(dialogButtonMinWidth)
            this.buttonTable.defaults().padTop(dialogButtonTopPadding)
            onKeyDown { key ->
                if (key == Input.Keys.BACK) {
                    this.remove()
                }
            }
            onKey {
                if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    this.remove()
                }
            }
            if (closeOnClickOutside) {
                addListener(object : InputListener() {
                    override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                        if (x < 0 || x > width || y < 0 || y > height) {
                            remove()
                        }
                        return true
                    }
                })
            }
            pad(dialogPaddingNormal)
            padTop(dialogPaddingTop)

            val label = Label(longWrappableMessage, Scene2DSkin.defaultSkin)
            label.setWrap(true)
            label.width = 10f
            if (scrollable) {
                val scrollPane = ScrollPane(label)
                contentTable.add(scrollPane).maxHeight(scrollPaneHeight).width(maxRelativeDialogWidth * screenWidth)
            } else {
                contentTable.add(label).width(maxRelativeDialogWidth * screenWidth)
            }
            for (i in 0 until buttonNames.size) {
                button(buttonNames[i], i)
            }
        }.show(uiStage)
    }

    fun createMyDefaultSkin(): Skin {
        val skin = Skin("uiskin.json".toInternalFile())
        skin.add("rotate-right", Texture("rotate-right.png"))
//        skin.add("rotate-left", Texture("rotate-left.png"))
        skin.add("info", Texture("info.png"))
        skin.add("new", Texture("new.png"))
        skin.add("center", Texture("center.png"))
        skin.label("default", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(24)
        }
        skin.label("withbg", extend = "default") {
            background = Color.SALMON.asDrawable()
        }
        skin.label("decorative") {
            font = FontFactory.Decorative.getOrLoadFontWithSize(64)
        }
        skin.label("huge", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(84)
        }
        skin.label("large", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(36)
        }
        skin.textButton("default", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(24)
            this.fontColor = settings.defaultButtonTextColor
        }
        skin.textButton("large", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(36)
        }
        skin.selectBox("default", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(32)
            background.leftWidth = 10f
            listStyle.font = FontFactory.Default.getOrLoadFontWithSize(30)
            this.fontColor = settings.defaultButtonTextColor
            this.listStyle.fontColorUnselected = settings.defaultButtonTextColor
            listStyle.selection.leftWidth = 10f
            listStyle.selection.topHeight = selectBoxListExtraTopBottomHeight
            listStyle.selection.bottomHeight = selectBoxListExtraTopBottomHeight

        }
        skin.textField("default", extend = "default") {
            this.fontColor = Color.SALMON
            font = FontFactory.Default.getOrLoadFontWithSize(36)
            background.leftWidth = 20f
        }
        skin.window("default", extend = "default") {
            titleFont = FontFactory.Default.getOrLoadFontWithSize(24)
            titleFontColor = Color.GOLD
            this.background = Color.DARK_GRAY.asDrawable()
        }
        return skin
    }

    fun playMusic(internalFilePath: String) {
        Gdx.audio.newMusic(internalFilePath.toInternalFile()).apply {
            volume = 0.3f
            setOnCompletionListener { play() }
        }.play()
    }
}