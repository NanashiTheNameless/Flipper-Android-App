package dev.namelessnanashi.flipperzero.info.impl.compose.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.impl.compose.dialogs.ComposableForgotDialog
import dev.namelessnanashi.flipperzero.info.impl.model.DeviceStatus
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.ConnectViewModel
import dev.namelessnanashi.flipperzero.info.shared.ButtonElementRow
import dev.namelessnanashi.flipperzero.info.shared.ComposableInfoDivider
import dev.namelessnanashi.flipperzero.info.shared.InfoElementCard
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposablePairDeviceActionCard(
    connectViewModel: ConnectViewModel,
    deviceStatus: DeviceStatus,
    modifier: Modifier = Modifier,
) {
    val rootNavigation = LocalRootNavigation.current

    InfoElementCard(modifier = modifier) {
        when (deviceStatus) {
            is DeviceStatus.Connected ->
                ComposableDisconnectElement(onDisconnect = connectViewModel::onDisconnect)

            DeviceStatus.NoDevice ->
                ComposableFirstConnectElement(
                    onGoToConnectScreen = {
                        rootNavigation.push(RootScreenConfig.FirstPair(null))
                    }
                )

            is DeviceStatus.NoDeviceInformation -> if (deviceStatus.connectInProgress) {
                ComposableDisconnectElement(onDisconnect = connectViewModel::onDisconnect)
            } else {
                ComposableConnectElement(
                    onConnectAndSynchronize = connectViewModel::connectAndSynchronize
                )
            }
        }

        if (deviceStatus is DeviceStatus.NoDevice) {
            return@InfoElementCard
        }

        ComposableInfoDivider()

        var isForgotDialogOpen by remember { mutableStateOf(false) }
        ButtonElementRow(
            titleId = R.string.info_device_forget,
            iconId = R.drawable.ic_disconnection,
            color = LocalPallet.current.forgetFlipper,
            onClick = { isForgotDialogOpen = true }
        )
        if (isForgotDialogOpen) {
            ComposableForgotDialog(
                flipperName = deviceStatus.getFlipperName(),
                onCancel = { isForgotDialogOpen = false },
                onForget = connectViewModel::forgetFlipper
            )
        }
    }
}

@Composable
private fun ComposableDisconnectElement(
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonElementRow(
        modifier = modifier,
        titleId = R.string.info_device_disconnect,
        iconId = DesignSystem.drawable.ic_bluetooth_disable,
        color = LocalPallet.current.accentSecond,
        onClick = onDisconnect
    )
}

@Composable
private fun ComposableConnectElement(
    onConnectAndSynchronize: () -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonElementRow(
        modifier = modifier,
        titleId = R.string.info_device_connect,
        iconId = DesignSystem.drawable.ic_bluetooth,
        color = LocalPallet.current.accentSecond,
        onClick = onConnectAndSynchronize
    )
}

@Composable
private fun ComposableFirstConnectElement(
    onGoToConnectScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonElementRow(
        modifier = modifier,
        titleId = R.string.info_device_connect,
        iconId = DesignSystem.drawable.ic_bluetooth,
        color = LocalPallet.current.accentSecond,
        onClick = onGoToConnectScreen
    )
}
