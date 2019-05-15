package de.sscholz

import de.sscholz.ai.*
import de.sscholz.util.alof
import de.sscholz.util.i
import de.sscholz.util.rearrangeNewlines

private val gobangRule = """
    In addition to normal Gomoku, in Gobang there is the following new rule: If one player
    surrounds two adjacent opponent stones from both sides, they kill them (e.g. X00X -> X__X).
""".trimIndent().rearrangeNewlines()

private val reversiRules = """
    In Reversi, when you place a stone adjacent to a row (horizontally, vertically or diagonally)
    of stones of your opponent that are enclosed by another one of your stones on the other end,
    then those stones are "captured". Captured opponent stones are replaced with stones of your color.

    (E.g. X000X -> XXXXX).

    The game starts with two stones of each color in the center.


    The game ends when the current player cannot make a legal move.

    In the end, the player who has more stones of their color on the board wins.
""".trimIndent().rearrangeNewlines()

private val goRules = """
    In Go, when you place a stone so that a vertically/horizontally connected group of
    stones has zero liberties afterwards, that group is captured.

    Liberties are vertically/horizontally adjacent empty squares next to your group.

    In this version of Capture Go, there is no Ko rule (i.e. recapturing is allowed),
    and suicide is also allowed (and counts as captures for your opponent).
""".trimIndent().rearrangeNewlines()

private val nimRules = """
    In Nim, during your turn, you must play a stone in a row so that it is then the rightmost (most Eastern)
    stone in that row.

    The first player who cannot make a valid move loses.

    At the beginning, three stones have already been placed.
""".trimIndent().rearrangeNewlines()

private val hexRules = """
    In Hex, your goal is to create a North-South connection with your stones. At the same time,
    your opponent tries to create a West-East connection with their stones.
    Whoever finishes their connection first, wins.

    Squares are considered to be connected vertically, horizontally, and in one diagonal direction,
    namely the Northwest-Southeast diagonal. The other diagonal (Northeast-Southwest) does not count for connections.
""".trimIndent().rearrangeNewlines()

enum class Level(val title: String,
                 val rules: String,
                 val humanHasFirstMove: Boolean,
                 val drawCountsAsLevelSolved: Boolean,
                 private val boardCreator: Level.() -> BoardState,
                 private val aiMoveComputer: Level.(BoardState) -> Int = { getAiMoveViaMcts(it) }) {
    Tutorial("Tutorial (Tic-Tac-Toe)", "First player to get three in a row wins.", false, true, {
        GomokuBoardState(3, 3, this)
    }),
    Gomoku1("Gomoku (6x6)", "First player to get four in a row wins.", true, false, {
        GomokuBoardState(6, 4, this)
    }),
    Gobang1("Gobang (6x6)", "First player to get four in a row wins. $gobangRule", true, false, {
        GobangBoardState(6, 4, this)
    }),
    Gomoku2("Gomoku (8x8)", "First player to get five in a row wins.", true, false, {
        GomokuBoardState(8, 5, this)
    }),
    Reversi1("Reversi (6x6)", reversiRules, true, false, {
        ReversiBoardState(6, this)
    }),
    Gobang2("Gobang (8x8)", "First player to get five in a row wins. $gobangRule", true, false, {
        GobangBoardState(8, 5, this)
    }),
    CaptureGo1("Capture Go (8x8)", "$goRules\n\nYou win once you capture any opponent stone.", true, false, {
        CaptureGoBoardState(8, 1, this)
    }),
    Reversi2("Reversi (8x8)", reversiRules, true, false, {
        ReversiBoardState(8, this)
    }),
    CaptureGo2("Capture Go (10x10)", "$goRules\n\nYou win once you've captured in total four opponent stones.", true, false, {
        CaptureGoBoardState(10, 4, this)
    }),
    Nim1("Nim (6x6)", nimRules, true, false, {
        NimBoardState(6, this)
    }, { boardState ->
        val aiMove = (boardState as NimBoardState).computeOptimalMoveExecptFirstTimeAround()
        boardState.makeMove(aiMove)
        aiMove
    }),
    Hex1("Hex (9x9)", hexRules, false, false, {
        HexBoardState(9, this)
    });

    val rulesAndConditions = rules.let {
        var res = it + "\n\n"
        res += "Get a win" + (if (drawCountsAsLevelSolved) " or a draw" else "") + " here in order to solve this level.\n\n"
        res += alof("AI moves", "You move")[humanHasFirstMove.i] + " first."
        res
    }

    fun createBoard(): BoardState = boardCreator()
    fun getAndExecuteBestMove(board: BoardState): Int {
        return aiMoveComputer(board)
    }

    private fun getAiMoveViaMcts(board: BoardState): Int {
        val policy = NormalRandomPlayoutPolicy()
        val mcts = Mcts(board, policy, Global.settings.cParameter, Global.settings.numMctsPlayouts)
        return mcts.getAndExecuteBestMove()
    }


}