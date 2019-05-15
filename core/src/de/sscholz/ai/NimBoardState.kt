package de.sscholz.ai

import de.sscholz.Level

class NimBoardState(size: Int, level: Level) : BoardState(size, level) {
    override fun deepCopy(): BoardState {
        val copy = NimBoardState(size, level)
        copy.copyAttributesFromOtherInstance(this)
        return copy
    }

    override fun reset() {
        super.reset()
        availableMoves.addAll(0 until size * size)
        val nRandomMoves = 3
        if (nRandomMoves % 2 == 1) {
            currentPlayerId = otherPlayerId
        }
        for (y in 0 until nRandomMoves) {
            makeMove(y * size + nRandomMoves - y - 1)
        }
        currentPlayerId = if (level.humanHasFirstMove) Player1 else Player2
    }

    init {
        reset()
    }

    private fun updateAvailables() {
        lastMove?.let {
            var move = it
            while (true) {
                availableMoves.remove(move)
                if (move % size == 0) {
                    break
                }
                move--
            }
        }
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
        if (availableMoves.isNotEmpty()) {
            return
        }
        winner = otherPlayerId
    }

    fun computeOptimalMoveExecptFirstTimeAround(): Int {
        if (availableMoves.size == size * size) {
            return 1 + size * (0 until size).random()
        }
        var xorResult = 0
        val rowValues = ArrayList<Int>()
        for (y in 0 until size) {
            var rowValue = 0
            for (x in size - 1 downTo 0) {
                if (this[x, y] != Empty) {
                    break
                }
                rowValue++
            }
            xorResult = xorResult xor rowValue
            rowValues.add(rowValue)
        }
        for (y in 0 until size) {
            val x = rowValues[y]
            val newPotentialRowValue = x xor xorResult
            if (newPotentialRowValue < x) {
                return y * size + (size - 1 - newPotentialRowValue)
            }
        }
        // cannot force win -> make random move
        return availableMoves.random()
    }
}
