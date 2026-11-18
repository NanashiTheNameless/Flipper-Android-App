package dev.namelessnanashi.flipperzero.keyedit.impl.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperFile
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKeyApi
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<NotSavedFlipperKeyApi>())
class NotSavedFlipperKeyApiImpl @Inject constructor(
    private val storageProvider: FlipperStorageProvider
) : NotSavedFlipperKeyApi {
    override suspend fun toNotSavedFlipperFile(
        flipperFile: FlipperFile
    ): NotSavedFlipperFile = withContext(FlipperDispatchers.workStealingDispatcher) {
        val localContent = flipperFile.content
        if (localContent is FlipperKeyContent.InternalFile) {
            return@withContext NotSavedFlipperFile(flipperFile.path, localContent)
        }
        val internalFile = storageProvider.getTemporaryFile().toFile()
        localContent.openStream().use { contentStream ->
            internalFile.outputStream().use { fileStream ->
                contentStream.copyTo(fileStream)
            }
        }
        return@withContext NotSavedFlipperFile(
            flipperFile.path,
            FlipperKeyContent.InternalFile(internalFile.absolutePath)
        )
    }
}
