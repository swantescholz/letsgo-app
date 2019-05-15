import de.sscholz.Level
import de.sscholz.ai.BoardState
import de.sscholz.ai.GomokuBoardState
import de.sscholz.util.*
import ktx.math.times
import ktx.math.vec2
import org.junit.Assert
import org.junit.Test


class MiscTest {

    val delta = 600f

    @Test
    fun `save level`() {
        var b = GomokuBoardState(3, 3, Level.Tutorial)
        b.makeMove(1)
        printl(b, b.availables)
        val s = b.exportToString()
        printl(s)
        b = GomokuBoardState(3, 3, Level.Tutorial)
        printl(b)
        b.importFromString(s)
        printl(b)
    }

    @Test
    fun `misc math`() {
        val p = arrayListOf(0.1, 0.3, 0.6)
        100.times {
            printl(p.sampleIndexFromThisProbabilityDistribution())
        }
    }

    @Test
    fun `test board states`() {
        val bs: BoardState = GomokuBoardState(3, 2, Level.Tutorial)
        Assert.assertEquals(0, bs[1, 2])
        Assert.assertEquals(1, bs.currentPlayerId)
        bs.makeMove(7)
        Assert.assertEquals(1, bs[1, 2])
        Assert.assertEquals(2, bs.currentPlayerId)
        bs.makeMove(8)
        Assert.assertEquals(2, bs[2, 2])
        Assert.assertEquals(1, bs.currentPlayerId)
        Assert.assertEquals(false, bs.isGameOver())
        bs.makeMove(5)
        printl(bs)
        Assert.assertEquals(true, bs.isGameOver())
    }

    @Test
    fun `polygon corner creation`() {
        GdxUtil.createRegularPolygonCorners(4, 2f).printl()
    }

    @Test
    fun `lat long geodesic distance`() {
        val berlin = LatLong(52.579689, 13.449969)
        val hamburg = LatLong(53.537074, 10.013083)
        val expected = 253.1f * 1000f
        Assert.assertEquals(expected, berlin.geodesicDistanceTo(hamburg), delta)
    }

    @Test
    fun `lat long east north delta vector`() {
        val hamburg = LatLong(53.537074, 10.013083)
        val berlin = LatLong(52.579689, 13.449969)
        val expected = vec2(227.1f, 106.5f) * 1000f
        Assert.assertEquals(expected.x, hamburg.eastNorthDistanceTo(berlin).x, delta)
        Assert.assertEquals(expected.y, hamburg.eastNorthDistanceTo(berlin).y, delta)
    }
}