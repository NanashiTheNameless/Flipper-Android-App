package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class LocalGridScreenDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            keyPath: FlipperKeyPath,
            onBack: DecomposeOnBackParameter,
            onCallback: (Callback) -> Unit
        ): LocalGridScreenDecomposeComponent
    }

    sealed interface Callback {
        data object UiFileNotFound : Callback
        data object Deleted : Callback
        data class Rename(val keyPath: FlipperKeyPath) : Callback
        data class ViewRemoteInfo(val keyPath: FlipperKeyPath) : Callback
    }
}
