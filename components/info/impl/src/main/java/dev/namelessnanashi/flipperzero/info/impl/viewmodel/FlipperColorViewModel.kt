package dev.namelessnanashi.flipperzero.info.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroBleModel
import dev.namelessnanashi.flipperzero.bridge.connection.feature.devicecolor.api.FDeviceColorFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.core.preference.pb.FlipperZeroBle.HardwareColor
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

private val UNKNOWN_COLOR = HardwareColor.fromValue(-1)

@OptIn(ExperimentalCoroutinesApi::class)
class FlipperColorViewModel @Inject constructor(
    fDevicePersistedStorage: FDevicePersistedStorage,
    fFeatureProvider: FFeatureProvider
) : DecomposeViewModel() {
    private val colorFlipperState = fDevicePersistedStorage.getCurrentDevice()
        .flatMapLatest { device ->
            if (device is FDeviceFlipperZeroBleModel) {
                flowOf(device.hardwareColor)
            } else {
                // Fork: a USB device has no stored colour; ask the connected Flipper
                fFeatureProvider.get<FDeviceColorFeatureApi>().flatMapLatest { status ->
                    (status as? FFeatureStatus.Supported<FDeviceColorFeatureApi>)
                        ?.featureApi
                        ?.updateAndGetColorFlow(HardwareColor.WHITE)
                        ?: emptyFlow()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UNKNOWN_COLOR)

    fun getFlipperColor(): StateFlow<HardwareColor> = colorFlipperState
}
