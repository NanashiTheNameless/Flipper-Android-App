package dev.namelessnanashi.flipperzero.info.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroBleModel
import dev.namelessnanashi.flipperzero.core.preference.pb.FlipperZeroBle.HardwareColor
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import dev.zacsweers.metro.Inject

class FlipperColorViewModel @Inject constructor(
    fDevicePersistedStorage: FDevicePersistedStorage,
) : DecomposeViewModel() {
    private val colorFlipperState = fDevicePersistedStorage.getCurrentDevice()
        .filterIsInstance<FDeviceFlipperZeroBleModel>()
        .map { coloredDevice -> coloredDevice.hardwareColor }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HardwareColor.fromValue(-1))

    fun getFlipperColor(): StateFlow<HardwareColor> = colorFlipperState
}
