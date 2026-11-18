package dev.namelessnanashi.flipperzero.updater.card.viewmodel

import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.updater.api.UpdateStateApi
import dev.namelessnanashi.flipperzero.updater.api.UpdaterApi
import dev.namelessnanashi.flipperzero.updater.model.FlipperUpdateState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import dev.zacsweers.metro.Inject

class UpdateStateViewModel @Inject constructor(
    private val updaterApi: UpdaterApi,
    private val updateStateApi: UpdateStateApi
) : DecomposeViewModel() {
    private val flipperStateFlow = updateStateApi.getFlipperUpdateState(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Eagerly, FlipperUpdateState.NotConnected)

    fun getUpdateState(): StateFlow<FlipperUpdateState> = flipperStateFlow

    fun onDismissUpdateDialog() {
        updaterApi.resetState()
    }
}
