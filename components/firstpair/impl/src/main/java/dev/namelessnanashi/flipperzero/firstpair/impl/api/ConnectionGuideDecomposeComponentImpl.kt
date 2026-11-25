package dev.namelessnanashi.flipperzero.firstpair.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.firstpair.api.ConnectionGuideDecomposeComponent
import dev.namelessnanashi.flipperzero.firstpair.impl.composable.guide.ComposableConnectionGuide
import dev.namelessnanashi.flipperzero.firstpair.impl.viewmodels.guide.ConnectionGuideViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, ConnectionGuideDecomposeComponent.Factory::class)
class ConnectionGuideDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onBack: DecomposeOnBackParameter,
    @Assisted private val onUsbConnected: (() -> Unit)?,
    private val viewModelProvider: Provider<ConnectionGuideViewModel>
) : ConnectionGuideDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val viewModel = viewModelWithFactory(key = null) { viewModelProvider.invoke() }
        val usbNotFound by viewModel.isUsbNotFound().collectAsState()
        val onUsbConnected = onUsbConnected
        ComposableConnectionGuide(
            onBack = onBack::invoke,
            onUseUsb = onUsbConnected?.let { callback -> { viewModel.useUsb(callback) } },
            usbNotFound = usbNotFound
        )
    }
}
