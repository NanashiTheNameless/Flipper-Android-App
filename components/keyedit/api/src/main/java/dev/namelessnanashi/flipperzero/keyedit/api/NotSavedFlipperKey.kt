package dev.namelessnanashi.flipperzero.keyedit.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import kotlinx.serialization.Serializable

@Serializable
data class NotSavedFlipperKey(
    val mainFile: NotSavedFlipperFile,
    val additionalFiles: List<NotSavedFlipperFile>,
    val notes: String?
)

@Serializable
data class NotSavedFlipperFile(
    val path: FlipperFilePath,
    val content: FlipperKeyContent
)
