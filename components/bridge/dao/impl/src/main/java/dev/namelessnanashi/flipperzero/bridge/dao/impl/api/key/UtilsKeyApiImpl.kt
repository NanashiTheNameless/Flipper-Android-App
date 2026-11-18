package dev.namelessnanashi.flipperzero.bridge.dao.impl.api.key

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.UtilsKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.dao.impl.ktx.toFlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.SynchronizedStatus
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.AdditionalFileDao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.key.UtilsKeyDao
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<UtilsKeyApi>())
class UtilsKeyApiImpl @Inject constructor(
    utilsKeysDaoProvider: Provider<UtilsKeyDao>,
    simpleKeyApiProvider: Provider<SimpleKeyApi>,
    flipperAdditionalDaoProvider: Provider<AdditionalFileDao>
) : UtilsKeyApi, LogTagProvider {
    override val TAG = "UtilsKeyApi"

    private val utilsKeyDao by utilsKeysDaoProvider
    private val simpleKeyApi by simpleKeyApiProvider
    private val flipperAdditionalDao by flipperAdditionalDaoProvider

    override suspend fun markAsSynchronized(
        keyPath: FlipperKeyPath
    ) = withContext(FlipperDispatchers.workStealingDispatcher) {
        utilsKeyDao.markSynchronized(
            keyPath.path.pathToKey,
            keyPath.deleted,
            SynchronizedStatus.SYNCHRONIZED
        )
    }

    override suspend fun updateNote(
        keyPath: FlipperKeyPath,
        note: String
    ) = withContext(FlipperDispatchers.workStealingDispatcher) {
        utilsKeyDao.updateNote(keyPath.path.pathToKey, keyPath.deleted, note)
    }

    override fun search(text: String): Flow<List<FlipperKey>> {
        val searchQuery = "%$text%"
        return utilsKeyDao.search(searchQuery).map { list ->
            list.map { it.toFlipperKey(flipperAdditionalDao) }
        }
    }

    override suspend fun findAvailablePath(keyPath: FlipperKeyPath): FlipperKeyPath {
        var newNameWithoutExtension = keyPath.path.nameWithoutExtension
        var newPath = getKeyPathWithDifferentNameWithoutExtension(
            keyPath,
            newNameWithoutExtension
        )
        var index = 1
        info {
            "Start finding free name for path $newPath " +
                "(newNameWithoutExtension=$newNameWithoutExtension)"
        }
        // Find empty key name
        while (simpleKeyApi.getKey(newPath) != null) {
            newNameWithoutExtension = "${keyPath.path.nameWithoutExtension}_${index++}"
            newPath = getKeyPathWithDifferentNameWithoutExtension(
                keyPath,
                newNameWithoutExtension
            )
            info {
                "Try $newPath ($newNameWithoutExtension)"
            }
        }
        info { "Found free key name! $newPath" }
        return newPath
    }
}

private fun getKeyPathWithDifferentNameWithoutExtension(
    keyPath: FlipperKeyPath,
    nameWithoutExtension: String
): FlipperKeyPath {
    return FlipperKeyPath(
        FlipperFilePath(
            keyPath.path.folder,
            "$nameWithoutExtension.${keyPath.path.nameWithExtension.substringAfterLast('.')}"
        ),
        keyPath.deleted
    )
}
