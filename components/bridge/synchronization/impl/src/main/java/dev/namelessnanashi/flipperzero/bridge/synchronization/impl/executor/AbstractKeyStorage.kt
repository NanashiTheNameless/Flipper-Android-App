package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.zacsweers.metro.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class StorageType(
    val value: Platform
)

enum class Platform {
    ANDROID,
    FLIPPER
}

interface AbstractKeyStorage {
    suspend fun loadFile(filePath: FlipperFilePath): FlipperKeyContent
    suspend fun modify(filePath: FlipperFilePath, newContent: FlipperKeyContent)
    suspend fun saveFile(filePath: FlipperFilePath, keyContent: FlipperKeyContent)
    suspend fun deleteFile(filePath: FlipperFilePath)
}
