package dev.namelessnanashi.flipperzero.info.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api.FVersionFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.model.FlipperSupportedState
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import dev.zacsweers.metro.Inject

class FirmwareUpdateViewModel @Inject constructor(
    private val fFeatureProvider: FFeatureProvider
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "FirmwareUpdateViewModel"

    fun getState(): StateFlow<FlipperSupportedState> {
        return fFeatureProvider.get<FVersionFeatureApi>().flatMapLatest { status ->
            when (status) {
                FFeatureStatus.NotFound -> flowOf(FlipperSupportedState.DEPRECATED_FLIPPER)
                FFeatureStatus.Unsupported -> flowOf(FlipperSupportedState.DEPRECATED_FLIPPER)
                FFeatureStatus.Retrieving -> flowOf(FlipperSupportedState.READY)
                is FFeatureStatus.Supported -> {
                    status.featureApi
                        .getSupportedStateFlow()
                        .map { state -> state ?: FlipperSupportedState.DEPRECATED_FLIPPER }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, FlipperSupportedState.READY)
    }
}
