package dev.namelessnanashi.flipperzero.bottombar.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.installedtab.api.FapUpdatePendingCountApi
import dev.namelessnanashi.flipperzero.toolstab.api.ToolsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import dev.zacsweers.metro.Inject

class BottomBarViewModel @Inject constructor(
    toolsApi: ToolsApi,
    fapUpdatePendingCountApi: FapUpdatePendingCountApi
) : DecomposeViewModel() {
    private val hasNotificationToolsStateFlow = MutableStateFlow(false)
    private val hasNotificationAppsStateFlow = MutableStateFlow(false)

    init {
        toolsApi.hasNotification(viewModelScope).onEach {
            hasNotificationToolsStateFlow.emit(it)
        }.launchIn(viewModelScope)
        fapUpdatePendingCountApi.getUpdatePendingCount().onEach {
            hasNotificationAppsStateFlow.emit(it > 0)
        }.launchIn(viewModelScope)
    }

    fun hasNotificationHubState() = hasNotificationToolsStateFlow.asStateFlow()

    fun hasNotificationAppsState() = hasNotificationAppsStateFlow.asStateFlow()
}
