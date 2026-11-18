package dev.namelessnanashi.flipperzero.filemanager.listing.impl.model

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.filemanager.util.serialization.PathSerializer
import kotlinx.serialization.Serializable
import okio.Path

@Serializable
data class PathWithType(
    val fileType: FileType,
    @Serializable(PathSerializer::class)
    val fullPath: Path,
    val size: Long
)
