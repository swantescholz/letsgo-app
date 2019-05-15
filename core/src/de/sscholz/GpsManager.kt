package de.sscholz

import com.badlogic.gdx.math.Vector2
import de.sscholz.util.*
import ktx.math.*

class GpsManager(val boardSize: Int) {
    private var compassRotation: Int = 0
    private var originalCenter = if (boardSize % 2 == 0) vec2(-0.5f, -0.5f) else vec2(0f, 0f)
    // e.g. for 5x5: (0f,0f) = center of center cell, (2.5f,2.5f) = north east corner
    private var currentRelativeCellDelta: Vector2 = vec2(0f, 0f)
    private var relativeCellOrigin: Vector2 = vec2(0f, 0f)
    private var originLatLong: LatLong = Global.gpsTracker.currentLocation
    private var currentLatLong: LatLong = Global.gpsTracker.currentLocation
    private val northEastCorner = vec2(1f, 1f) * (0.5f * boardSize)

    init {
        recenterToCurrentCellCenter()
    }

    fun recenterToCurrentCellCenter() {
        val (px, py) = boardCoordToMove(coordToBoordCoord(rotatedRelativeDelta() + relativeCellOrigin))
        relativeCellOrigin = northEastCorner * -1 + vec2(0.5f, 0.5f) + vec2(px.f, py.f)
        currentRelativeCellDelta = vec2(0f, 0f)
        recenterGpsToLastMove()
        updatePositionViaGps(0)
    }

    private fun rotatedRelativeDelta(): Vector2 {
        var res = (currentRelativeCellDelta * 1f)
        compassRotation.times {
            res = res.rotate90(1)
        }
        return res
    }

    fun updatePositionViaGps(newCompassRotation: Int) {
        compassRotation = newCompassRotation
        currentLatLong = Global.gpsTracker.currentLocation
        val deltaMeterNorthEast = originLatLong.eastNorthDistanceTo(currentLatLong)
        currentRelativeCellDelta = deltaMeterNorthEast / Preferences.cellMeterWidth.get()
//        currentRelativeCellPosition = currentRelativeCellPosition.max(northEastCorner * -0.999f).min(northEastCorner * 0.999f)
    }

    fun recenterGpsToLastMove() {
        originLatLong = currentLatLong
    }

    private fun coordToBoordCoord(xy: Vector2): Vector2 {
        return (xy / boardSize + vec2(0.5f, 0.5f)) * BoardView.cellWidth * boardSize
    }

    fun getPositionInBoardCoordinates(): Vector2 {
        return coordToBoordCoord(rotatedRelativeDelta() + relativeCellOrigin)
    }

    fun boardCoordToMove(xy: Vector2): Coordi {
        var ix = (xy.x / BoardView.cellWidth).i
        var iy = (xy.y / BoardView.cellWidth).i
        ix = Math.max(0, Math.min(boardSize - 1, ix))
        iy = Math.max(0, Math.min(boardSize - 1, iy))
        return Coordi(ix, iy)
    }

    fun resetOriginToLastMoveCellCoordinates(lastMove: Coordi) {
        relativeCellOrigin = vec2(lastMove.x.f, lastMove.y.f) + vec2(0.5f, 0.5f) - northEastCorner
        currentRelativeCellDelta = vec2(0f, 0f)
        recenterToCurrentCellCenter()
    }

    fun resetToOriginalCenter() {
        currentRelativeCellDelta = vec2(0f, 0f)
        relativeCellOrigin = originalCenter
        recenterToCurrentCellCenter()
    }
}

