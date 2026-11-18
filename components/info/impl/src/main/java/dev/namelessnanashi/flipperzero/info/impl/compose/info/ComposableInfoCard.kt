package dev.namelessnanashi.flipperzero.info.impl.compose.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.model.FlipperSupportedState
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.animatedDots
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.impl.compose.elements.ComposableInfoCardContent
import dev.namelessnanashi.flipperzero.info.impl.model.DeviceStatus
import dev.namelessnanashi.flipperzero.info.impl.model.FlipperBasicInfo
import dev.namelessnanashi.flipperzero.info.shared.InfoElementCard
import dev.namelessnanashi.flipperzero.updater.model.FlipperUpdateState
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableInfoCard(
    deviceStatus: DeviceStatus,
    updateState: FlipperUpdateState,
    firmwareUpdateState: FlipperSupportedState,
    deviceInfo: FlipperBasicInfo,
    onOpenFullDeviceInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUnsupported = remember(firmwareUpdateState) {
        firmwareUpdateState != FlipperSupportedState.READY
    }

    InfoElementCard(
        modifier.clickableRipple(onClick = onOpenFullDeviceInfo),
        isSelectionArea = true,
        titleId = R.string.info_device_info_title,
        endContent = {
            if (deviceStatus is DeviceStatus.Connected && !isUnsupported) {
                Icon(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(14.dp),
                    tint = LocalPallet.current.iconTint30,
                    painter = painterResource(DesignSystem.drawable.ic_navigate),
                    contentDescription = stringResource(R.string.info_device_info_more_information)
                )
            }
        }
    ) {
        if (updateState is FlipperUpdateState.Updating) {
            ComposableWaitingFlipper()
            return@InfoElementCard
        }
        ComposableInfoCardContent(
            isUnsupported = isUnsupported,
            deviceStatus = deviceStatus,
            deviceInfo = deviceInfo
        )
    }
}

@Composable
fun ComposableWaitingFlipper(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 68.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = LocalPallet.current.accentSecond,
            strokeWidth = 3.dp
        )
        Text(
            text = stringResource(id = R.string.info_firmware_waiting) + animatedDots(),
            color = LocalPallet.current.text30,
            style = LocalTypography.current.bodyR14,
            textAlign = TextAlign.Center
        )
    }
}
