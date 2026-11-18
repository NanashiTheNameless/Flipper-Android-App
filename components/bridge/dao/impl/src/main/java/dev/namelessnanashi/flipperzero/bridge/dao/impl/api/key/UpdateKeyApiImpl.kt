package dev.namelessnanashi.flipperzero.bridge.dao.impl.api.key

import androidx.room.withTransaction
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FlipperFileApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.UpdateKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.dao.impl.AppDatabase
import dev.namelessnanashi.flipperzero.bridge.dao.impl.api.key.ShouldSynchronizeHelper.isShouldSynchronize
import dev.namelessnanashi.flipperzero.bridge.dao.impl.ktx.toDatabaseKey
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.SynchronizedStatus
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.key.SimpleKeyDao
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.ktx.jre.withFirstElement
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<UpdateKeyApi>())
class UpdateKeyApiImpl @Inject constructor(
    keysDaoProvider: Provider<SimpleKeyDao>,
    additionalFileApiProvider: Provider<FlipperFileApi>,
    databaseProvider: Provider<AppDatabase>
) : UpdateKeyApi, LogTagProvider {
    override val TAG = "UpdateKeyApi"

    private val simpleKeyDao by keysDaoProvider
    private val additionalFileApi by additionalFileApiProvider
    private val database by databaseProvider

    private val updatePathFlow = MutableSharedFlow<Pair<FlipperKeyPath, FlipperKeyPath>>()

    override fun subscribeOnUpdatePath(originalPath: FlipperKeyPath): Flow<FlipperKeyPath> {
        var lastRememberPath = originalPath
        return updatePathFlow.filter {
            it.first == lastRememberPath
        }.map {
            lastRememberPath = it.second
            return@map it.second
        }.withFirstElement(originalPath)
    }

    override suspend fun updateKey(
        oldKey: FlipperKey,
        newKey: FlipperKey
    ) = withContext(FlipperDispatchers.workStealingDispatcher) {
        if (oldKey == newKey) return@withContext

        val isShouldSynchronize = isShouldSynchronize(oldKey, newKey)

        database.withTransaction {
            val oldKeyInDatabase = simpleKeyDao.getByPath(oldKey.path.pathToKey, deleted = false)
                ?: throw IllegalArgumentException("Can't find old key in database")

            val newKeyInDatabase = newKey.toDatabaseKey().copy(
                uid = oldKeyInDatabase.uid,
                synchronizedStatus = if (isShouldSynchronize) {
                    SynchronizedStatus.NOT_SYNCHRONIZED
                } else {
                    SynchronizedStatus.SYNCHRONIZED
                }
            )
            if (oldKey.additionalFiles != newKey.additionalFiles) {
                additionalFileApi.updateAdditionalFiles(
                    oldKeyInDatabase.uid,
                    newKey.additionalFiles
                )
            }
            if (oldKey.path != newKey.path) {
                additionalFileApi.renameAdditionalFiles(oldKeyInDatabase.uid, newKey)
            }
            simpleKeyDao.update(newKeyInDatabase)
        }
        if (oldKey.getKeyPath() != newKey.getKeyPath()) {
            updatePathFlow.emit(oldKey.getKeyPath() to newKey.getKeyPath())
        }
    }
}
