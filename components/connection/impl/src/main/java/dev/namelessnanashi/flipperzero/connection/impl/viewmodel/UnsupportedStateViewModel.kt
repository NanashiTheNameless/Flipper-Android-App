package dev.namelessnanashi.flipperzero.connection.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.model.FlipperSupportedState
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.connection.impl.util.getSupportedState
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import dev.zacsweers.metro.Inject

class UnsupportedStateViewModel @Inject constructor(
    private val featureProvider: FFeatureProvider,
) : DecomposeViewModel() {
    private val unsupportedStateFlow = MutableStateFlow(FlipperSupportedState.READY)

    init {
        collectVersionState()
    }

    fun getUnsupportedState(): StateFlow<FlipperSupportedState> = unsupportedStateFlow

    private fun collectVersionState() {
        featureProvider.getSupportedState()
            .onEach { state -> unsupportedStateFlow.emit(state) }
            .launchIn(viewModelScope)
    }
}
