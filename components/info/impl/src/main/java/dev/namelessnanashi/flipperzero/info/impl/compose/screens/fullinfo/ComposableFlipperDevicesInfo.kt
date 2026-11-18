package dev.namelessnanashi.flipperzero.info.impl.compose.screens.fullinfo

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperDeviceInfo
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.shared.ComposableDeviceInfoRow
import dev.namelessnanashi.flipperzero.info.shared.ComposableDeviceInfoRowText
import dev.namelessnanashi.flipperzero.info.shared.ComposableDeviceInfoRowWithText
import dev.namelessnanashi.flipperzero.info.shared.ComposableInfoDivider

@Composable
fun ComposableFlipperDevicesInfo(
    info: FlipperDeviceInfo,
    inProgress: Boolean
) {
    ComposableDeviceInfoRowWithText(
        titleId = R.string.full_info_device_name,
        inProgress = inProgress,
        value = info.deviceName
    )
    ComposableInfoDivider()
    ComposableDeviceInfoRowWithText(
        titleId = R.string.full_info_hardware_model,
        inProgress = inProgress,
        value = info.hardwareModel
    )
    ComposableInfoDivider()
    ComposableDeviceInfoRowWithText(
        titleId = R.string.full_info_hardware_region,
        inProgress = inProgress,
        value = info.hardwareRegion
    )
    ComposableInfoDivider()
    ComposableCountryRow(
        titleId = R.string.full_info_hardware_region_provisioned,
        inProgress = inProgress,
        value = info.hardwareRegionProv
    )
    ComposableInfoDivider()
    ComposableDeviceInfoRowWithText(
        titleId = R.string.full_info_hardware_version,
        inProgress = inProgress,
        value = info.hardwareVersion
    )
    ComposableInfoDivider()
    ComposableDeviceInfoRowWithText(
        titleId = R.string.full_info_hardware_otp_version,
        inProgress = inProgress,
        value = info.hardwareOTPVersion
    )
    ComposableInfoDivider()
    ComposableDeviceInfoRowWithText(
        titleId = R.string.full_info_serial_number,
        inProgress = inProgress,
        value = info.serialNumber
    )
}

@Composable
private fun ComposableCountryRow(
    titleId: Int,
    value: String?,
    inProgress: Boolean
) {
    if (value == null) {
        ComposableDeviceInfoRow(titleId = titleId, inProgress = inProgress, content = null)
    } else {
        ComposableDeviceInfoRow(
            titleId,
            inProgress,
            content = { modifier ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ComposableDeviceInfoRowText(modifier = modifier, text = value)
                }
            }
        )
    }
}
