package de.sscholz.ai

import de.sscholz.Level
import de.sscholz.util.Coordi
import de.sscholz.util.times

class GobangBoardState(size: Int, winningRowLength: Int = 5, level: Level)
    : GomokuBoardState(size, winningRowLength, level) {

    override fun deepCopy(): BoardState {
        val copy = GobangBoardState(size, winningRowLength, level)
        copy.copyAttributesFromOtherInstance(this)
        return copy
    }

    override fun makeMove(cellIndex: Int) {
        super.makeMove(cellIndex)
        val xy = Coordi.fromIndex(cellIndex, size)
        val expected = arrayOf(3 - currentPlayerId, currentPlayerId, currentPlayerId, 3 - currentPlayerId)
        Coordi.Nesw8.forEach { delta ->
            checkKills(xy, delta, expected)
        }
    }

    private fun checkKills(xy: Coordi, delta: Coordi, expected: Array<Int>) {
        var good = true
        for (i in 1..3) {
            if (this[xy + delta * i] != expected[i]) {
                good = false
                break
            }
        }
        if (good) {
            for (i in 1..2) {
                this[xy + delta * i] = Empty
                availableMoves.add((xy + i * delta).toIndex(size))
            }
        }
    }


}
