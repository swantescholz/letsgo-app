import de.sscholz.Level
import de.sscholz.ai.*
import de.sscholz.util.f
import de.sscholz.util.format
import de.sscholz.util.printl
import io.mockk.mockk
import org.junit.Test

class AiTest {

    val mockLevel = mockk<Level>()

    @Test
    fun `test board self play`() {
        val b = GomokuBoardState(6, 4, mockLevel)
        val policy = NormalRandomPlayoutPolicy()
        val mcts = Mcts(b, policy, numPlayouts = 10000)
        printl(b)
        while (!b.isGameOver()) {
            val bestMove = mcts.getAndExecuteBestMove()
            printl("Player ${b.currentPlayerId}: ${bestMove % b.size}, ${bestMove / b.size}")
//            sleep(0.3)
            printl(b)
        }
        printl("winner: ${b.winner}")
    }

    @Test
    fun `reversi draw end`() {
        val b = ReversiBoardState(4, mockLevel)
        val policy = StupidRandomMovePolicy()
        val mcts = Mcts(b, policy, numPlayouts = 10000)
        printl(b)
        while (!b.isGameOver()) {
            val bestMove = mcts.getAndExecuteBestMove()
            printl("Player ${b.currentPlayerId}: ${bestMove % b.size}, ${bestMove / b.size}")
//            sleep(0.3)
            printl(b)
        }
        printl("winner: ${b.winner}")
    }

    @Test
    fun `go capture test`() {
        val b = CaptureGoBoardState(9, 3, Level.CaptureGo2)
        val policy = StupidRandomMovePolicy()
        val mcts = Mcts(b, policy, numPlayouts = 10000)
        printl(b)
        while (!b.isGameOver()) {
            val bestMove = mcts.getAndExecuteBestMove()
            printl("Player ${b.currentPlayerId}: ${bestMove % b.size}, ${bestMove / b.size}")
//            sleep(0.3)
            printl(b)
        }
        printl("winner: ${b.winner}")
    }

    @Test
    fun `mcts should be better than random`() {
        val stupidPolicy = StupidRandomMovePolicy()
        val normalRandomPlayoutPolicy = NormalRandomPlayoutPolicy()

        val numGames = 100
        var delta = 0
        for (i in 1..numGames) {
            val b = GomokuBoardState(3, 3, Level.Tutorial)
            var player = if (i % 2 == 0) 1 else 2
            while (!b.isGameOver()) {
                var move = stupidPolicy.sampleMoveFromProbabilityDistribution(b)
                if (player == 1) {
                    val mcts = Mcts(b, normalRandomPlayoutPolicy, numPlayouts = 1000)
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

    @Test
    fun `test tictactoe ai not stopping me from winning`() {
        val normalRandomPlayoutPolicy = NormalRandomPlayoutPolicy()
        val b = GomokuBoardState(3, 3, mockLevel)
        b.makeMove(3)
        b.makeMove(4)
        b.makeMove(8)
        b.makeMove(7)
        printl(b)
        val mcts = Mcts(b, normalRandomPlayoutPolicy, numPlayouts = 1000)
        val move = mcts.getAndExecuteBestMove()
        printl(move)
        printl(b)
    }
}
