package dev.namelessnanashi.flipperzero.remotecontrols.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class RemoteControlsScreenDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter,
            onDeeplink: (Deeplink.BottomBar) -> Unit
        ): RemoteControlsScreenDecomposeComponent<*>
    }
}
