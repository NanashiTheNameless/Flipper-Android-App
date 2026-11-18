package dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.repository

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.set
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import com.flipperdevices.protobuf.screen.ScreenFrame
import com.flipperdevices.protobuf.screen.ScreenOrientation
import dev.namelessnanashi.flipperzero.screenstreaming.impl.model.FlipperScreenState
import dev.namelessnanashi.flipperzero.screenstreaming.impl.model.ScreenOrientationEnum
import kotlin.experimental.and

private const val SCREEN_WIDTH = 128
private const val SCREEN_HEIGHT = 64
const val FLIPPER_SCREEN_RATIO = (SCREEN_WIDTH / SCREEN_HEIGHT).toFloat()
private const val BACKGROUND_COLOR = -0x73d7 // 0xFFFF8C29
private const val SINGLE_BIT = 1
private const val ZERO_BYTE = 0.toByte()
private const val PIXEL_MASK = 7
private const val RAINBOW_MS_PER_DEGREE = 20L
private const val FULL_CIRCLE_DEGREES = 360

/**
 * Momentum sends screen colours as `ScreenFrameColor`, a packed little-endian union: byte 0 is
 * the mode, bytes 1..3 are red, green, blue. Official firmware leaves the fields at 0 (default).
 */
internal object MomentumScreenColor {
    private const val MODE_CUSTOM = 1
    private const val MODE_RAINBOW = 2
    private const val BYTE_MASK = 0xFF
    private const val OPAQUE = 0xFF shl 24

    /**
     * @return ARGB colour, or null to use the app's default
     */
    fun resolve(packed: Int, rainbowHueDegrees: () -> Float): Int? = when (packed and BYTE_MASK) {
        MODE_CUSTOM -> {
            val red = (packed ushr 8) and BYTE_MASK
            val green = (packed ushr 16) and BYTE_MASK
            val blue = (packed ushr 24) and BYTE_MASK
            OPAQUE or (red shl 16) or (green shl 8) or blue
        }
        MODE_RAINBOW -> Color.HSVToColor(floatArrayOf(rainbowHueDegrees(), 1f, 1f))
        else -> null
    }
}

object ScreenStreamFrameDecoder : LogTagProvider {
    override val TAG = "ScreenStreamFrameDecoder"
    fun emptyBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(SCREEN_WIDTH, SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(BACKGROUND_COLOR)
        return bitmap
    }

    fun decode(streamFrame: ScreenFrame): FlipperScreenState.Ready? {
        val bytes = streamFrame.data_.toByteArray()
        info { "Receive package with ${bytes.size} bytes" }
        if (bytes.isEmpty()) {
            return null
        }
        val orientation = when (streamFrame.orientation) {
            ScreenOrientation.HORIZONTAL -> ScreenOrientationEnum.HORIZONTAL
            ScreenOrientation.HORIZONTAL_FLIP -> ScreenOrientationEnum.HORIZONTAL_FLIP
            ScreenOrientation.VERTICAL -> ScreenOrientationEnum.VERTICAL
            ScreenOrientation.VERTICAL_FLIP -> ScreenOrientationEnum.VERTICAL_FLIP
            else -> ScreenOrientationEnum.HORIZONTAL
        }
        val rainbowHue = {
            (System.currentTimeMillis() / RAINBOW_MS_PER_DEGREE % FULL_CIRCLE_DEGREES).toFloat()
        }
        val foreground = MomentumScreenColor.resolve(streamFrame.fg_color, rainbowHue) ?: Color.BLACK
        val background = MomentumScreenColor.resolve(streamFrame.bg_color, rainbowHue) ?: BACKGROUND_COLOR
        val screen = Bitmap.createBitmap(SCREEN_WIDTH, SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)
        fillBitmap(screen, bytes, orientation, foreground, background)
        return FlipperScreenState.Ready(
            bitmap = screen,
            orientation = orientation
        )
    }

    private fun fillBitmap(
        screen: Bitmap,
        bytes: ByteArray,
        orientation: ScreenOrientationEnum,
        foreground: Int,
        background: Int
    ) {
        for (x in 0 until SCREEN_WIDTH) {
            for (y in 0 until SCREEN_HEIGHT) {
                val color = if (bytes.isPixelSet(x, y)) foreground else background
                val bitmapX = when (orientation) {
                    ScreenOrientationEnum.VERTICAL_FLIP,
                    ScreenOrientationEnum.HORIZONTAL_FLIP -> SCREEN_WIDTH - x - 1
                    else -> x
                }
                val bitmapY = when (orientation) {
                    ScreenOrientationEnum.HORIZONTAL_FLIP,
                    ScreenOrientationEnum.VERTICAL_FLIP -> SCREEN_HEIGHT - y - 1
                    else -> y
                }
                screen[bitmapX, bitmapY] = color
            }
        }
    }

    private fun ByteArray.isPixelSet(x: Int, y: Int): Boolean {
        val index = (y / Byte.SIZE_BITS) * SCREEN_WIDTH + x
        val modifiedY = y and PIXEL_MASK
        return get(index) and (SINGLE_BIT.shl(modifiedY).toByte()) != ZERO_BYTE
    }
}
