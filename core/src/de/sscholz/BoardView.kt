package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import de.sscholz.Global.backToMenu
import de.sscholz.Global.settings
import de.sscholz.Global.shapeRenderer
import de.sscholz.ai.BoardState
import de.sscholz.ai.BoardState.Companion.Draw
import de.sscholz.ai.BoardState.Companion.Empty
import de.sscholz.ai.BoardState.Companion.Player1
import de.sscholz.ai.BoardState.Companion.Player2
import de.sscholz.extensions.drawRectLine
import de.sscholz.util.*
import de.sscholz.util.GdxUtil.drawTextureWithGivenHeight
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.graphics.use
import ktx.math.*
import ktx.scene2d.KTableWidget
import ktx.scene2d.button
import ktx.scene2d.image
import ktx.scene2d.table

class BoardView(val uiStage: Stage, val board: BoardState, val level: Level) : GestureDetector.GestureAdapter(), KtxInputAdapter {

    companion object {
        const val cellWidth = 10f
        val invalidCoord = Coordi(-1,-1)
    }

    private var compassRotation: Int = 0 // 0 = north, 1 = west, ...
    private var playerGpsCell: Coordi = invalidCoord
    private var selectedCell: Coordi? = null
    private val textTextures = HashMap<Coordi, TextureRegion>()
    val boardWidth = cellWidth * board.size
    private val gpsManager = GpsManager(board.size)
    private var lastAIMove = invalidCoord
    private var lastHumanMove = invalidCoord


    init {
        for (x in 0 until board.size) {
            for (y in 0 until board.size) {
                val text = coordsToLabel(x, y)
                textTextures[Coordi(x, y)] = FontFactory.Default.renderToTexture(text, 42)
            }
        }
        reenter()
        camera2.setNewViewportWorldWidth(vec2(boardWidth, boardWidth) * 0.5f, boardWidth * 1.05f)
    }

    private fun coordsToLabel(x: Int, y: Int) = "${ALPHABET_UPPER[x]}${y + 1}"

    private fun ShapeRenderer.renderCellNormal(x: Int, y: Int, selected: Boolean) {
        val center = vec2((x + 0.5f) * cellWidth, (y + 0.5f) * cellWidth)
        val hs = cellWidth * 0.5f
        color = if ((x + y) % 2 == 1) settings.cellColor1 else settings.cellColor2
        if (selected) {
            color = settings.selectedCellColor
        } else if (playerGpsCell == Coordi(x, y) && !Preferences.trainingMode.get()) {
            color = settings.cellWithMarkerOnItColor
        }
        Gdx.gl.glLineWidth(settings.defaultLineWidth)
        identity()
        use(ShapeRenderer.ShapeType.Filled) {
            rect(center.x - hs, center.y - hs, cellWidth, cellWidth)
        }
        use(ShapeRenderer.ShapeType.Line) {
            color = Color.DARK_GRAY
            rect(center.x - hs, center.y - hs, cellWidth, cellWidth)
        }
    }

    private fun ShapeRenderer.renderCellContent(x: Int, y: Int) {
        val center = vec2((x + 0.5f) * cellWidth, (y + 0.5f) * cellWidth)
        val hs = cellWidth * 0.5f
        Gdx.gl.glLineWidth(settings.defaultLineWidth)
        when (board[x, y]) {
            Empty -> {
                val texture = this@BoardView.textTextures[Coordi(x, y)]!!
                val textUnitHeight = cellWidth * settings.relativeCharacterHeight
                drawTextureWithGivenHeight(texture, textUnitHeight, center - vec2(0f, 0.12f * textUnitHeight))
            }
            Player1, Player2 -> {
                color = if (board[x, y] == Player1) settings.player1Color else settings.player2Color
                use(ShapeRenderer.ShapeType.Filled) {
                    circle(center.x, center.y, hs * 0.8f, settings.defaultCircleSegments)
                }
                if (Coordi(x, y) == lastAIMove || Coordi(x, y) == lastHumanMove) {
                    Gdx.gl.glLineWidth(settings.thickLineWidth * 1.6f)
                    use(ShapeRenderer.ShapeType.Line) {
                        color = settings.lastMoveFrameColor
                        circle(center.x, center.y, hs * 0.8f, settings.defaultCircleSegments)
                    }
                    Gdx.gl.glLineWidth(settings.defaultLineWidth)
                }
            }
            else -> {
                error("bad cell value")
            }
        }
    }

