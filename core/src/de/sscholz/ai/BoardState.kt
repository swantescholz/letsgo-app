package de.sscholz.ai

import de.sscholz.Level
import de.sscholz.util.Coordi
import de.sscholz.util.Preferences
import de.sscholz.util.i


abstract class BoardState(val size: Int, val level: Level = Level.Tutorial) {

    companion object {
        // cell IDs: 0=empty, 1=player1, 2=player2, 3=burned, -1=invalid indexing
        // player1 always starts
        val cellChars = arrayListOf("⋅", "X", "O", "B")
        val Invalid = -1
        val Empty = 0
        val Player1 = 1
        val Player2 = 2
        val Burned = 3
        val Draw = 0
        val Undefined = -1
    }

    protected fun copyAttributesFromOtherInstance(otherInstance: BoardState) {
        this.cells = otherInstance.cells.copyOf()
        this.lastMove = otherInstance.lastMove
        this.winner = otherInstance.winner
        this.currentPlayerId = otherInstance.currentPlayerId
        this.availableMoves.clear()
        this.availableMoves.addAll(otherInstance.availableMoves)
    }

    var currentPlayerId: Int = Player1
        protected set

    val otherPlayerId: Int
        get() = 3 - currentPlayerId

    var lastMove: Int? = null
        protected set

    // -1 = game not over yet, 0 = draw, 1=player1 won, 2=player2 won
    var winner: Int = -1
        protected set

    protected var cells = IntArray(size * size)
    protected val availableMoves = HashSet<Int>()

    val availables: Set<Int>
        get() = availableMoves

    fun isGameOver() = winner != Undefined
    fun isGameDrawn() = winner == Draw

    open fun makeMove(cellIndex: Int) {
        cells[cellIndex] = currentPlayerId
        availableMoves.remove(cellIndex)
        lastMove = cellIndex
        currentPlayerId = 3 - currentPlayerId
        checkWinConditions()
    }

    fun xy2index(x: Int, y: Int): Int {
        return y * size + x
    }

    operator fun get(xy: Coordi): Int {
        return this[xy.x, xy.y]
    }

    operator fun get(i: Int): Int {
        if (i < 0 || i >= size * size) {
            return Invalid
        }
        return cells[i]
    }

    operator fun get(x: Int, y: Int): Int {
        if (x < 0 || y < 0 || x >= size || y >= size) {
            return Invalid
        }
        return cells[y * size + x]
    }

    protected operator fun set(xy: Coordi, value: Int) {
        this[xy.x, xy.y] = value
    }

    protected operator fun set(i: Int, value: Int) {
        cells[i] = value
    }

    protected operator fun set(x: Int, y: Int, value: Int) {
        cells[x + y * size] = value
    }

    override fun toString(): String {
        return (size - 1 downTo 0).map { y ->
            (0 until size).map { x -> cellChars[this[x, y]] }.joinToString("")
        }.joinToString("\n")
    }

    abstract fun deepCopy(): BoardState
    protected abstract fun checkWinConditions()
    open fun reset() {
        for (i in 0 until cells.size) {
            cells[i] = Empty
        }
        currentPlayerId = Player1
        lastMove = null
        winner = Undefined
        availableMoves.clear()
    }

    val human1DefaultId: Int
        get() = if (level.humanHasFirstMove) Player1 else Player2

    val aiDefaultId: Int
        get() = 3 - human1DefaultId

    fun getPlayerName(playerId: Int = currentPlayerId): String {
        val colorName = if (playerId == Player1) "Black" else "White"
        var playerName = if (playerId == human1DefaultId) "you" else "AI"
        if (!Preferences.vsAiMode.get()) {
            playerName = if (playerId == Player1) "Player 1" else "Player 2"
        }
        return "$colorName ($playerName)"
    }

    protected fun currentPlayerOrWinnerStatusText(): String {
        if (isGameOver()) {
            return "${getPlayerName(winner)} has won."
        }
        return "${getPlayerName()} to move."
    }

    open fun getStatusText(): String {
        return currentPlayerOrWinnerStatusText()
    }

    fun exportToString(): String {
        return "$currentPlayerId;$lastMove;$winner;" + (0 until size).map { y ->
            (0 until size).map { x -> this[x, y].toString() }.joinToString("")
        }.joinToString("") + ";" + availableMoves.map { it.toString() }.joinToString(",")
    }

    fun importFromString(s: String) {
        if (s == "-1") {
            return // nothing to do
        }
        val (sCurrentPlayerId, sLastMove, sWinner, sCells, sAvailables) = s.split(";")
        currentPlayerId = sCurrentPlayerId.i!!
        lastMove = sLastMove.i
        winner = sWinner.i!!
        for (i in 0 until size * size) {
            cells[i] = ("" + sCells[i]).i!!
        }
        availableMoves.clear()
        sAvailables.split(",").forEach { it.i?.let { availableMoves.add(it) } }
    }

    open fun isHexagonalGame() = false
    fun index2xy(move: Int): Coordi {
        return Coordi(move % size, move / size)
    }

}
