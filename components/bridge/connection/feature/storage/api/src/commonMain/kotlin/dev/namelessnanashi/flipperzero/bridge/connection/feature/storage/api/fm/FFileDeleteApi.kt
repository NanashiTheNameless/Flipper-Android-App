package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.StorageRequestPriority

interface FFileDeleteApi {
    suspend fun delete(
        path: String,
        recursive: Boolean = false,
        priority: StorageRequestPriority = StorageRequestPriority.DEFAULT
    ): Result<Unit>
}
