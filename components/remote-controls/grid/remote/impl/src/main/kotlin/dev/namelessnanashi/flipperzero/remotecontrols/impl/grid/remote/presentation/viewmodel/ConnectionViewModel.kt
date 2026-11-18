package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.viewmodel

import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.infrared.api.InfraredConnectionApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import dev.zacsweers.metro.Inject

class ConnectionViewModel @Inject constructor(
    infraredConnectionApi: InfraredConnectionApi,
) : DecomposeViewModel() {
    val state = infraredConnectionApi.getState().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        InfraredConnectionApi.InfraredEmulateState.ALL_GOOD
    )
}
