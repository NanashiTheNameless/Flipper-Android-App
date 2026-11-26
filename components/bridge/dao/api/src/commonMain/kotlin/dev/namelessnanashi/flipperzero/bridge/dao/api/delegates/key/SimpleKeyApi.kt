package dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType
import kotlinx.coroutines.flow.Flow

interface SimpleKeyApi {
    suspend fun getAllKeys(includeDeleted: Boolean = false): List<FlipperKey>

    fun getKeyAsFlow(keyPath: FlipperKeyPath): Flow<FlipperKey?>

    fun getExistKeysAsFlow(
        fileType: FlipperKeyType? = null
    ): Flow<List<FlipperKey>>

    suspend fun getExistKeys(fileType: FlipperKeyType? = null): List<FlipperKey>

    suspend fun insertKey(key: FlipperKey)

    /** Atomically import keys, replacing matching paths while keeping existing key identities. */
    suspend fun restoreKeys(keys: List<FlipperKey>)

    suspend fun updateKeyContent(keyPath: FlipperKeyPath, content: FlipperKeyContent)

    suspend fun getKey(keyPath: FlipperKeyPath): FlipperKey?
}
