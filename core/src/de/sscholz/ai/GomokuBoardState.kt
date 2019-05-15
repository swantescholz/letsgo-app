package de.sscholz.ai

import de.sscholz.Level
import de.sscholz.util.Coordi

open class GomokuBoardState(size: Int, val winningRowLength: Int = 5, level: Level = Level.Tutorial,
                            initAvailableMoves: Boolean = true) : BoardState(size, level) {

    override fun reset() {
        super.reset()
        availableMoves.addAll(0 until size * size)
    }

    override fun deepCopy(): BoardState {
        val copy = GomokuBoardState(size, winningRowLength, level, false)
        copy.copyAttributesFromOtherInstance(this)
        return copy
    }

    init {
        if (initAvailableMoves) {
            this.availableMoves.addAll(0 until size * size)
        }
    }

    override fun checkWinConditions() {
        for (pId in 1..2) {
            Coordi.Nneese4.forEach { (dx, dy) ->
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        var good = true
                        for (i in 0 until winningRowLength) {
                            if (this[x + dx * i, y + dy * i] != pId) {
                                good = false
                                break
                            }
                        }
                        if (good) {
                            winner = pId
                            return
                        }
                    }
                }
            }
        }
        if ((0 until size * size).none { this[it] == 0 }) {
            winner = 0 // draw
        }
    }

}
