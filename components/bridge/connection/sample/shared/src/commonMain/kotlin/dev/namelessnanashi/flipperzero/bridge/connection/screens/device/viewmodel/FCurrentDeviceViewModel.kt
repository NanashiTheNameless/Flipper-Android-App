package dev.namelessnanashi.flipperzero.bridge.connection.screens.device.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import dev.zacsweers.metro.Inject

class FCurrentDeviceViewModel @Inject constructor(
    private val orchestrator: FDeviceOrchestrator,
    persistedStorage: FDevicePersistedStorage
) : DecomposeViewModel() {

    init {
        persistedStorage.getCurrentDevice()
            .onEach { device ->
                if (device == null) {
                    orchestrator.disconnectCurrent()
                } else {
                    orchestrator.connect(device)
                }
            }.launchIn(viewModelScope)
    }

    fun getState() = orchestrator.getState()
}
