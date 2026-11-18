package dev.namelessnanashi.flipperzero.bridge.connection.screens.device

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigator
import com.arkivanov.decompose.router.stack.pushNew
import dev.namelessnanashi.flipperzero.bridge.connection.screens.device.composable.FCurrentDeviceComposable
import dev.namelessnanashi.flipperzero.bridge.connection.screens.device.composable.FDeviceDropdownComposable
import dev.namelessnanashi.flipperzero.bridge.connection.screens.device.composable.FPingComposable
import dev.namelessnanashi.flipperzero.bridge.connection.screens.device.viewmodel.FCurrentDeviceViewModel
import dev.namelessnanashi.flipperzero.bridge.connection.screens.device.viewmodel.FDevicesViewModel
import dev.namelessnanashi.flipperzero.bridge.connection.screens.device.viewmodel.PingViewModel
import dev.namelessnanashi.flipperzero.bridge.connection.screens.models.ConnectionRootConfig
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactoryWithoutRemember
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider

class ConnectionDeviceScreenDecomposeComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val navigation: StackNavigator<ConnectionRootConfig>,
    private val devicesViewModelProvider: Provider<FDevicesViewModel>,
    private val currentDeviceViewModelProvider: Provider<FCurrentDeviceViewModel>,
    private val pingViewModelProvider: Provider<PingViewModel>
) : ScreenDecomposeComponent(componentContext) {
    private val devicesViewModel = viewModelWithFactoryWithoutRemember(null) {
        devicesViewModelProvider.invoke()
    }
    private val currentDeviceViewModel = viewModelWithFactoryWithoutRemember(null) {
        currentDeviceViewModelProvider.invoke()
    }
    private val pingViewModel = viewModelWithFactoryWithoutRemember(null) {
        pingViewModelProvider.invoke()
    }

    @Composable
    override fun Render() {
        Column {
            val deviceState by devicesViewModel.getState().collectAsState()
            FDeviceDropdownComposable(
                devicesState = deviceState,
                onDeviceSelect = devicesViewModel::onSelectDevice,
                onOpenSearch = { navigation.pushNew(ConnectionRootConfig.Search) },
                onDisconnect = devicesViewModel::onDisconnect
            )
            val currentDevice by currentDeviceViewModel.getState().collectAsState()
            FCurrentDeviceComposable(currentDevice)
            val logs by pingViewModel.getLogLinesState().collectAsState()
            FPingComposable(
                logs = logs,
                onSendPing = pingViewModel::sendPing,
                invalidateRpcInfo = pingViewModel::invalidateRpcInfo,
                onOpenFM = {
                    navigation.pushNew(ConnectionRootConfig.FileManager)
                }
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            @Assisted componentContext: ComponentContext,
            @Assisted navigation: StackNavigator<ConnectionRootConfig>
        ): ConnectionDeviceScreenDecomposeComponent
    }
}
