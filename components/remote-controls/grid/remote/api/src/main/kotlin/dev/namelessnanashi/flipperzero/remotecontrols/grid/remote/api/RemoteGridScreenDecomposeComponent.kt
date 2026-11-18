package dev.namelessnanashi.flipperzero.remotecontrols.grid.remote.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKey
import dev.namelessnanashi.flipperzero.remotecontrols.grid.remote.api.model.ServerRemoteControlParam
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class RemoteGridScreenDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            param: ServerRemoteControlParam,
            onBack: DecomposeOnBackParameter,
            onSaveKey: (NotSavedFlipperKey) -> Unit
        ): RemoteGridScreenDecomposeComponent
    }
}
