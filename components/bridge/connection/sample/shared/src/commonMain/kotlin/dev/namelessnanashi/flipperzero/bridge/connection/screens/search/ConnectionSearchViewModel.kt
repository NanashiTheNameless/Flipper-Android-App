package dev.namelessnanashi.flipperzero.bridge.connection.screens.search

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class ConnectionSearchViewModel(
    private val persistedStorage: FDevicePersistedStorage
) : DecomposeViewModel() {
    abstract fun getDevicesFlow(): StateFlow<ImmutableList<ConnectionSearchItem>>
    fun onDeviceClick(searchItem: ConnectionSearchItem) {
        viewModelScope.launch {
            if (searchItem.isAdded) {
                persistedStorage.removeDevice(searchItem.deviceModel.uniqueId)
            } else {
                persistedStorage.addDevice(searchItem.deviceModel)
            }
        }
    }
}
