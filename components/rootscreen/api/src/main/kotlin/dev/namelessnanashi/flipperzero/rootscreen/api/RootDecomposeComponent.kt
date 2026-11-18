package dev.namelessnanashi.flipperzero.rootscreen.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

interface RootDecomposeComponent :
    ComponentContext,
    RootNavigationInterface,
    RootDeeplinkHandler {
    @Composable
    fun Render(modifier: Modifier)

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter,
            initialDeeplink: Deeplink?
        ): RootDecomposeComponent
    }
}
