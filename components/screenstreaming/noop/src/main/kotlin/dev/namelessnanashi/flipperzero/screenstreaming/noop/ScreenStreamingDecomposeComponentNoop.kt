package dev.namelessnanashi.flipperzero.screenstreaming.noop

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.screenstreaming.api.ScreenStreamingDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

private data object MainScreen

@ContributesAssistedFactory(AppGraph::class, ScreenStreamingDecomposeComponent.Factory::class)
class ScreenStreamingDecomposeComponentNoop @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted
    @Suppress("UnusedPrivateProperty", "UNUSED_PARAMETER")
    onBack: DecomposeOnBackParameter
) : ScreenStreamingDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        // Noop
    }
}
