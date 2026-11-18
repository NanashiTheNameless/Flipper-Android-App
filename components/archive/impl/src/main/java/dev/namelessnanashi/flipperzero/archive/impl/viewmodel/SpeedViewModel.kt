package dev.namelessnanashi.flipperzero.archive.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.serialspeed.api.FSpeedFeatureApi
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import dev.zacsweers.metro.Inject

class SpeedViewModel @Inject constructor(
    fFeatureProvider: FFeatureProvider,
) : DecomposeViewModel() {
    @ExperimentalCoroutinesApi
    val speedFlow = fFeatureProvider
        .get<FSpeedFeatureApi>()
        .map { status -> status as? FFeatureStatus.Supported<FSpeedFeatureApi> }
        .flatMapLatest { status -> status?.featureApi?.getSpeed() ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
