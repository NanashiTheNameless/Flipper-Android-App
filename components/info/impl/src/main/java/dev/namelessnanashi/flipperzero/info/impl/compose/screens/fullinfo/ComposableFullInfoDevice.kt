package dev.namelessnanashi.flipperzero.info.impl.compose.screens.fullinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import dev.namelessnanashi.flipperzero.core.ktx.jre.titlecaseFirstCharIfItIsLowercase
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.SwipeRefresh
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.shared.ComposableDeviceInfoRowWithText
import dev.namelessnanashi.flipperzero.info.shared.ComposableInfoDivider
import dev.namelessnanashi.flipperzero.info.shared.InfoElementCard
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import kotlinx.collections.immutable.ImmutableSet

@Composable
fun ComposableFullInfoDevice(
    fullDeviceInfo: FlipperRpcInformation,
    inProgress: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    getFirmwareChannel: (String?) -> FirmwareChannel?,
) = SwipeRefresh(modifier = modifier, onRefresh = onRefresh) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InfoElementCard(
            Modifier,
            isSelectionArea = true,
            titleId = R.string.full_info_flipper_device
        ) {
            ComposableFlipperDevicesInfo(fullDeviceInfo.flipperDeviceInfo, inProgress)
        }
        InfoElementCard(
            Modifier,
            isSelectionArea = true,
            titleId = R.string.full_info_firmware
        ) {
            ComposableFirmwareInfo(fullDeviceInfo.firmware, inProgress) { getFirmwareChannel(it) }
        }
        InfoElementCard(
            Modifier,
            isSelectionArea = true,
            titleId = R.string.full_info_radio_stack
        ) {
            ComposableRadioStackInfo(fullDeviceInfo.radioStack, inProgress)
        }
        InfoElementCard(
            Modifier,
            isSelectionArea = true,
            titleId = R.string.full_info_other
        ) {
            ComposableOtherInfo(fullDeviceInfo.otherFields.entries, inProgress)
        }
    }
}

@Composable
private fun ComposableOtherInfo(
    fields: ImmutableSet<Map.Entry<String, String>>,
    inProgress: Boolean
) {
    fields.forEachIndexed { index, field ->
        val name = field.key.split(' ', '_', '.')
            .map { it.titlecaseFirstCharIfItIsLowercase() }
            .joinToString(" ")
        val value = field.value

        ComposableDeviceInfoRowWithText(text = name, inProgress = inProgress, value = value)
        if (index != fields.size - 1) {
            ComposableInfoDivider()
        }
    }
}
