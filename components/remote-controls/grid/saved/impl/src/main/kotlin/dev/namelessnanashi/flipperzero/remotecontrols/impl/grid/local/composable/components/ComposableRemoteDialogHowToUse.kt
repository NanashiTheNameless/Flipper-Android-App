package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.composable.components

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.FlipperDialogAndroid
import dev.namelessnanashi.flipperzero.remotecontrols.grid.saved.impl.R

@Composable
internal fun ComposableRemoteDialogHowToUse(
    isShowDialog: Boolean,
    onClose: () -> Unit,
) {
    if (!isShowDialog) {
        return
    }

    val imageId = R.drawable.ir_flipper_format_dark

    FlipperDialogAndroid(
        buttonTextId = R.string.rc_dialog_how_to_use_btn,
        onClickButton = onClose,
        onDismissRequest = onClose,
        imageId = imageId,
        titleId = R.string.rc_dialog_how_to_use_title,
        textId = R.string.rc_dialog_how_to_use_text,
    )
}
