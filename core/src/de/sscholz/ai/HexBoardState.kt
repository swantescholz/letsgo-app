package de.sscholz.ai

import de.sscholz.Level
import de.sscholz.util.Coordi
import java.util.*

class HexBoardState(size: Int, level: Level) : BoardState(size, level) {
    override fun deepCopy(): BoardState {
        val copy = HexBoardState(size, level)
        copy.copyAttributesFromOtherInstance(this)
        return copy
    }

    override fun isHexagonalGame(): Boolean {
        return true
    }

    override fun reset() {
        super.reset()
        availableMoves.addAll(0 until size * size)
    }

    init {
        reset()
    }

    private fun updateAvailables() {
        lastMove?.let { availableMoves.remove(it) }
    }

    override fun makeMove(cellIndex: Int) {
        if (cellIndex !in availableMoves) {
            error("invalid nim move")
        }
        cells[cellIndex] = currentPlayerId
        lastMove = cellIndex
        currentPlayerId = otherPlayerId
        updateAvailables()
        checkWinConditions()
    }

    override fun checkWinConditions() {
        if (winner != Undefined) {
            return
        }
        val q = LinkedList<Coordi>()
        val seen = HashSet<Coordi>()
        for (x in 0 until size) {
            if (this[x, 0] == human1DefaultId) {
                q.add(Coordi(x, 0))
            }
        }
        seen.addAll(q)
        while (q.isNotEmpty()) {
            val e = q.pollFirst()
            if (e.y == size - 1) {
                winner = human1DefaultId
                return
            }
            e.neighborsNESeSWNw6.forEach {
                if (this[it] == human1DefaultId && it !in seen) {
                    q.addLast(it)
                    seen.add(e)
                }
            }

        }
        q.clear()
        seen.clear()
        for (y in 0 until size) {
            if (this[0, y] == aiDefaultId) {
                q.add(Coordi(0, y))
            }
        }
        seen.addAll(q)
        while (q.isNotEmpty()) {
            val e = q.pollFirst()
            if (e.x == size - 1) {
                winner = aiDefaultId
                return
            }
            e.neighborsNESeSWNw6.forEach {
                if (this[it] == aiDefaultId && it !in seen) {
                    q.addLast(it)
                    seen.add(e)
                }
            }

        }
        if (availableMoves.isEmpty()) {
            error("but draw is not possible in Hex!")
        }
    }


}
