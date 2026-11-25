package dev.namelessnanashi.flipperzero.infrared.impl.composable.components

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.FlipperDialogAndroid
import dev.namelessnanashi.flipperzero.infrared.impl.R

@Composable
internal fun ComposableInfraredDialogHowToUse(
    isShowDialog: Boolean,
    onClose: () -> Unit,
) {
    if (!isShowDialog) {
        return
    }

    val imageId = R.drawable.pic_how_to_use_dark

    FlipperDialogAndroid(
        buttonTextId = R.string.infrared_dialog_how_to_use_btn,
        onClickButton = onClose,
        onDismissRequest = onClose,
        imageId = imageId,
        titleId = R.string.infrared_dialog_how_to_use_title,
        textId = R.string.infrared_dialog_how_to_use_text,
    )
}
