package dev.namelessnanashi.flipperzero.screenstreaming.impl.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactoryWithoutRemember
import dev.namelessnanashi.flipperzero.screenstreaming.api.ScreenStreamingDecomposeComponent
import dev.namelessnanashi.flipperzero.screenstreaming.impl.composable.ComposableStreamingScreen
import dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.ScreenStreamingViewModel
import dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.ScreenshotViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import dev.zacsweers.metro.Provider

@ContributesAssistedFactory(AppGraph::class, ScreenStreamingDecomposeComponent.Factory::class)
class ScreenStreamingDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val screenStreamingViewModelFactory: ScreenStreamingViewModel.Factory,
    private val screenshotViewModelProvider: Provider<ScreenshotViewModel>
) : ScreenStreamingDecomposeComponent(componentContext) {
    private val screenStreamingViewModel = viewModelWithFactoryWithoutRemember(key = this) {
        screenStreamingViewModelFactory(this@ScreenStreamingDecomposeComponentImpl)
    }

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val screenshotViewModel = viewModelWithFactory(null) {
            screenshotViewModelProvider.invoke()
        }

        ComposableStreamingScreen(
            screenStreamingViewModel = screenStreamingViewModel,
            screenshotViewModel = screenshotViewModel,
            onBack = onBack::invoke,
        )
    }
}
