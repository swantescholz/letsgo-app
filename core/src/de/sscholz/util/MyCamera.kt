package de.sscholz.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import de.sscholz.Global
import de.sscholz.extensions.toVector2
import de.sscholz.extensions.toVector3
import ktx.math.plus
import ktx.math.vec2


object camera2 {

    val viewportScreenWidth by lazy { screenWidth }
    val viewportScreenHeight by lazy { screenHeight - hudTopTotalHeight }
    val viewportScreenDyBottom by lazy { 0f }
    val viewportScreenDx by lazy { 0f }
    private val orthoCam = OrthographicCamera(viewportScreenWidth, viewportScreenHeight)

    val heightToWidthRatio: Float
        get() = orthoCam.viewportHeight / orthoCam.viewportWidth
    val viewportWidth: Float get() = orthoCam.viewportWidth
    val viewportHeight: Float get() = orthoCam.viewportHeight
    val combinedMatrix: Matrix4 get() = orthoCam.combined
    val position: Vector2 get() = vec2(orthoCam.position.x, orthoCam.position.y)
    private lateinit var viewport: Viewport

    fun screenToWorldCoordinates(screenXInPx: Float, screenYInPx: Float): Vector2 {
        val xNormalized = (screenXInPx - viewportScreenDx) / viewportScreenWidth - 0.5f
        val yNormalized = (Gdx.graphics.height - screenYInPx - 1 -
                viewportScreenDyBottom) / viewportScreenHeight - 0.5f
        return vec2(xNormalized * viewportWidth, yNormalized * viewportHeight) + position
    }

    fun currentMouseWorldCoordinates(): Vector2 {
        return screenToWorldCoordinates(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
    }

    fun moveTo(newWorldXy: Vector2) {
        orthoCam.position.set(newWorldXy.x, newWorldXy.y, 0f)
        update()
    }

    fun moveBy(deltaXy: Vector2) {
        orthoCam.position.set(orthoCam.position.x + deltaXy.x, orthoCam.position.y + deltaXy.y, 0f)
        update()
    }

    // factor > 1.0 -> zoom out, factor < 1.0 -> zoom in
    fun zoomOut(factor: Float, targetPositionInWorldCoordinates: Vector2) {
        orthoCam.viewportWidth *= factor
        orthoCam.viewportHeight *= factor
        orthoCam.position.set(Vector2(targetPositionInWorldCoordinates).apply {
            lerp(orthoCam.position.toVector2(), factor)
        }.toVector3())
        update()
    }

    // updates camera2 matrix and assign viewport width/height info to viewport instance
    private fun update() {
        orthoCam.update()
        viewport.worldWidth = orthoCam.viewportWidth
        viewport.worldHeight = orthoCam.viewportHeight
    }

    fun rotate(angleDegrees: Float) {
        orthoCam.up.set(0f, 1f, 0f)
        orthoCam.direction.set(0f, 0f, -1f)
        orthoCam.rotate(angleDegrees)
    }

    fun apply() {
        update()
        viewport.apply()
        Global.shapeRenderer.projectionMatrix = orthoCam.combined
    }

    fun setNewViewportWorldWidth(newPosition: Vector2, newViewportWidthInUnits: Float) {
        if (!::viewport.isInitialized) {
            initCamera(newViewportWidthInUnits)
        }
        log("camera2. set viewport width")
        orthoCam.viewportHeight = newViewportWidthInUnits * 1f * heightToWidthRatio
        orthoCam.viewportWidth = newViewportWidthInUnits
        orthoCam.position.set(newPosition.x, newPosition.y, 0f)
        update()
    }

    fun initCamera(viewportWidthInUnits: Float) {
        orthoCam.viewportHeight = viewportWidthInUnits * 1f * viewportScreenHeight / viewportScreenWidth
        orthoCam.viewportWidth = viewportWidthInUnits
        viewport = FitViewport(orthoCam.viewportWidth, orthoCam.viewportHeight, orthoCam)
        viewport.setScreenBounds(viewportScreenDx.toInt(), viewportScreenDyBottom.toInt(),
                viewportScreenWidth.toInt(), viewportScreenHeight.toInt())
        update()
    }

    override fun toString(): String {
        return "pos=$position,width=$viewportWidth"
    }

}

//object camera3 {
//
//    val viewportScreenWidth by lazy { screenWidth }
//    val viewportScreenHeight by lazy { screenHeight - hudTopTotalHeight }
//    val viewportScreenDyBottom by lazy { 0f }
//    val viewportScreenDx by lazy { 0f }
//    private val projCam by lazy {
//        val aspectRatio = viewportScreenWidth / viewportScreenHeight
//        return@lazy PerspectiveCamera(67f, 2f * aspectRatio, 2f)
//    }
//
//    val heightToWidthRatio: Float
//        get() = projCam.viewportHeight / projCam.viewportWidth
//    val viewportWidth: Float get() = projCam.viewportWidth
//    val viewportHeight: Float get() = projCam.viewportHeight
//    val combinedMatrix: Matrix4 get() = projCam.combined
//    val position: Vector3 get() = projCam.position!!
//    private lateinit var viewport: Viewport
//
//    fun moveTo(newWorldXyz: Vector3) {
//        projCam.position.set(newWorldXyz.x, newWorldXyz.y, newWorldXyz.z)
//        update()
//    }
//
//    // updates camera matrix and assign viewport width/height info to viewport instance
//    private fun update() {
//        projCam.update()
//        viewport.worldWidth = projCam.viewportWidth
//        viewport.worldHeight = projCam.viewportHeight
//    }
//
//    fun apply() {
//        update()
//        viewport.apply()
//        Global.shapeRenderer.projectionMatrix = projCam.combined
//    }
//
//    fun initCamera(viewportWidthInUnits: Float) {
//        projCam.viewportHeight = viewportWidthInUnits * 1f * viewportScreenHeight / viewportScreenWidth
//        projCam.viewportWidth = viewportWidthInUnits
//        viewport = FitViewport(projCam.viewportWidth, projCam.viewportHeight, projCam)
//        viewport.setScreenBounds(viewportScreenDx.toInt(), viewportScreenDyBottom.toInt(),
//                viewportScreenWidth.toInt(), viewportScreenHeight.toInt())
//        update()
//    }
//
//}