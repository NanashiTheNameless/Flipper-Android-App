package dev.namelessnanashi.flipperzero.toolstab.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class ToolsDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    abstract fun handleDeeplink(deeplink: Deeplink.BottomBar.ToolsTab)

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            deeplink: Deeplink.BottomBar.ToolsTab?,
            onBack: DecomposeOnBackParameter,
            onDeeplink: (Deeplink.BottomBar) -> Unit
        ): ToolsDecomposeComponent<*>
    }
}
