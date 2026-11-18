package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.StorageRequestPriority

interface FFileTimestampApi {
    suspend fun fetchFolderTimestamp(
        folder: String,
        priority: StorageRequestPriority = StorageRequestPriority.DEFAULT
    ): Long?
}
