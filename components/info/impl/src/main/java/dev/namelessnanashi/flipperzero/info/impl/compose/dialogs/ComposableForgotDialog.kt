package dev.namelessnanashi.flipperzero.info.impl.compose.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialog
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialogModel
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.addButton
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.setTitle
import dev.namelessnanashi.flipperzero.info.impl.R

@Composable
internal fun ComposableForgotDialog(
    flipperName: String,
    onCancel: () -> Unit,
    onForget: () -> Unit
) {
    val description = stringResource(R.string.info_device_forget_dialog_description, flipperName)
    val dialogModel = remember(onCancel, onForget) {
        FlipperMultiChoiceDialogModel.Builder()
            .setTitle(R.string.info_device_forget_dialog_title)
            .setDescription(AnnotatedString(description))
            .setOnDismissRequest(onCancel)
            .addButton(R.string.info_device_forget_dialog_forget, onForget, isActive = true)
            .addButton(R.string.info_device_forget_dialog_cancel, onCancel)
            .build()
    }
    FlipperMultiChoiceDialog(model = dialogModel)
}
