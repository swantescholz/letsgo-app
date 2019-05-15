package de.sscholz.ai

import de.sscholz.Level
import de.sscholz.ai.BoardState.Companion.Player1
import de.sscholz.ai.BoardState.Companion.Player2
import de.sscholz.util.d
import de.sscholz.util.f
import de.sscholz.util.printl
import org.tensorflow.Graph
import org.tensorflow.Session
import org.tensorflow.Tensor
import java.io.File

private fun predict(sess: Session, inputTensor: Tensor<*>, board: BoardState): Pair<FloatArray, Float> {
    val sProbs = "output_probs/LogSoftmax"
    val sScore = "output_score/Tanh"
    val result = sess.runner()
            .feed("input", inputTensor)
            .fetch(sProbs).fetch(sScore).run()
    val outputBufferProbs = Array(1) { FloatArray(board.size * board.size) }
    result[0].copyTo(outputBufferProbs)
    for (i in 0 until outputBufferProbs[0].size) {
        outputBufferProbs[0][i] = Math.exp(outputBufferProbs[0][i].d).f
    }
    val outputBufferScore = Array(1) { FloatArray(1) }
    result[1].copyTo(outputBufferScore)
    return Pair(outputBufferProbs[0], outputBufferScore[0][0])
}

private fun twoDFloatArray(size: Int, f: (Int, Int) -> Float): Array<FloatArray> {
    return Array(size) { y ->
        FloatArray(size) { x ->
            f(x, y)
        }
    }
}

private fun BoardState.twoDArray(contentId: Int): Array<FloatArray> {
    return twoDFloatArray(size) { x, y ->
        if (this[x, y] == contentId) 1.0f else 0f
    }
}

fun BoardState.createTfInputTensor(): Tensor<*> {
    val p1Stones = this.twoDArray(Player1)
    val p2Stones = this.twoDArray(Player2)
    val lastMoveLayer = twoDFloatArray(size) { x, y -> 0f }
    val currentPlayerLayer = twoDFloatArray(size) { x, y -> if (currentPlayerId == 1) 0f else 1f }
    lastMove?.let { lastMoveLayer[it / size][it % size] = 1f }
    val inputData = arrayOf(arrayOf(p1Stones, p2Stones, lastMoveLayer, currentPlayerLayer))
    val inputTensor = Tensor.create(inputData, java.lang.Float::class.java)
    return inputTensor!!
}

fun main() {
//    val graphBytes = MyDatabase.readInternalReadOnlyBinaryFile("ttt1.tf")
    val graphBytes = File("android/assets/ttt2.tf").readBytes()

    val g = Graph()
    g.importGraphDef(graphBytes)
    //open session using imported graph
    val sess = Session(g)
    val b = GomokuBoardState(3, 3, Level.Tutorial)
    b.makeMove(1)
    b.makeMove(0)
    b.makeMove(4)
    b.makeMove(3)
    printl(b)
    val inputTensor = b.createTfInputTensor()
    val (probs, score) = predict(sess, inputTensor, b)
    printl("score = $score")
    for (i in 0 until probs.size) {
        printl(i, probs[i])
    }
}