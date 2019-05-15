package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import de.sscholz.Global.backToMenu
import de.sscholz.Global.levelToLoad
import de.sscholz.ai.BoardState
import de.sscholz.extensions.asGestureDetector
import de.sscholz.util.*
import ktx.app.KtxInputAdapter
import ktx.scene2d.label
import ktx.scene2d.table


class GameScreen(val application: App) : MyBaseScreen() {

    private lateinit var boardState: BoardState
    private val uiStage = Stage()
    private var cameraInputHandler: Camera2InputHandler
    private lateinit var titleBarLabel: Label
    private lateinit var fpsLabel: Label
    private lateinit var boardView: BoardView

    init {
        Global.application = application
        printl("windows size: $screenWidth x $screenHeight")
//        fooFun()
        Gdx.input.isCatchBackKey = true // make sure we cleanly quit GDX when user presses "back"
        GdxUtil.setupOpenGl()
        Sounds.load() // so that sounds are loaded already and first time works immediately
        cameraInputHandler = Camera2InputHandler()
    }


    override fun show() {
        log("GameScreen.show()")
        reset()
    }

    private fun createViews() {
        boardView = BoardView(uiStage, boardState, Global.levelToLoad)
        val viewTop = table {
            setFillParent(true)
            align(Align.topLeft)
            touchable = Touchable.enabled
            pad(0f)
            titleBarLabel = label("<foobar>", style = "withbg") {
                this.setAlignment(Align.center)
            }.cell(row = true, growX = true, height = hudStatusBarHeight)
            boardView.apply { this@table.addUiToTopView() }
        }
        uiStage.addActor(viewTop)
        StatusBar.addToUiState(uiStage)
    }

    private fun reset() {
        Settings.reloadFromConfigFile()
        Preferences.reset()
        boardState = Global.levelToLoad.createBoard()

        uiStage.clear()
        createViews()
        updateTitleText()
        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(uiStage)
//        inputMultiplexer.addProcessor(cameraInputHandler.asGestureDetector())
        inputMultiplexer.addProcessor(cameraInputHandler) // for scrolling events
        inputMultiplexer.addProcessor(boardView)
        inputMultiplexer.addProcessor(inputProcessor.asGestureDetector())
        inputMultiplexer.addProcessor(inputProcessor)
        Gdx.input.inputProcessor = inputMultiplexer
    }


    // updates before rendering
    private fun myUpdate(delta: Float) {
        uiStage.act(delta)
        boardView.update(delta)
    }

    override fun render(delta: Float) {
        GdxUtil.framerateComputer.addDeltaTimeOfCurrentFrame(delta)
        myUpdate(delta)
        camera2.apply()
        boardView.render()
        hudViewport.apply()
        uiStage.draw()
        Toasts.render()
    }


    private val inputProcessor = object : GestureDetector.GestureAdapter(), KtxInputAdapter {

        override fun keyDown(keycode: Int): Boolean {
            val gpsDelta = 0.000009
            when (keycode) {
                Input.Keys.ESCAPE -> {
                    printl("====================EXIT (GDX) =================")
                    quit()
                }
                Input.Keys.BACK, Input.Keys.T -> {
                    backToMenu()
                }
                Input.Keys.R -> {
                    reset()
                    boardView.startNewGame()
                }
                Input.Keys.TAB -> {
                    editMode = !editMode
                    updateTitleText()
                }
                Input.Keys.A -> {
                    Global.gpsTracker.manuallyChangeCurrentLocation(Global.gpsTracker.currentLocation + LatLong(0.0, -2.0 * gpsDelta))
                }
                Input.Keys.D -> {
                    Global.gpsTracker.manuallyChangeCurrentLocation(Global.gpsTracker.currentLocation + LatLong(0.0, 2.0 * gpsDelta))
                }
                Input.Keys.W -> {
                    Global.gpsTracker.manuallyChangeCurrentLocation(Global.gpsTracker.currentLocation + LatLong(gpsDelta, 0.0))
                }
                Input.Keys.S -> {
                    Global.gpsTracker.manuallyChangeCurrentLocation(Global.gpsTracker.currentLocation + LatLong(-gpsDelta, 0.0))
                }
            }
            return true
        }

    }

    private fun updateTitleText() {
        titleBarLabel.setText(levelToLoad.title + if (editMode) " <Edit Mode>" else "")
    }

    override fun hide() {
        log("GameScreen.hide()")
    }

    override fun resize(width: Int, height: Int) {

    }

}

