package de.sscholz.util

import com.badlogic.gdx.math.Vector2
import java.util.*

data class Coordi(val x: Int, val y: Int) : Comparable<Coordi> {

    companion object {
        val Nneese4 = arrayOf(Coordi(0, 1), Coordi(1, 1), Coordi(1, 0), Coordi(1, -1))
        val Nesw4 = arrayOf(Coordi(0, 1), Coordi(1, 0), Coordi(0, -1), Coordi(-1, 0))
        val Nesw8 = arrayOf(
                Coordi(0, 1), Coordi(1, 1), Coordi(1, 0), Coordi(1, -1),
                Coordi(0, -1), Coordi(-1, -1), Coordi(-1, 0), Coordi(-1, 1))

        inline fun fromIndex(cellIndex: Int, gridWidth: Int) = Coordi(cellIndex % gridWidth, cellIndex / gridWidth)
    }

    override fun compareTo(other: Coordi): Int {
        if (y < other.y)
            return -1
        if (y > other.y)
            return 1
        return x.compareTo(other.x)
    }

    operator fun unaryMinus(): Coordi = Coordi(-x, -y)

    operator fun plus(o: Coordi): Coordi = Coordi(x + o.x, y + o.y)

    operator fun minus(o: Coordi): Coordi = Coordi(x - o.x, y - o.y)

    operator fun times(o: Int): Coordi = Coordi(x * o, y * o)
    operator fun times(o: Coordi): Coordi = Coordi(x * o.x, y * o.y)

    fun rotateLeft() = Coordi(-y, x)
    fun rotateRight() = Coordi(y, -x)

    fun min(o: Coordi) = Coordi(Math.min(x, o.x), Math.min(y, o.y))
    fun max(o: Coordi) = Coordi(Math.max(x, o.x), Math.max(y, o.y))
    fun toIndex(gridWidth: Int) = y * gridWidth + x

    val vec2: Vector2
        get() = Vector2(x.f, y.f)

    override fun toString() = "($x, $y)"

    val neighborsNesw4: ArrayList<Coordi> // gives north, east, south and west neighbors
        get() = arrayListOf(Coordi(x, y + 1), Coordi(x + 1, y), Coordi(x, y - 1), Coordi(x - 1, y))
    val neighborsNESeSWNw6: ArrayList<Coordi> // gives north, east, southeast, south, west and northwest neighbors
        get() = arrayListOf(Coordi(x, y + 1), Coordi(x + 1, y), Coordi(x + 1, y - 1),
                Coordi(x, y - 1), Coordi(x - 1, y), Coordi(x - 1, y + 1))
}

operator fun Int.times(o: Coordi) = Coordi(o.x * this, o.y * this)