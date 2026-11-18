package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.timestamp

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileTimestampApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.StorageRequestPriority

class FFileTimestampApiNoop : FFileTimestampApi {
    override suspend fun fetchFolderTimestamp(
        folder: String,
        priority: StorageRequestPriority
    ): Long? = null
}
