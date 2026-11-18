package dev.namelessnanashi.flipperzero.toolstab.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.toolstab.api.ToolsApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import dev.zacsweers.metro.Inject

class ToolsNotificationViewModel @Inject constructor(
    toolsApi: ToolsApi
) : DecomposeViewModel() {
    val hasNotificationStateFlow: StateFlow<Boolean> = toolsApi
        .hasNotification(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
