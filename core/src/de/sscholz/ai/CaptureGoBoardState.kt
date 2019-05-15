package de.sscholz.ai

import de.sscholz.Level
import de.sscholz.util.Coordi
import java.util.*

// simplified rules: there is no ko. you can simply capture back
class CaptureGoBoardState(size: Int, val capturesToWin: Int, level: Level) : BoardState(size, level) {

    var capturesByPlayer1 = 0
    var capturesByPlayer2 = 0

    override fun deepCopy(): BoardState {
        val copy = CaptureGoBoardState(size, capturesToWin, Level.CaptureGo2)
        copy.copyAttributesFromOtherInstance(this)
        copy.capturesByPlayer1 = capturesByPlayer1
        copy.capturesByPlayer2 = capturesByPlayer2
        return copy
    }

    override fun reset() {
        super.reset()
        capturesByPlayer1 = 0
        capturesByPlayer2 = 0
        val c = size / 2 - 1
        this[c, c] = Player1
        this[c + 1, c + 1] = Player1
        this[c, c + 1] = Player2
        this[c + 1, c] = Player2
        availableMoves.addAll(0 until size * size)
        updateAvailables()
    }

    init {
        if (size % 2 != 0) {
            error("size must be even!")
        }
        reset()
    }

    private fun updateAvailables() {
        lastMove?.let { availableMoves.remove(it) }
    }

    override fun makeMove(cellIndex: Int) {
        if (cellIndex !in availableMoves) {
            error("invalid go move")
        }
        val xy = Coordi.fromIndex(cellIndex, size)
        cells[cellIndex] = currentPlayerId
        lastMove = cellIndex
        xy.neighborsNesw4.forEach {
            killGroupIfItHasZeroLiberties(it)
        }
        killGroupIfItHasZeroLiberties(xy)
        currentPlayerId = 3 - currentPlayerId
        updateAvailables()
        checkWinConditions()
    }

    private fun killGroupIfItHasZeroLiberties(start: Coordi) {
        val playerThatMightDie = this[start]
        if (playerThatMightDie !in 1..2) {
            return
        }
        val q = LinkedList<Coordi>()
        q.addLast(start)
        val groupThatMightDie = HashSet<Coordi>()
        while (!q.isEmpty()) {
            val current = q.pollFirst()
            if (current in groupThatMightDie) {
                continue
            }
            groupThatMightDie.add(current)
            current.neighborsNesw4.forEach {
                when (this[it]) {
                    Empty -> return
                    playerThatMightDie -> {
                        if (it !in groupThatMightDie) {
                            q.addLast(it)
                        }
                    }
                }
            }
        }
        if (playerThatMightDie == Player2) {
            capturesByPlayer1 += groupThatMightDie.size
        } else {
            capturesByPlayer2 += groupThatMightDie.size
        }
        groupThatMightDie.forEach {
            this[it] = Empty
            availableMoves.add(it.toIndex(size))
        }
    }

    override fun checkWinConditions() {
        if (capturesByPlayer1 >= capturesToWin) {
            winner = Player1
        } else if (capturesByPlayer2 >= capturesToWin) {
            winner = Player2
        } else if (availableMoves.isEmpty()) {
            winner = Draw
        }
    }

    override fun getStatusText(): String {
        return "${currentPlayerOrWinnerStatusText()} Score: B/W=$capturesByPlayer1/$capturesByPlayer2"
    }
}
