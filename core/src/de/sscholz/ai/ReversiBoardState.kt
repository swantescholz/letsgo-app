package de.sscholz.ai

import de.sscholz.Level
import de.sscholz.util.Coordi

// simplified rules: game ends when one player cannot move anymore
class ReversiBoardState(size: Int, level: Level) : BoardState(size, level) {
    override fun deepCopy(): BoardState {
        val copy = ReversiBoardState(size, level)
        copy.copyAttributesFromOtherInstance(this)
        return copy
    }

    override fun reset() {
        super.reset()
        val c = size / 2 - 1
        this[c, c] = Player1
        this[c + 1, c + 1] = Player1
        this[c, c + 1] = Player2
        this[c + 1, c] = Player2
        updateAvailables()
    }

    init {
        if (size % 2 != 0) {
            error("size must be even!")
        }
        reset()
    }

    private fun updateAvailables() {
        availableMoves.clear()
        val p = currentPlayerId
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (this[x, y] == p) {
                    val xy = Coordi(x, y)
                    Coordi.Nesw8.forEach { delta ->
                        if (this[xy + delta] != 3 - p) {
                            return@forEach
                        }
                        for (i in 2 until size) {
                            val candidateCoords = xy + delta * i
                            val cellValue = this[candidateCoords]
                            if (cellValue != 3 - p) {
                                if (cellValue == Empty) {
                                    availableMoves.add(candidateCoords.toIndex(size))
                                }
                                return@forEach
                            }
                        }
                    }
                }
            }
        }
    }

    override fun makeMove(cellIndex: Int) {
        if (cellIndex !in availableMoves) {
            error("invalid reversi move")
        }
        val xy = Coordi.fromIndex(cellIndex, size)
        Coordi.Nesw8.forEach { delta ->
            doCaptures(xy, delta)
        }
        cells[cellIndex] = currentPlayerId
        lastMove = cellIndex
        currentPlayerId = 3 - currentPlayerId
        updateAvailables()
        checkWinConditions()
    }

    private fun doCaptures(xy: Coordi, delta: Coordi) {
        val p = currentPlayerId
        for (i in 1 until size) {
            val cellContent = this[xy + delta * i]
            if (cellContent != 3 - p) {
                if (cellContent == p) {
                    for (j in 1 until i) {
                        this[xy + delta * j] = p
                    }
                }
                break
            }
        }
    }

    override fun checkWinConditions() {
        if (availableMoves.isNotEmpty()) {
            return
        }
        val p1StoneCount = cells.count { it == Player1 }
        val p2StoneCount = cells.count { it == Player2 }
        winner = when {
            p1StoneCount > p2StoneCount -> Player1
            p1StoneCount < p2StoneCount -> Player2
            else -> Draw
        }
    }

    override fun getStatusText(): String {
        val p1StoneCount = cells.count { it == Player1 }
        val p2StoneCount = cells.count { it == Player2 }
        return "${currentPlayerOrWinnerStatusText()} Score: B/W=$p1StoneCount/$p2StoneCount"
    }
}
