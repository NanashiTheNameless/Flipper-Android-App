package dev.namelessnanashi.flipperzero.nfc.mfkey32.screen.viewmodel

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.core.preference.pb.HardwareColor
import dev.namelessnanashi.flipperzero.core.preference.pb.PairSettings
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import dev.zacsweers.metro.Inject

class FlipperColorViewModel @Inject constructor(
    settings: DataStore<PairSettings>
) : DecomposeViewModel() {
    private val colorFlipperState = MutableStateFlow<HardwareColor>(HardwareColor.WHITE)

    init {
        settings.data.onEach {
            colorFlipperState.emit(it.hardware_color)
        }.launchIn(viewModelScope)
    }

    fun getFlipperColor(): StateFlow<HardwareColor> = colorFlipperState
}
