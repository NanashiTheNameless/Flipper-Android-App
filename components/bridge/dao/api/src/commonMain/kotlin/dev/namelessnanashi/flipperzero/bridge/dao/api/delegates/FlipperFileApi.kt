package dev.namelessnanashi.flipperzero.bridge.dao.api.delegates

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent

interface FlipperFileApi {
    suspend fun getFile(filePath: FlipperFilePath): FlipperFile
    suspend fun deleteFile(filePath: FlipperFilePath)
    suspend fun insert(file: FlipperFile)
    suspend fun updateFileContent(filePath: FlipperFilePath, newContent: FlipperKeyContent)
    suspend fun updateAdditionalFiles(id: Int, newAdditionalFiles: List<FlipperFile>)
    suspend fun renameAdditionalFiles(id: Int, newKey: FlipperKey)
}
