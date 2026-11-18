package dev.namelessnanashi.flipperzero.bridge.dao.impl.api.key

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType
import dev.namelessnanashi.flipperzero.bridge.dao.impl.ktx.toDatabaseFile
import dev.namelessnanashi.flipperzero.bridge.dao.impl.ktx.toDatabaseKey
import dev.namelessnanashi.flipperzero.bridge.dao.impl.ktx.toFlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.DatabaseKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.AdditionalFileDao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.key.SimpleKeyDao
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<SimpleKeyApi>())
class SimpleKeyApiImpl @Inject constructor(
    keysDaoProvider: Provider<SimpleKeyDao>,
    additionalFileDaoProvider: Provider<AdditionalFileDao>
) : SimpleKeyApi, LogTagProvider {
    override val TAG = "SimpleKeyApi"

    private val simpleKeyDao by keysDaoProvider
    private val additionalFileDao by additionalFileDaoProvider

    override suspend fun getAllKeys(
        includeDeleted: Boolean
    ): List<FlipperKey> = withContext(FlipperDispatchers.workStealingDispatcher) {
        return@withContext if (includeDeleted) {
            simpleKeyDao.getAllIncludeDeleted()
        } else {
            simpleKeyDao.getAll()
        }.map { it.toFlipperKey(additionalFileDao) }
    }

    override suspend fun getExistKeys(
        fileType: FlipperKeyType?
    ): List<FlipperKey> = withContext(FlipperDispatchers.workStealingDispatcher) {
        return@withContext if (fileType == null) {
            simpleKeyDao.getAll()
        } else {
            simpleKeyDao.getByType(fileType)
        }.map { it.toFlipperKey(additionalFileDao) }
    }

    override fun getKeyAsFlow(keyPath: FlipperKeyPath): Flow<FlipperKey?> {
        return simpleKeyDao.getByPathFlow(keyPath.path.pathToKey, keyPath.deleted).map {
            it?.toFlipperKey(additionalFileDao)
        }
    }

    override fun getExistKeysAsFlow(
        fileType: FlipperKeyType?
    ): Flow<List<FlipperKey>> {
        val flowWithFilter = if (fileType == null) {
            simpleKeyDao.subscribe()
        } else {
            simpleKeyDao.subscribeByType(fileType)
        }
        return flowWithFilter.map { keys ->
            keys.map { it.toFlipperKey(additionalFileDao) }
        }
    }

    override suspend fun insertKey(key: FlipperKey) = withContext(FlipperDispatchers.workStealingDispatcher) {
        simpleKeyDao.insert(key.toDatabaseKey())
        simpleKeyDao.getByPath(path = key.path.pathToKey, deleted = key.deleted)?.uid?.let { uid ->
            key.additionalFiles
                .map { it.toDatabaseFile(uid) }
                .forEach { additionalFileDao.insert(it) }
        }
        Unit
    }

    override suspend fun updateKeyContent(keyPath: FlipperKeyPath, content: FlipperKeyContent) {
        val existedKey = simpleKeyDao.getByPath(keyPath.path.pathToKey, keyPath.deleted)
            ?: error("Can't find $keyPath")
        simpleKeyDao.update(existedKey.copy(content = DatabaseKeyContent(content)))
    }

    override suspend fun getKey(
        keyPath: FlipperKeyPath
    ): FlipperKey? = withContext(FlipperDispatchers.workStealingDispatcher) {
        return@withContext simpleKeyDao.getByPath(keyPath.path.pathToKey, keyPath.deleted)
            ?.toFlipperKey(additionalFileDao)
    }
}
