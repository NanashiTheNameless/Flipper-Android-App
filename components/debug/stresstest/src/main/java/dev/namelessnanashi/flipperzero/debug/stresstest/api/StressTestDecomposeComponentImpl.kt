package dev.namelessnanashi.flipperzero.debug.stresstest.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.debug.api.StressTestDecomposeComponent
import dev.namelessnanashi.flipperzero.debug.stresstest.composable.ComposableStressTestScreen
import dev.namelessnanashi.flipperzero.debug.stresstest.viewmodel.StressTestViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import dev.zacsweers.metro.Provider

@ContributesAssistedFactory(AppGraph::class, StressTestDecomposeComponent.Factory::class)
class StressTestDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    private val stressTestViewModelProvider: Provider<StressTestViewModel>
) : StressTestDecomposeComponent(componentContext) {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val stressTestViewModel = viewModelWithFactory(key = null) {
            stressTestViewModelProvider.invoke()
        }
        ComposableStressTestScreen(
            viewModel = stressTestViewModel
        )
    }
}