    private fun ShapeRenderer.renderHexLine(x: Int, y: Int) {
        if (!board.isHexagonalGame()) {
            return
        }
        if (x >= board.size - 1 || y <= 0) {
            return
        }
        val start = vec2((x + 0.5f + settings.hexLineOffset) * cellWidth, (y + 0.5f - settings.hexLineOffset) * cellWidth)
        val end = vec2((x + 1.5f - settings.hexLineOffset) * cellWidth, (y - 0.5f + settings.hexLineOffset) * cellWidth)
        this.drawRectLine(start, end, settings.hexLineColor, settings.hexLineWidthFactor * cellWidth)
    }

    private fun ShapeRenderer.renderCellFrame(x: Int, y: Int) {
        val center = vec2((x + 0.5f) * cellWidth, (y + 0.5f) * cellWidth)
        val hs = cellWidth * 0.5f
        val selectionScaleFactor = 1.1f
        color = Global.settings.defaultSelectionColor
        identity()
        translate(center.x, center.y, -1.01f)
        scale(selectionScaleFactor, selectionScaleFactor, 1.0f)
        Gdx.gl.glLineWidth(Global.settings.thickLineWidth)
        use(ShapeRenderer.ShapeType.Line) {
            rect(-hs, -hs, cellWidth, cellWidth)
        }
        identity()
        Gdx.gl.glLineWidth(Global.settings.defaultLineWidth)
    }

    private inline fun xyUntilSize(f: (Int, Int) -> Unit) {
        for (x in 0 until board.size) {
            for (y in 0 until board.size) {
                f(x, y)
            }
        }
    }

    fun render() {
        camera2.rotate(-90f * compassRotation)
        camera2.apply()
        GdxUtil.withGlBlendingEnabled {
            with(shapeRenderer) {
                xyUntilSize { x, y ->
                    val isSelected = Coordi(x, y) == selectedCell
                    renderCellNormal(x, y, isSelected)
                }
                xyUntilSize { x, y ->
                    renderHexLine(x, y)
                }
                xyUntilSize { x, y ->
                    renderCellContent(x, y)
                }
                selectedCell?.let {
                    renderCellFrame(it.x, it.y)
                }
            }
            if (!Preferences.trainingMode.get()) {
                renderGpsLocation()
            }
        }
    }

    private fun renderGpsLocation() {
        with(shapeRenderer) {
            use(ShapeRenderer.ShapeType.Filled) {
                color = settings.locationMarkerColor
                val xy = gpsManager.getPositionInBoardCoordinates()
                circle(xy.x, xy.y, cellWidth * settings.relativeLocationMarkerSize * 0.5f, settings.defaultCircleSegments)
            }
        }
    }

    fun update(delta: Float) {
        gpsManager.updatePositionViaGps(compassRotation)
        val xy = gpsManager.getPositionInBoardCoordinates()
        playerGpsCell = gpsManager.boardCoordToMove(xy)
        if (androidMode && !releaseMode) {
            doRegularly("toast gps delta meter", 5.0, runFunctionOnFirstCall = false) {
                Toasts.showToast(gpsManager.getPositionInBoardCoordinates().toString())
//                Toasts.showToast(Global.gpsTracker.currentLocation.toString())
            }
        }
    }


    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val (x, y) = camera2.screenToWorldCoordinates(screenX.toFloat(), screenY.toFloat())
        if (x !in 0f..board.size * cellWidth || y !in 0f..board.size * cellWidth) {
            return false
        }
        var xyRotated = vec2(x, y) - vec2(1f, 1f) * boardWidth * 0.5f
        compassRotation.times { xyRotated.rotate90(1) }
        xyRotated = xyRotated + vec2(1f, 1f) * boardWidth * 0.5f
        val (ix, iy) = gpsManager.boardCoordToMove(xyRotated)
        if (selectedCell != null || board.isGameOver() || board[ix, iy] != Empty) {
            return false
        }

