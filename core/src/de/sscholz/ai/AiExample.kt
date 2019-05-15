package de.sscholz.ai

import de.sscholz.util.MyDatabase
import org.tensorflow.Graph
import org.tensorflow.Session
import org.tensorflow.Tensor

private fun predict(sess: Session, inputTensor: Tensor<*>): Array<FloatArray> {
    val result = sess.runner()
            .feed("input", inputTensor)
            .fetch("not_activated_output").run()[0]
    val outputBuffer = Array(1) { FloatArray(3) }
    result.copyTo(outputBuffer)
    return outputBuffer
}

fun aiTest() {
    val graphBytes = MyDatabase.readInternalReadOnlyBinaryFile("my_saved_model.pb")

    val g = Graph()
    g.importGraphDef(graphBytes)
    //open session using imported graph
    val sess = Session(g)
    val inputData = arrayOf(floatArrayOf(4f, 3f, 2f, 1f))
    // We have to create tensor to feed it to session,
    // unlike in Python where you just pass Numpy array
    val inputTensor = Tensor.create(inputData, java.lang.Float::class.java)
    val output = predict(sess, inputTensor)
    for (i in 0 until output[0].size) {
        println(output[0][i])//should be 41. 51.5 62.
    }
}

