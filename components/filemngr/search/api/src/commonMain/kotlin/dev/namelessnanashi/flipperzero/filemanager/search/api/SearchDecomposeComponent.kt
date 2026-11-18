package dev.namelessnanashi.flipperzero.filemanager.search.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import okio.Path

abstract class SearchDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            path: Path,
            onBack: DecomposeOnBackParameter,
            onFolderSelect: FolderSelectCallback
        ): SearchDecomposeComponent
    }

    fun interface FolderSelectCallback {
        fun invoke(path: Path)
    }
}
