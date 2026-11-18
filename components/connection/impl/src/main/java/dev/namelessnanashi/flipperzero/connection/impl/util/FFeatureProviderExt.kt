package dev.namelessnanashi.flipperzero.connection.impl.util

import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api.FVersionFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.model.FlipperSupportedState
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal fun FFeatureProvider.getSupportedState(): Flow<FlipperSupportedState> {
    return get<FVersionFeatureApi>().flatMapLatest { status ->
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
    }
}
