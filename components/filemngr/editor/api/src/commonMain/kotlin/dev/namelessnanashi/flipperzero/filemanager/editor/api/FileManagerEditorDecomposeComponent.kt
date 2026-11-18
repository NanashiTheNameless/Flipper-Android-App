package dev.namelessnanashi.flipperzero.filemanager.editor.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.ListingItem
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import okio.Path

abstract class FileManagerEditorDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter,
            onFileChanged: (ListingItem) -> Unit,
            path: Path
        ): FileManagerEditorDecomposeComponent<*>
    }
}
