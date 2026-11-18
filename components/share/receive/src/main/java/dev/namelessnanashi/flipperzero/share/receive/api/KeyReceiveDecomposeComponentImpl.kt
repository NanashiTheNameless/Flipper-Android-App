package dev.namelessnanashi.flipperzero.share.receive.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.keyscreen.api.KeyScreenApi
import dev.namelessnanashi.flipperzero.share.api.KeyReceiveDecomposeComponent
import dev.namelessnanashi.flipperzero.share.receive.composable.ComposableKeyReceive
import dev.namelessnanashi.flipperzero.share.receive.viewmodels.KeyReceiveViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, KeyReceiveDecomposeComponent.Factory::class)
class KeyReceiveDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val deeplink: Deeplink.RootLevel.SaveKey,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val keyScreenApi: KeyScreenApi,
    private val keyReceiveViewModelFactory: KeyReceiveViewModel.Factory
) : KeyReceiveDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val viewModel: KeyReceiveViewModel = viewModelWithFactory(key = deeplink.toString()) {
            keyReceiveViewModelFactory(deeplink)
        }
        val state by viewModel.getState().collectAsState()
        ComposableKeyReceive(
            keyScreenApi = keyScreenApi,
            onCancel = {
                viewModel.onFinish()
                onBack()
            },
            state = state,
            onRetry = viewModel::onRetry,
            onSave = viewModel::onSave
        )
    }
}
