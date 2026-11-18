package dev.namelessnanashi.flipperzero.updater.screen.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialog
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialogModel
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.addButton
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setDescription
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setTitle
import dev.namelessnanashi.flipperzero.updater.screen.R

@Composable
internal fun ComposableCancelDialog(
    onAbort: () -> Unit,
    onContinue: () -> Unit
) {
    val dialogModel = remember(onContinue, onAbort) {
        FlipperMultiChoiceDialogModel.Builder()
            .setTitle(R.string.update_cancel_dialog_title)
            .setDescription(R.string.update_cancel_dialog_desc)
            .setOnDismissRequest(onContinue)
            .addButton(R.string.update_cancel_dialog_yes, onAbort, isActive = true)
            .addButton(R.string.update_cancel_dialog_no, onContinue)
            .build()
    }
    FlipperMultiChoiceDialog(model = dialogModel)
}
