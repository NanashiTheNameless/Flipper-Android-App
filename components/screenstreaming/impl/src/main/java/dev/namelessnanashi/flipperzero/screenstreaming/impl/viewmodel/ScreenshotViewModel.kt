package dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import dev.namelessnanashi.flipperzero.core.ktx.android.BitmapKtx.rescale
import dev.namelessnanashi.flipperzero.core.ktx.jre.createClearNewFileWithMkDirs
import dev.namelessnanashi.flipperzero.core.share.SharableFile
import dev.namelessnanashi.flipperzero.core.share.ShareHelper
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.screenstreaming.impl.R
import dev.namelessnanashi.flipperzero.screenstreaming.impl.model.FlipperScreenState
import dev.namelessnanashi.flipperzero.screenstreaming.impl.model.ScreenOrientationEnum
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dev.zacsweers.metro.Inject

private const val SCREENSHOT_FILE_PREFIX = "flpr"
private const val TIMEFORMAT = "yyyy-MM-dd-HH-mm-ss"
private const val QUALITY = 100
private const val EXPORT_RESCALE_MULTIPLIER = 8f

class ScreenshotViewModel @Inject constructor(
    private val application: Application
) : DecomposeViewModel() {
    fun shareScreenshot(
        screenShapshot: FlipperScreenState
    ) = viewModelScope.launch {
        val currentSnapshotState = screenShapshot as? FlipperScreenState.Ready ?: return@launch
        var currentSnapshot = currentSnapshotState.bitmap
        currentSnapshot = currentSnapshot.rescale(EXPORT_RESCALE_MULTIPLIER)
        currentSnapshot = when (screenShapshot.orientation) {
            ScreenOrientationEnum.VERTICAL,
            ScreenOrientationEnum.VERTICAL_FLIP -> currentSnapshot.rotate(angel = 90f)
            ScreenOrientationEnum.HORIZONTAL,
            ScreenOrientationEnum.HORIZONTAL_FLIP -> currentSnapshot
        }
        val date = SimpleDateFormat(TIMEFORMAT, Locale.US).format(Date())
        val filename = "$SCREENSHOT_FILE_PREFIX-$date.png"
        val sharableFile = SharableFile(application, filename)
        sharableFile.createClearNewFileWithMkDirs()
        sharableFile.outputStream().use {
            currentSnapshot.compress(Bitmap.CompressFormat.PNG, QUALITY, it)
        }
        ShareHelper.shareFile(application, sharableFile, R.string.screenshot_export_title)
    }
}

private fun Bitmap.rotate(angel: Float): Bitmap {
    val matrix = Matrix()

    matrix.postRotate(angel)

    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)

    return Bitmap.createBitmap(
        scaledBitmap,
        0,
        0,
        scaledBitmap.width,
        scaledBitmap.height,
        matrix,
        true
    )
}
