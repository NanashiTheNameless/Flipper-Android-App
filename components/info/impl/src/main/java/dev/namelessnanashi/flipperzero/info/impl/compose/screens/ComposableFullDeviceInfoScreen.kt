package dev.namelessnanashi.flipperzero.info.impl.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.dataOrNull
import dev.namelessnanashi.flipperzero.core.ui.ktx.OrangeAppBar
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.impl.compose.screens.fullinfo.ComposableFullInfoDevice
import dev.namelessnanashi.flipperzero.info.impl.model.DeviceStatus
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.DeviceStatusViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.deviceinfo.BasicInfoViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.deviceinfo.FullInfoViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.deviceinfo.ShareFullInfoFileViewModel
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableFullDeviceInfoScreen(
    onBack: () -> Unit,
    shareViewModel: ShareFullInfoFileViewModel,
    basicInfoViewModel: BasicInfoViewModel,
    fullInfoViewModel: FullInfoViewModel,
    deviceStatusViewModel: DeviceStatusViewModel,
    modifier: Modifier = Modifier,
) {
    val deviceStatus by deviceStatusViewModel.getState().collectAsState()
    val localDeviceStatus = deviceStatus
    val flipperRpcInformation by fullInfoViewModel.getFlipperRpcInformation().collectAsState()
    val basicInfo by basicInfoViewModel.getDeviceInfo().collectAsState()

    val inProgress = when (localDeviceStatus) {
        is DeviceStatus.NoDeviceInformation -> localDeviceStatus.connectInProgress
        is DeviceStatus.Connected ->
            flipperRpcInformation !is FlipperInformationStatus.Ready<*> ||
                basicInfo.storageInfo.externalStorageStatus !is FlipperInformationStatus.Ready<*> ||
                basicInfo.storageInfo.internalStorageStatus !is FlipperInformationStatus.Ready<*> ||
                basicInfo.firmwareVersion !is FlipperInformationStatus.Ready<*>

        else -> false
    }

    Column(modifier = modifier) {
        ComposableFullDeviceInfoScreenBar(
            onBack = onBack,
            onShare = {
                shareViewModel.shareDeviceInfo(flipperRpcInformation.dataOrNull(), basicInfo)
            },
            inProgress = inProgress
        )
        ComposableFullInfoDevice(
            fullDeviceInfo = flipperRpcInformation.dataOrNull() ?: FlipperRpcInformation(),
            inProgress = inProgress,
            getFirmwareChannel = fullInfoViewModel::getFirmwareChannel,
            onRefresh = fullInfoViewModel::refresh
        )
    }
}

@Composable
private fun ComposableFullDeviceInfoScreenBar(
    onBack: () -> Unit,
    onShare: () -> Unit,
    inProgress: Boolean
) {
    OrangeAppBar(
        titleId = R.string.info_device_info_title,
        onBack = onBack,
        endBlock = { modifier ->
            if (!inProgress) {
                Icon(
                    modifier = modifier
                        .clickableRipple(bounded = false, onClick = onShare)
                        .size(size = 24.dp),
                    painter = painterResource(DesignSystem.drawable.ic_upload),
                    contentDescription = null,
                    tint = LocalPallet.current.onAppBar
                )
            } else {
                CircularProgressIndicator(
                    color = LocalPallet.current.onAppBar,
                    modifier = modifier
                        .size(size = 24.dp),
                    strokeWidth = 2.5.dp
                )
            }
        }
    )
}
