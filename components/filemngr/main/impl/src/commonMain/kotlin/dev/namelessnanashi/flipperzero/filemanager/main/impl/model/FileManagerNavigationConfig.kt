package dev.namelessnanashi.flipperzero.filemanager.main.impl.model

import androidx.compose.runtime.Stable
import dev.namelessnanashi.flipperzero.filemanager.transfer.api.model.TransferType
import dev.namelessnanashi.flipperzero.filemanager.util.serialization.PathSerializer
import kotlinx.serialization.Serializable
import okio.Path
import okio.Path.Companion.toPath

@Stable
@Serializable
sealed interface FileManagerNavigationConfig {
    @Serializable
    data class FileTree(
        @Serializable(with = PathSerializer::class)
        val path: Path
    ) : FileManagerNavigationConfig

    @Serializable
    data class Edit(
        @Serializable(with = PathSerializer::class)
        val path: Path
    ) : FileManagerNavigationConfig

    @Serializable
    data class Search(
        @Serializable(with = PathSerializer::class)
        val path: Path
    ) : FileManagerNavigationConfig

    @Serializable
    data class Transfer(
        @Serializable(with = PathSerializer::class)
        val path: Path,
        val transferType: TransferType,
        val fullPathToMove: List<
            @Serializable(with = PathSerializer::class)
            Path
            >,
    ) : FileManagerNavigationConfig

    companion object {
        val DefaultFileTree: FileTree
            get() = FileTree("/".toPath())
    }
}
