package dev.namelessnanashi.flipperzero.filemanager.create.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.ListingItem
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import kotlinx.coroutines.flow.StateFlow
import okio.Path
import uk.kulikov.metro.assisted.AssistedKey

abstract class CreateFileDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    abstract val canCreateFiles: StateFlow<Boolean>

    abstract fun startCreateFile(parent: Path)

    abstract fun startCreateFolder(parent: Path)

    abstract fun startCreate(parent: Path, type: FileType)

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @AssistedKey("createdCallback") createCallback: CreatedCallback,
        ): CreateFileDecomposeComponent
    }

    fun interface CreatedCallback {
        fun invoke(item: ListingItem)
    }
}
