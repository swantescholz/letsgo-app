package de.sscholz.ai

interface Policy {

    // returns estimated probabilities for each possible action, and a value of the current board
    // state for the current player (between -1 = lose and 1 = win)
    fun evaluate(state: BoardState): Pair<HashMap<Int, Double>, Double>

}

//class AlphaZeroPolicy(tfModelFilePath: String) : Policy {
//
//
//
//    override fun evaluate(state: BoardState): Pair<HashMap<Int, Double>, Double> {
//
//    }
//
//}

class NormalRandomPlayoutPolicy : Policy {
    override fun evaluate(state: BoardState): Pair<HashMap<Int, Double>, Double> {
        val prob = 1.0 / state.availables.size
        val probs = HashMap<Int, Double>()
        for (action in state.availables) {
            probs[action] = prob
        }
        val currentPlayerId = state.currentPlayerId
        while (!state.isGameOver()) {
            val randomMove = sampleMoveFromProbabilityDistribution(state)
            state.makeMove(randomMove)
        }
        if (state.isGameDrawn()) {
            return Pair(probs, 0.0)
        }
        if (state.winner == 3 - currentPlayerId) {
            return Pair(probs, 1.0)
        }
        return Pair(probs, -1.0)
    }

    // by default: uniform at random. this method is only used in normal playout policy
    fun sampleMoveFromProbabilityDistribution(state: BoardState): Int {
        return state.availables.random()
    }
}

class StupidRandomMovePolicy : Policy {
    override fun evaluate(state: BoardState): Pair<HashMap<Int, Double>, Double> {
        val prob = 1.0 / state.availables.size
        val probs = HashMap<Int, Double>()
        for (action in state.availables) {
            probs[action] = prob
        }
        return Pair(probs, 0.0)
    }

    fun sampleMoveFromProbabilityDistribution(state: BoardState): Int {
        return state.availables.random()
    }
}
