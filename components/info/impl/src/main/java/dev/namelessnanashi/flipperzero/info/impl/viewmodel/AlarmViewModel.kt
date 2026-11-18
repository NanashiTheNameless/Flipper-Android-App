package dev.namelessnanashi.flipperzero.info.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.feature.alarm.api.FAlarmFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dev.zacsweers.metro.Inject

class AlarmViewModel @Inject constructor(
    private val featureProvider: FFeatureProvider
) : DecomposeViewModel() {
    val hasAlarm = featureProvider.get<FAlarmFeatureApi>()
        .map { status -> status is FFeatureStatus.Supported<FAlarmFeatureApi> }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun alarmOnFlipper() {
        viewModelScope.launch {
            featureProvider.getSync<FAlarmFeatureApi>()?.makeSound()
        }
    }
}
