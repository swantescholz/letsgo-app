package de.sscholz.ai

import de.sscholz.util.random
import de.sscholz.util.sampleIndexFromThisProbabilityDistribution

private class MctsNode(var parent: MctsNode?, var priorP: Double) {
    val children = HashMap<Int, MctsNode>()
    var nVisits = 0
    var Q = 0.0
    var u = 0.0
    private val noise = (0.0..1e-8).random() // as randomizing tie braker for getValue

    /**
     * Expand tree by creating new children.
     * action_priors: a list of tuples of actions and their prior probability
     * according to the policy function.
     */
    fun expand(actionPriors: HashMap<Int, Double>) {
        for ((action, prob) in actionPriors) {
            if (action !in children) {
                children[action] = MctsNode(this, prob)
            }
        }
    }

    // returns (action, child), selecting the child that maximizes the value
    fun select(c_parameter: Double): Pair<Int, MctsNode> {
        val entry = children.maxBy { it.value.getValue(c_parameter) }!!
        return Pair(entry.key, entry.value)
    }

    /**
     * Calculate and return the value for this node.
    It is a combination of leaf evaluations Q, and this node's prior
    adjusted for its visit count, u.
    c_puct: a number in (0, inf) controlling the relative impact of
    value Q, and prior probability P, on this node's score.
     */
    fun getValue(c_parameter: Double): Double {
        u = (c_parameter * priorP *
                Math.sqrt(parent!!.nVisits.toDouble()) / (1 + nVisits))
        return Q + u + noise
    }

    fun update(leafValue: Double) {
        nVisits++
        Q += 1.0 * (leafValue - Q) / nVisits
    }

    fun updateRecursive(leafValue: Double) {
        parent?.updateRecursive(-leafValue) // switch good/bad value perception (as it depends on current player)
        update(leafValue)
    }

    val isLeaf: Boolean
        get() = children.size == 0

    val isRoot: Boolean
        get() = parent == null
}

/**
 *  policy_value_fn: a function that takes in a board state and outputs
a list of (action, probability) tuples and also a score in [-1, 1]
(i.e. the expected value of the end game score from the current
player's perspective) for the current player.
c_puct: a number in (0, inf) that controls how quickly exploration
converges to the maximum-value policy. A higher value means
relying on the prior more.
 */
class Mcts(val board: BoardState, val policy: Policy, val cParameter: Double = 5.0, val numPlayouts: Int = 10000) {

    companion object {
        fun softmax(doubleArray: DoubleArray): DoubleArray {
            if (doubleArray.size == 0) {
                return doubleArray
            }
            val sumInv = 1.0 / doubleArray.sum()
            val max = doubleArray.max()!!
            return doubleArray.map { Math.exp(it - max) * sumInv }.toDoubleArray()
        }
    }

    private var root = MctsNode(null, 1.0)

    /**
     * Run a single playout from the root to the leaf, getting a value at
    the leaf and propagating it back through its parents.
    State is modified in-place, so a copy should usually be provided.
     */
    private fun playout(boardCopy: BoardState) {
        var node = root
        while (true) {
            if (node.isLeaf) {
                break
            }
            val (action, newNode) = node.select(cParameter)
            boardCopy.makeMove(action)
            node = newNode
        }
        val mctsLeafIsGameOver = boardCopy.isGameOver()
        var (actionProbs, leafValue) = policy.evaluate(boardCopy)
        if (!mctsLeafIsGameOver) {
            node.expand(actionProbs)
        } else {
            if (boardCopy.isGameDrawn()) {
                leafValue = 0.0
            } else {
                leafValue = if (boardCopy.winner == boardCopy.currentPlayerId) 1.0 else -1.0
            }
        }
        node.updateRecursive(-leafValue)
    }

    /**
     * Run all playouts sequentially and return the available actions and
    their corresponding probabilities.
    board: the current game board
    temp: temperature parameter in (0, 1] controls the level of exploration
     */
    private fun getMoveProbs(temp: Double = 1e-3): Pair<IntArray, DoubleArray> {
        for (n in 0 until numPlayouts) {
            val stateCopy = board.deepCopy()
            playout(stateCopy)
        }

        // calc the move probabilities based on visit counts at the root node
        val acts = root.children.keys.toIntArray()
        val visits = acts.map { root.children[it]!!.nVisits }.toIntArray()
        val actProbs = softmax(visits.map { 1.0 / temp * Math.log(Math.max(1, it) + 1e-10) }.toDoubleArray())
        if (actProbs.sum() < 0.000001) {
            error("bad actProbs")
        }

        return Pair(acts, actProbs)
    }

    /**
     * Step forward in the tree, keeping everything we already know
    about the subtree.
     */
    private fun executeMove(move: Int) {
        if (move in root.children) {
            root = root.children[move]!!
            root.parent = null
        } else {
            root = MctsNode(null, 1.0)
        }
    }

    fun getAndExecuteBestMove(temp: Double = 1e-3): Int {
        if (board.isGameOver()) {
            throw RuntimeException("no possible moves")
        }
        val (acts, probs) = getMoveProbs(temp)
        // with the default temp=1e-3, it is almost equivalent
        // to choosing the move with the highest prob
        val move = acts[probs.asIterable().sampleIndexFromThisProbabilityDistribution()]
        executeMove(-1) // reset root node
        board.makeMove(move)
        return move
    }
}