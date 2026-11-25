package dev.namelessnanashi.flipperzero.updater.card.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialog
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialogModel
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.addButton
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setDescription
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setTitle
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.placeholderConnecting
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.updater.card.R
import dev.namelessnanashi.flipperzero.updater.card.composable.dialogs.ComposableUpdateRequest
import dev.namelessnanashi.flipperzero.updater.card.model.UpdatePending
import dev.namelessnanashi.flipperzero.updater.card.viewmodel.UpdateRequestViewModel
import dev.namelessnanashi.flipperzero.updater.model.UpdateCardState
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComposableUpdateButton(
    updateRequestViewModel: UpdateRequestViewModel,
    updateCardState: UpdateCardState,
    inProgress: Boolean,
    modifier: Modifier = Modifier,
    onStartUpdateRequest: (UpdateRequest) -> Unit
) {
    var buttonModifier = modifier.padding(all = 12.dp)
    if (inProgress) {
        ComposableUpdateButtonPlaceholder(buttonModifier)
        return
    }
    var pendingUpdateRequest by remember { mutableStateOf<UpdatePending?>(null) }
    val localUpdaterRequest = pendingUpdateRequest
    if (localUpdaterRequest != null) {
        ComposableUpdateRequest(
            pendingUpdateRequest = localUpdaterRequest,
            onStartUpdateRequest = onStartUpdateRequest,
            updateRequestViewModel = updateRequestViewModel
        ) {
            pendingUpdateRequest = null
        }
    }

    when (updateCardState) {
        is UpdateCardState.Error -> return
        UpdateCardState.InProgress -> return
        is UpdateCardState.NoUpdate -> {
            // Fork: long press offers reinstalling the current build ("Update anyway")
            val reinstall = updateCardState.reinstall
            var showUpdateAnyway by remember { mutableStateOf(false) }
            if (reinstall != null) {
                val haptic = LocalHapticFeedback.current
                buttonModifier = buttonModifier
                    .clip(RoundedCornerShape(9.dp))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showUpdateAnyway = true
                        }
                    )
            }
            if (showUpdateAnyway && reinstall != null) {
                ComposableUpdateAnywayDialog(
                    onConfirm = {
                        showUpdateAnyway = false
                        pendingUpdateRequest = UpdatePending.Request(reinstall)
                    },
                    onDismiss = { showUpdateAnyway = false }
                )
            }
            ComposableUpdateButtonContent(
                buttonModifier = buttonModifier,
                textId = R.string.updater_card_updater_button_no_updates,
                descriptionId = if (reinstall != null) {
                    R.string.updater_card_updater_button_no_updates_hold_desc
                } else {
                    R.string.updater_card_updater_button_no_updates_desc
                },
                color = LocalPallet.current.text30
            )
        }

        is UpdateCardState.UpdateFromFile -> ComposableUpdateButtonContentChooseFile(
            modifier = buttonModifier,
            updateCardState = updateCardState,
            onChoose = { pendingUpdateRequest = it }
        )

        is UpdateCardState.UpdateAvailable -> {
            buttonModifier = buttonModifier.clickableRipple {
                pendingUpdateRequest = UpdatePending.Request(updateCardState.update)
            }

            if (updateCardState.isOtherChannel) {
                ComposableUpdateButtonContent(
                    buttonModifier = buttonModifier,
                    textId = R.string.updater_card_updater_button_install,
                    descriptionId = R.string.updater_card_updater_button_install_desc,
                    color = LocalPallet.current.text100
                )
            } else {
                ComposableUpdateButtonContent(
                    buttonModifier = buttonModifier,
                    textId = R.string.updater_card_updater_button_update,
                    descriptionId = R.string.updater_card_updater_button_update_desc,
                    color = LocalPallet.current.text100
                )
            }
        }
    }
}

@Composable
private fun ComposableUpdateAnywayDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val model = remember(onConfirm, onDismiss) {
        FlipperMultiChoiceDialogModel.Builder()
            .setTitle(R.string.updater_card_update_anyway_title)
            .setDescription(R.string.updater_card_update_anyway_desc)
            .setOnDismissRequest(onDismiss)
            .addButton(R.string.updater_card_update_anyway_button, onConfirm, isActive = true)
            .addButton(R.string.updater_card_update_anyway_cancel, onDismiss)
            .build()
    }
    FlipperMultiChoiceDialog(model = model)
}

@Composable
private fun ComposableUpdateButtonPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .fillMaxWidth()
            .placeholderConnecting(shape = 9)
    )
}

@Composable
fun ComposableUpdateButtonContent(
    @StringRes textId: Int,
    @StringRes descriptionId: Int,
    color: Color,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            // Fork: outlined like the app's other buttons (was a filled block)
            modifier = buttonModifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(LocalPallet.current.background)
                .border(2.dp, color, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(vertical = 6.dp),
                text = stringResource(textId),
                textAlign = TextAlign.Center,
                style = LocalTypography.current.updateButton40,
                color = color
            )
        }
        Text(
            modifier = Modifier.padding(
                top = 3.dp,
                bottom = 8.dp,
                start = 12.dp,
                end = 12.dp
            ),
            text = stringResource(descriptionId),
            textAlign = TextAlign.Center,
            style = LocalTypography.current.subtitleR12,
            color = LocalPallet.current.text16
        )
    }
}
