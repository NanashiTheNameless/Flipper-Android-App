package dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.composable.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialog
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialogModel
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.addButton
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setDescription
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setTitle
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.ComposableAppDialogBox
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItem
import dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.R

@Composable
fun ComposableFapHideConfirmDialog(
    fapItem: FapItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textError = LocalPallet.current.onError
    val multiChoiceDialogModel = remember(fapItem, onConfirm, onDismiss, textError) {
        FlipperMultiChoiceDialogModel.Builder()
            .setImage {
                ComposableAppDialogBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 24.dp),
                    fapItem = fapItem
                )
            }
            .setTitle(R.string.fapscreen_developer_dialog_title)
            .setDescription(R.string.fapscreen_developer_dialog_desc)
            .setOnDismissRequest(onDismiss)
            .addButton(
                textId = R.string.fapscreen_developer_dialog_btn_confirm,
                onClick = onConfirm,
                textColor = textError
            )
            .addButton(R.string.fapscreen_developer_dialog_btn_cancel, onDismiss)
            .build()
    }

    FlipperMultiChoiceDialog(
        model = multiChoiceDialogModel,
        modifier = modifier
    )
}
