package dev.namelessnanashi.flipperzero.share.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent

interface CryptoStorageApi {
    suspend fun upload(keyContent: FlipperKeyContent, path: String, name: String): Result<String>
    suspend fun download(
        id: String,
        key: String,
        name: String,
    ): Result<FlipperKeyContent>
}
