package dev.namelessnanashi.flipperzero.info.impl.compose.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.model.FlipperSupportedState
import dev.namelessnanashi.flipperzero.core.preference.pb.HardwareColor
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.SwipeRefresh
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.info.impl.compose.bar.ComposableDeviceBar
import dev.namelessnanashi.flipperzero.info.impl.compose.elements.ComposableConnectedDeviceActionCard
import dev.namelessnanashi.flipperzero.info.impl.compose.elements.ComposableFirmwareUpdate
import dev.namelessnanashi.flipperzero.info.impl.compose.elements.ComposableOptionsCard
import dev.namelessnanashi.flipperzero.info.impl.compose.elements.ComposablePairDeviceActionCard
import dev.namelessnanashi.flipperzero.info.impl.compose.elements.ComposableScreenStreamingCard
import dev.namelessnanashi.flipperzero.info.impl.compose.info.ComposableInfoCard
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.info.impl.model.DeviceStatus
import dev.namelessnanashi.flipperzero.info.impl.model.FlipperBasicInfo
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.ConnectViewModel
import dev.namelessnanashi.flipperzero.updater.api.UpdaterCardApi
import dev.namelessnanashi.flipperzero.updater.model.FlipperUpdateState
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest

@Composable
@Suppress("NonSkippableComposable", "LongMethod")
fun ComposableDeviceInfoScreen(
    updaterCardApi: UpdaterCardApi,
    synchronizationUiApi: SynchronizationUiApi,
    componentContext: ComponentContext,
    deeplink: Deeplink.BottomBar.DeviceTab.WebUpdate?,
    deviceStatus: DeviceStatus,
    scrollState: ScrollState,
    connectViewModel: ConnectViewModel,
    hardwareColor: HardwareColor,
    supportedState: FlipperSupportedState,
    updateState: FlipperUpdateState,
    deviceInfo: FlipperBasicInfo,
    onOpenFullDeviceInfo: () -> Unit,
    onOpenOptions: () -> Unit,
    hasAlarm: Boolean,
    alarmOnFlipper: () -> Unit,
    onOpenRemoteControl: () -> Unit,
    onStartUpdateRequest: (UpdateRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var refreshRequested: Boolean by remember { mutableStateOf(false) }

    SwipeRefresh(
        modifier = modifier
            .fillMaxSize()
            .background(LocalPallet.current.appBar)
            .statusBarsPadding()
            .background(LocalPallet.current.background),
        onRefresh = { refreshRequested = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ComposableDeviceBar(deviceStatus, hardwareColor)
            synchronizationUiApi.RenderSynchronizationStatus()
            updaterCardApi.ComposableUpdaterCard(
                modifier = Modifier.padding(top = 14.dp),
                onStartUpdateRequest = onStartUpdateRequest,
                requestRefresh = refreshRequested,
                onRefreshRequestExecute = { refreshRequested = false },
                deeplink = deeplink,
                componentContext = componentContext
            )
            ComposableFirmwareUpdate(
                modifier = Modifier.padding(top = 14.dp),
                supportedState = supportedState
            )
            ComposableInfoCard(
                modifier = Modifier.padding(top = 14.dp),
                onOpenFullDeviceInfo = onOpenFullDeviceInfo,
                deviceStatus = deviceStatus,
                updateState = updateState,
                firmwareUpdateState = supportedState,
                deviceInfo = deviceInfo
            )
            if (deviceStatus is DeviceStatus.Connected) {
                ComposableScreenStreamingCard(
                    modifier = Modifier
                        .padding(top = 14.dp),
                    onOpen = onOpenRemoteControl
                )
            }
            ComposableOptionsCard(
                modifier = Modifier
                    .padding(top = 14.dp),
                onOpenOptions = onOpenOptions
            )
            ComposableConnectedDeviceActionCard(
                modifier = Modifier.padding(top = 14.dp),
                deviceStatus = deviceStatus,
                supportedState = supportedState,
                requestSynchronize = {
                    synchronizationUiApi.showSynchronizationDialog()
                    connectViewModel.requestSynchronize()
                },
                alarmOnFlipper = alarmOnFlipper,
                hasAlarm = hasAlarm
            )
            ComposablePairDeviceActionCard(
                modifier = Modifier.padding(top = 14.dp, bottom = 14.dp),
                deviceStatus = deviceStatus,
                connectViewModel = connectViewModel
            )
        }
    }
}
