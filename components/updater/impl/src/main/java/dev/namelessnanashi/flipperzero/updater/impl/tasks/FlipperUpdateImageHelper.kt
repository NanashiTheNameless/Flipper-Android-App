package dev.namelessnanashi.flipperzero.updater.impl.tasks

import android.content.Context
import dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api.DisplayApi
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import dev.zacsweers.metro.Inject
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

private const val STOP_IMAGE_TIMEOUT_MS = 5 * 1000L

class FlipperUpdateImageHelper @Inject constructor(
    private val context: Context,
) : LogTagProvider {
    override val TAG = "FlipperUpdateImageHelper"

    suspend fun loadImageOnFlipper(displayApi: DisplayApi) {
        info { "Start streaming" }
        val bytes = loadImageFromResource()
        displayApi.startVirtualDisplay(bytes)
            .onFailure { error(it) { "#loadImageOnFlipper could not start virtual display" } }
    }

    suspend fun stopImageOnFlipperSafe(displayApi: DisplayApi) = try {
        info { "Request stop streaming" }
        withTimeout(STOP_IMAGE_TIMEOUT_MS) {
            displayApi.stopVirtualDisplay()
                .onFailure { error(it) { "#loadImageOnFlipper could not stop virtual display" } }
        }
    } catch (e: Exception) {
        error(e) { "Error while stop streaming" }
    }

    private suspend fun loadImageFromResource(): ByteArray =
        withContext(FlipperDispatchers.workStealingDispatcher) {
            val pictureStream = context.resources.openRawResource(DesignSystem.raw.update_pic)
            return@withContext pictureStream.use { inputStream ->
                inputStream.readBytes()
            }
        }
}
