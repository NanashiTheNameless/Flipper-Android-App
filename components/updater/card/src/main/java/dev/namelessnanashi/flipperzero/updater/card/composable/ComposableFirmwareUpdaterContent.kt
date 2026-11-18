package dev.namelessnanashi.flipperzero.updater.card.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.info.shared.ComposableDeviceInfoRow
import dev.namelessnanashi.flipperzero.info.shared.ComposableInfoDivider
import dev.namelessnanashi.flipperzero.updater.card.R
import dev.namelessnanashi.flipperzero.updater.card.viewmodel.UpdateRequestViewModel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.updater.model.UpdateCardState
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest

@Composable
fun ComposableFirmwareUpdaterContent(
    updateRequestViewModel: UpdateRequestViewModel,
    version: FirmwareVersion?,
    updateCardState: UpdateCardState,
    onSelectFirmwareChannel: (FirmwareChannel) -> Unit,
    modifier: Modifier = Modifier,
    onStartUpdateRequest: (UpdateRequest) -> Unit
) {
    val inProgress = version == null
    ComposableDeviceInfoRow(
        titleId = R.string.updater_card_updater_channel,
        inProgress = false,
        content = {
            ComposableUpdaterFirmwareVersionWithChoice(
                modifier = it,
                onSelectFirmwareChannel = onSelectFirmwareChannel,
                version = version
            )
        },
        modifier = modifier
    )
    ComposableInfoDivider()
    ComposableUpdateButton(
        updateCardState = updateCardState,
        inProgress = inProgress,
        onStartUpdateRequest = onStartUpdateRequest,
        updateRequestViewModel = updateRequestViewModel
    )
}
