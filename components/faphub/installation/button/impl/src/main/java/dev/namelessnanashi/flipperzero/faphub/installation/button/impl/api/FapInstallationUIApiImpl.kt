package dev.namelessnanashi.flipperzero.faphub.installation.button.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonConfig
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapInstallationUIApi
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.states.ComposableFapButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.viewmodel.FapStatusViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.viewmodel.OpenFapViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.api.model.FapState
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FapInstallationUIApi>())
class FapInstallationUIApiImpl @Inject constructor(
    private val openFapViewModelProvider: Provider<OpenFapViewModel>,
    private val fapStatusViewModelProvider: Provider<FapStatusViewModel>
) : FapInstallationUIApi {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun ComposableButton(
        modifier: Modifier,
        componentContext: ComponentContext,
        config: FapButtonConfig?,
        fapButtonSize: FapButtonSize
    ) {
        val statusViewModel = componentContext.viewModelWithFactory(key = null) {
            fapStatusViewModelProvider.invoke()
        }
        val stateFlow = remember(config) {
            statusViewModel.getStateForApplicationId(config)
        }
        val state by stateFlow.collectAsState(FapState.NotInitialized)
        val openViewModel = componentContext.viewModelWithFactory(key = null) {
            openFapViewModelProvider.invoke()
        }

        ComposableFapButton(
            modifier = modifier,
            localState = state,
            fapButtonSize = fapButtonSize,
            statusViewModel = statusViewModel,
            config = config,
            openViewModel = openViewModel
        )
    }
}
