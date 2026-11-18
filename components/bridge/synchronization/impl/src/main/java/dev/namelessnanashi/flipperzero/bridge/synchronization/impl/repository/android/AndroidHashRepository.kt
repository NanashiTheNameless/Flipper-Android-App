package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.android

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di.TaskGraph
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyWithHash
import dev.namelessnanashi.flipperzero.core.ktx.jre.md5
import dev.namelessnanashi.flipperzero.core.ktx.jre.pmap
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

interface AndroidHashRepository {
    suspend fun getHashes(keys: List<FlipperKey>): List<KeyWithHash>
}

@ContributesBinding(TaskGraph::class, binding<AndroidHashRepository>())
class AndroidHashRepositoryImpl @Inject constructor() : AndroidHashRepository {
    override suspend fun getHashes(keys: List<FlipperKey>): List<KeyWithHash> {
        return keys.pmap { flipperKey ->
            flipperKey.additionalFiles.plus(flipperKey.mainFile).map {
                KeyWithHash(
                    it.path,
                    it.content.openStream().md5()
                )
            }
        }.flatten()
    }
}
