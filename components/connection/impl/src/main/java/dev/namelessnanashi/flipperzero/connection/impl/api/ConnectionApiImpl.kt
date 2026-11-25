package dev.namelessnanashi.flipperzero.connection.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bottombar.model.TabState
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroUsbModel
import dev.namelessnanashi.flipperzero.connection.api.ConnectionApi
import dev.namelessnanashi.flipperzero.connection.impl.dialog.ComposableUnsupportedDialog
import dev.namelessnanashi.flipperzero.connection.impl.viewmodel.ConnectionStatusViewModel
import dev.namelessnanashi.flipperzero.connection.impl.viewmodel.ConnectionTabStateMapper
import dev.namelessnanashi.flipperzero.connection.impl.viewmodel.UnsupportedStateViewModel
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@ContributesBinding(AppGraph::class)
class ConnectionApiImpl @Inject constructor(
    private val connectionStatusVMProvider: Provider<ConnectionStatusViewModel>,
    private val unsupportedStateVMProvider: Provider<UnsupportedStateViewModel>,
    private val devicePersistedStorage: FDevicePersistedStorage
) : ConnectionApi {
    @Composable
    override fun getConnectionTabState(
        componentContext: ComponentContext
    ): TabState {
        val connectionStatusViewModel = componentContext.viewModelWithFactory(null) {
            connectionStatusVMProvider.invoke()
        }
        val connectionStatusState by connectionStatusViewModel.getStatusState().collectAsState()
        val currentDevice by remember { devicePersistedStorage.getCurrentDevice() }.collectAsState(null)
        return ConnectionTabStateMapper.getConnectionTabState(
            connectionState = connectionStatusState,
            isUsb = currentDevice is FDeviceFlipperZeroUsbModel
        )
    }

    @Composable
    override fun CheckAndShowUnsupportedDialog(
        componentContext: ComponentContext
    ) {
        val viewModel = componentContext.viewModelWithFactory(null) {
            unsupportedStateVMProvider.invoke()
        }
        val supportedState by viewModel.getUnsupportedState().collectAsState()
        ComposableUnsupportedDialog(supportedState)
    }
}
