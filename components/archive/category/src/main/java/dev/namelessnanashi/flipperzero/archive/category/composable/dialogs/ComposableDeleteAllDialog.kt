package dev.namelessnanashi.flipperzero.archive.category.composable.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.namelessnanashi.flipperzero.archive.category.R
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialog
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialogModel
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.addButton
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setDescription
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setTitle

@Composable
internal fun ComposableDeleteAllDialog(
    onAction: () -> Unit,
    onCancel: () -> Unit
) {
    val dialogModel = remember(onCancel, onAction) {
        FlipperMultiChoiceDialogModel.Builder()
            .setTitle(R.string.dialog_delete_all_title)
            .setDescription(R.string.dialog_delete_all_desc)
            .setOnDismissRequest(onCancel)
            .addButton(R.string.dialog_delete_all_confirm, onAction, isActive = true)
            .addButton(R.string.dialog_cancel, onCancel)
            .build()
    }
    FlipperMultiChoiceDialog(model = dialogModel)
}
