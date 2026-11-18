package dev.namelessnanashi.flipperzero.info.impl.compose.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.FlipperStorageInformation
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.StorageStats
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.dataOrNull
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.externalStorageRequestInProgress
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.flashSdStats
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.isExtStorageEnding
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.toString
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.impl.compose.info.ComposableFirmwareBuildDate
import dev.namelessnanashi.flipperzero.info.impl.compose.info.ComposableFirmwareVersion
import dev.namelessnanashi.flipperzero.info.impl.model.DeviceStatus
import dev.namelessnanashi.flipperzero.info.impl.model.FlipperBasicInfo
import dev.namelessnanashi.flipperzero.info.shared.ComposableDeviceInfoRowWithText
import dev.namelessnanashi.flipperzero.info.shared.ComposableInfoDivider
import dev.namelessnanashi.flipperzero.info.shared.InfoElementCard
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion

@Composable
fun ComposableInfoCardContent(
    isUnsupported: Boolean,
    deviceStatus: DeviceStatus,
    deviceInfo: FlipperBasicInfo
) {
    ComposableInfoCardContentInternal(
        isUnsupported = isUnsupported,
        flipperBasicInfo = deviceInfo,
        deviceStatus = deviceStatus,
    )
}

@Composable
private fun ComposableInfoCardContentInternal(
    isUnsupported: Boolean,
    flipperBasicInfo: FlipperBasicInfo,
    deviceStatus: DeviceStatus
) {
    val firmwareVersionInProgress = if (deviceStatus is DeviceStatus.NoDeviceInformation) {
        deviceStatus.connectInProgress
    } else {
        deviceStatus is DeviceStatus.Connected &&
            flipperBasicInfo.firmwareVersion !is FlipperInformationStatus.Ready
    }

    ComposableFirmwareVersion(
        flipperBasicInfo.firmwareVersion.dataOrNull(),
        firmwareVersionInProgress
    )
    ComposableInfoDivider()
    ComposableFirmwareBuildDate(
        flipperBasicInfo.firmwareVersion.dataOrNull(),
        firmwareVersionInProgress
    )
    if (isUnsupported) {
        return
    }
    ComposableInfoDivider()
    val storageInfo = flipperBasicInfo.storageInfo
    ComposableDeviceInfoRowWithText(
        R.string.info_device_info_ext_flash,
        firmwareVersionInProgress || storageInfo.externalStorageRequestInProgress,
        storageInfo.flashSdStats?.toString(LocalContext.current),
        color = if (storageInfo.isExtStorageEnding()) LocalPallet.current.warningColor else null
    )
}

@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ComposableInfoCardContentInternalPreview() {
    val flipperBasicInfo = FlipperBasicInfo(
        firmwareVersion = FlipperInformationStatus.Ready(
            FirmwareVersion(
                channel = FirmwareChannel.UNKNOWN,
                version = "fsfsfmfmmflmsmflslmfmlsfmlsmlfsmfmlsmlfslmfmlslfmlsmflsmlflslfmlsflmslmfslf"
            )
        ),
        storageInfo = FlipperStorageInformation(
            internalStorageStatus = FlipperInformationStatus.Ready(
                StorageStats.Loaded(
                    total = 1000,
                    free = 300
                )
            ),
            externalStorageStatus = FlipperInformationStatus.Ready(StorageStats.Error)
        )
    )
    val deviceStatus = DeviceStatus.Connected(
        deviceName = "Flipper",
        batteryLevel = 0.4f,
        isCharging = true
    )
    FlipperThemeInternal {
        InfoElementCard(Modifier, R.string.info_device_info_title) {
            ComposableInfoCardContentInternal(
                isUnsupported = false,
                flipperBasicInfo = flipperBasicInfo,
                deviceStatus = deviceStatus
            )
        }
    }
}
