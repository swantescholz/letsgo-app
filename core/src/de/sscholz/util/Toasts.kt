package de.sscholz.util


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFontCache
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import java.util.*


object Toasts {
    enum class Duration(val timeInSeconds: Float) {
        SHORT(0.8f),
        DEFAULT(1.6f),
        LONG(3.2f)
    }

    // toasts will be skipped if there are too many. new toasts have preference over older ones
    val maximumNumberOfToastsInQueue = 3

    private data class ToastData(val message: String, val duration: Duration)

    private val unprocessedToastDatas = LinkedList<ToastData>()
    private val toasts = LinkedList<Toast>()

    private val font by lazy { FontFactory.Default.getOrLoadFontWithSize(24) }
    private val backgroundColor by lazy { Color.LIGHT_GRAY }
    private val fontColor by lazy { Color.BLACK }
    private val spriteBatch by lazy { SpriteBatch() }
    private val shapeRenderer by lazy { ShapeRenderer().apply { color = backgroundColor } }

    private var fadingDuration = 0.4f // faceout time
    private var maxRelativeWidth = 0.75f // will linebreak if box would get larger than this otherwise
    private val bottomGap = 50f

    private class Toast(val message: String,
                        duration: Duration = Duration.DEFAULT) {

        private var opacity = 1f
        private var timeToLive = duration.timeInSeconds
        private val toastWidth: Float
        private val toastHeight: Float
        private val positionX: Float
        private val fontX: Float
        private val fontY: Float
        private var textBlockWidth: Float
        private val positionY: Float = bottomGap

        init {
            val layoutSimple = GlyphLayout()
            layoutSimple.setText(font, message)

            var textBlockHeight = layoutSimple.height
            textBlockWidth = layoutSimple.width

            val screenWidth = Gdx.graphics.width.toFloat()
            val maxTextWidth = screenWidth * maxRelativeWidth
            if (textBlockWidth > maxTextWidth) {
                val cache = BitmapFontCache(font, true)
                val layout = cache.addText(message, 0f, 0f, maxTextWidth, Align.center, true)
                textBlockWidth = layout.width
                textBlockHeight = layout.height
            }
            val padding = screenWidth / 50f
            toastHeight = textBlockHeight + 2 * padding
            toastWidth = textBlockWidth + 2 * padding
            positionX = screenWidth / 2 - toastWidth / 2
            fontX = positionX + padding
            fontY = positionY + padding + textBlockHeight
        }

        // returns true if toast is still active
        fun render(deltaTime: Float): Boolean {
            timeToLive -= deltaTime

            if (timeToLive < 0) {
                return false
            }

            GdxUtil.withGlBlendingEnabled {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.color.set(backgroundColor)
                shapeRenderer.color.a *= opacity
                shapeRenderer.rect(positionX, positionY, toastWidth, toastHeight)
                shapeRenderer.end()

                spriteBatch.begin()

                if (timeToLive > 0) {
                    if (timeToLive < fadingDuration) {
                        opacity = timeToLive / fadingDuration
                    }
                    font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * opacity)
                    font.draw(spriteBatch, message, fontX, fontY, textBlockWidth, Align.center, true)
                }
                spriteBatch.end()
            }
            return true
        }

    }

    fun showToast(message: String, duration: Duration = Toasts.Duration.DEFAULT) {
        unprocessedToastDatas.add(ToastData(message, duration))
    }

    private fun update() {
        while (!unprocessedToastDatas.isEmpty()) {
            unprocessedToastDatas.pop().let {
                toasts.add(Toast(it.message, it.duration))
            }
        }
        while (toasts.size > maximumNumberOfToastsInQueue) {
            toasts.removeFirst()
        }
    }

    fun render() {
        update()
        // remove all toasts at the front that are not alive anymore
        while (!toasts.isEmpty() && !toasts.first().render(Gdx.graphics.deltaTime)) {
            toasts.removeFirst()
        }
    }
}