package dev.namelessnanashi.flipperzero.bridge.dao.impl.api.delegates

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.key.DeleteKeyDao
import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.zacsweers.metro.ContributesBinding
import java.io.File
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<KeyContentCleaner>())
class KeyContentCleanerImpl @Inject constructor(
    private val deleteKeyDaoProvider: Provider<DeleteKeyDao>,
    flipperStorageProvider: FlipperStorageProvider
) : KeyContentCleaner, LogTagProvider {
    override val TAG = "KeyContentCleaner"
    private val keyFolder = flipperStorageProvider.getKeyFolder().toFile()

    private val deleteKeyDao by deleteKeyDaoProvider

    override suspend fun deleteUnusedFiles() {
        val remainingHashes = keyFolder.listFiles()?.map { it.absolutePath }?.toHashSet() ?: return
        deleteKeyDao.getAllWithDeleted().forEach {
            val path = (it.content.flipperContent as? FlipperKeyContent.InternalFile)
                ?: return@forEach
            remainingHashes.remove(path.path)
        }
        info { "Found ${remainingHashes.size} file to deleted" }
        remainingHashes.forEach {
            File(it).delete()
        }
    }
}
