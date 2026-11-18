package dev.namelessnanashi.flipperzero.share.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class KeyReceiveDecomposeComponent(componentContext: ComponentContext) : ScreenDecomposeComponent(
    componentContext
) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            deeplink: Deeplink.RootLevel.SaveKey,
            onBack: DecomposeOnBackParameter
        ): KeyReceiveDecomposeComponent
    }
}
