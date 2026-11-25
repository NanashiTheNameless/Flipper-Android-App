package dev.namelessnanashi.flipperzero.firstpair.impl.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.firstpair.api.ForkNoticeDecomposeComponent
import dev.namelessnanashi.flipperzero.firstpair.impl.composable.tos.ComposableTOS
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, ForkNoticeDecomposeComponent.Factory::class)
class ForkNoticeDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onClose: () -> Unit
) : ForkNoticeDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        ComposableTOS(onApplyPress = onClose)
    }
}
