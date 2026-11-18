package dev.namelessnanashi.flipperzero.filemanager.rename.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import okio.Path

abstract class RenameDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {

    abstract fun startRename(fullPath: Path, type: FileType)

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            renamedCallback: RenamedCallback,
        ): RenameDecomposeComponent
    }

    fun interface RenamedCallback {
        fun invoke(oldFullPath: Path, newFullPath: Path)
    }
}