        when (button) {
            Input.Buttons.LEFT -> {
                selectedCell = Coordi(ix, iy)
                return true
            }
        }
        return false
    }

    private fun computeAndExecuteAiMove(): Coordi {
        val aiMove = level.getAndExecuteBestMove(board)
        return Coordi(aiMove % board.size, aiMove / board.size)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            Input.Buttons.LEFT -> {
                if (selectedCell != null) {
                    if (selectedCell!!.toIndex(board.size) in board.availables) {
                        makePlayerAndMaybeAiMove(selectedCell!!)
                    } else {
                        Toasts.showToast("Invalid move.")
                    }
                    selectedCell = null
                    return true
                }
            }
        }
        return false
    }

    fun saveCurrentBoardState() {
        val pref = Preferences.Preference("level${level.ordinal}", "-1", { it })
        var s = board.xy2index(lastHumanMove.x, lastHumanMove.y).toString() + "|"
        s += board.xy2index(lastAIMove.x, lastAIMove.y).toString() + "|"
        pref.set(s + board.exportToString())
    }

    fun loadBoardStateFromPreferences() {
        val pref = Preferences.Preference("level${level.ordinal}", "-1", { it }).get()
        if (pref != "-1") {
            val (sHumanLastMove, sAiLastMove, sBoard) = pref.split("|")
            board.importFromString(sBoard)
            if (sHumanLastMove.i!! >= 0) {
                lastHumanMove = board.index2xy(sHumanLastMove.i!!)
            }
            if (sAiLastMove.i!! >= 0) {
                lastAIMove = board.index2xy(sAiLastMove.i!!)
            }
        }
        if (lastHumanMove != invalidCoord) {
            gpsManager.resetOriginToLastMoveCellCoordinates(lastHumanMove)
        }
    }

    private fun makePlayerAndMaybeAiMove(humanMove: Coordi) {
        if (humanMove != playerGpsCell && !Preferences.trainingMode.get()) {
            Toasts.showToast("You must walk there before you can make that move!", Toasts.Duration.LONG)
            return
        }
        Toasts.showToast("${board.getPlayerName()} made move ${coordsToLabel(humanMove.x, humanMove.y)}")
        Sounds.kick.play()
        lastHumanMove = humanMove
        val (px, py) = humanMove
        board.makeMove(py * board.size + px)
        StatusBar.setStatusMessage(board.getStatusText())
        gpsManager.recenterToCurrentCellCenter()
        checkGameOver()
        saveCurrentBoardState()
        if (!board.isGameOver() && Preferences.vsAiMode.get()) {
            makeAiMove()
        }
    }

    private fun makeAiMove() {
        lastAIMove = computeAndExecuteAiMove()
        val (aiX, aiY) = lastAIMove
        Toasts.showToast("AI made move ${coordsToLabel(aiX, aiY)}")
        StatusBar.setStatusMessage(board.getStatusText())
        checkGameOver()
        saveCurrentBoardState()
    }

    private fun checkGameOver() {
        if (!board.isGameOver()) {
            return
        }
        if (!Preferences.mute.get()) {
            Gdx.input.vibrate(longArrayOf(0, 200, 200, 200), -1)
        }
        var unlockMessage = "Next level is unlocked."
        if (!Preferences.cheatingEnabled) {
            if (Preferences.trainingMode.get()) {
                unlockMessage = "In normal (GPS) mode, the next level would now get unlocked. You can switch back to normal mode in the settings."
            }
        }
        if (Preferences.vsAiMode.get()) {
            when (board.winner) {
                Draw -> {
                    if (level.drawCountsAsLevelSolved) {
                        gameOverWindow("Draw!", "It's a draw! For this level, this is sufficient. $unlockMessage")
                        handleLevelSolved()
                    } else {
                        gameOverWindow("Draw!", "It's just a draw. Maybe you can do better next time?")
                    }
                }
                Player1, Player2 -> {
                    if (board.winner == alof(Player2, Player1)[level.humanHasFirstMove.i]) {
                        gameOverWindow("You won!", "Congratulations! You won! $unlockMessage")
                        handleLevelSolved()
                    } else {
                        gameOverWindow("You lost!", "You lost. Better luck next time!")
                    }
                }
                else -> error("bad winner")
            }
        } else {
            when (board.winner) {
                Draw -> {
                    gameOverWindow("Draw!", "It's a draw!")
                }
                Player1, Player2 -> {
                    val winnerName = board.getPlayerName(board.winner)
                    gameOverWindow("$winnerName won!", "$winnerName won. (In order to unlock levels, you'll have to beat the AI though.)")
                }
                else -> error("bad winner")
            }
        }
    }

    private fun handleLevelSolved() {
        if ((Preferences.trainingMode.get() || !Preferences.vsAiMode.get()) && !Preferences.cheatingEnabled) {
            return
        }
        if (level.ordinal + 1 == Preferences.unlockedLevels.get()) {
            Preferences.unlockedLevels.set(Preferences.unlockedLevels.get() + 1)
        }
    }

    private fun gameOverWindow(title: String, message: String) {
        UiUtil.myDialog(uiStage,
                title, message, listOf("Main Menu", "Restart"),
                closeOnClickOutside = true) { buttonIndex ->
            when (buttonIndex) {
                0 -> {
                    backToMenu()
                }
                else -> startNewGame()
            }
        }
    }

    fun KTableWidget.addUiToTopView() {
        table {
            align(Align.left)
            button {
                image("info")
                color = settings.defaultButtonBgColor
                onClick { showInfoDialog() }
            }.cell(height = hudTopButtonHeight, growX = true, fillX = true, expandX = true)
            //textButton("Recenter GPS", style = "large") {
            button {
                image("center")
                color = settings.defaultButtonBgColor
                onClick { gpsManager.recenterGpsToLastMove() }
            }.cell(height = hudTopButtonHeight, growX = true, fillX = true, expandX = true)
            button {
                image("rotate-right")
                color = settings.defaultButtonBgColor
                onClick {
                    compassRotation = (compassRotation + 1) % 4
                    gpsManager.recenterGpsToLastMove()
                }
            }.cell(width = hudTopButtonHeight, height = hudTopButtonHeight)
//            textButton("Restart", style = "large") {
            button {
                image("new")
                pad(10f)
                color = settings.defaultButtonBgColor
                onClick {
                    UiUtil.myDialog(uiStage,
                            "Start New Game?", "Do you want to start a new game? Any progress of this game will be lost.",
                            listOf("New Game", "Cancel"),
                            closeOnClickOutside = true) { buttonIndex: Int ->
                        if (buttonIndex == 0) {
                            startNewGame()
                            Toasts.showToast("New Game Started.")
                        }
                    }
                }
            }.cell(height = hudTopButtonHeight, growX = true, row = true)
        }.cell(align = Align.left, row = true, growX = true)
    }

    private fun showInfoDialog() {
        UiUtil.myDialog(uiStage,
                "Game Rules", level.rulesAndConditions, listOf("Close"),
                closeOnClickOutside = true, scrollable = true)
    }

    fun startNewGame() {
        lastAIMove = invalidCoord
        lastHumanMove = invalidCoord
        board.reset()
        saveCurrentBoardState()
        reenter()
        gpsManager.resetToOriginalCenter()
    }

    private fun reenter() {
        board.reset()
        loadBoardStateFromPreferences()
        if (Preferences.vsAiMode.get()) {
            if (board.currentPlayerId == board.human1DefaultId && !board.isGameOver()) {
                makeAiMove()
            }
        }
        compassRotation = 0
        StatusBar.setStatusMessage(board.getStatusText())
    }

}