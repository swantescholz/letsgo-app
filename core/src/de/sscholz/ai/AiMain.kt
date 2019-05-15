package de.sscholz.ai

import de.sscholz.Level
import de.sscholz.util.f
import de.sscholz.util.format
import de.sscholz.util.printl

fun main() {
    val stupidPolicy = StupidRandomMovePolicy()
    val normalRandomPlayoutPolicy = NormalRandomPlayoutPolicy()

    val numGames = 1000
    var delta = 0
    for (i in 1..numGames) {
        val b = GomokuBoardState(3, 3, Level.Tutorial)
        var player = if (i % 2 == 0) 1 else 2
        while (!b.isGameOver()) {
            var move = stupidPolicy.sampleMoveFromProbabilityDistribution(b)
            if (player == 1) {
                val mcts = Mcts(b, normalRandomPlayoutPolicy, numPlayouts = 10000)
                move = mcts.getAndExecuteBestMove()
            }
            player = 3 - player
            b.makeMove(move)
        }
        var d = b.winner
        if (d == 2) {
            d = -1
        }
        printl("$i: $d")
        delta += d
    }
    printl("dela: ${delta}/$numGames = ${(delta.f / numGames * 100.0).format(3)} %")
}