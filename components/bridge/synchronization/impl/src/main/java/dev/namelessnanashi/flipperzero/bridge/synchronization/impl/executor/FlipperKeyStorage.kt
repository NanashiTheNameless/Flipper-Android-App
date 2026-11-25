package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDeleteApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDownloadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.StorageRequestPriority
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di.TaskGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.zacsweers.metro.ContributesBinding
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@StorageType(Platform.FLIPPER)
@ContributesBinding(TaskGraph::class, binding<AbstractKeyStorage>())
class FlipperKeyStorage @Inject constructor(
    private val fileDownloadApi: FFileDownloadApi,
    private val fileUploadApi: FFileUploadApi,
    private val fileDeleteApi: FFileDeleteApi
) : AbstractKeyStorage, LogTagProvider {
    override val TAG = "FlipperKeyStorage"

    override suspend fun loadFile(filePath: FlipperFilePath): FlipperKeyContent {
        // Blocking read: keep it off the shared pool
        val responseBytes = withContext(FlipperDispatchers.blockingDispatcher) {
            fileDownloadApi.source(
                pathOnFlipper = filePath.getPathOnFlipper(),
                priority = StorageRequestPriority.BACKGROUND,
                scope = this
            ).buffer().readByteArray()
        }

        return FlipperKeyContent.RawData(responseBytes)
    }

    override suspend fun modify(filePath: FlipperFilePath, newContent: FlipperKeyContent) {
        saveFile(filePath, newContent)
    }

    override suspend fun saveFile(
        filePath: FlipperFilePath,
        keyContent: FlipperKeyContent
    ) = keyContent.openStream().use { stream ->
        // Close the buffered sink, not only the raw one: writeAll keeps a partial (< 8 KiB)
        // segment buffered, and upstream closed the raw sink without it, so small files such as
        // favorites.txt were written as empty files
        fileUploadApi.sink(
            pathOnFlipper = filePath.getPathOnFlipper(),
            priority = StorageRequestPriority.BACKGROUND
        ).buffer().use { sink -> sink.writeAll(stream.source()) }
        return@use
    }

    override suspend fun deleteFile(filePath: FlipperFilePath) {
        fileDeleteApi.delete(
            path = filePath.getPathOnFlipper(),
            priority = StorageRequestPriority.BACKGROUND
        )
    }
}
