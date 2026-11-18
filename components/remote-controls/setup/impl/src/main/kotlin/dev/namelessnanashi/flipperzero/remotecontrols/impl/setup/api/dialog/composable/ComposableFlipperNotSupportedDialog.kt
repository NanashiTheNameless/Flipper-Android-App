package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.api.dialog.composable

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.FlipperDialogAndroid
import dev.namelessnanashi.flipperzero.remotecontrols.setup.impl.R
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableFlipperNotSupportedDialog(
    onDismiss: () -> Unit,
    onOpenDeviceTab: () -> Unit
) {
    FlipperDialogAndroid(
        titleId = R.string.remotecontrols_dialog_not_supported_flipper_title,
        textId = R.string.remotecontrols_dialog_not_supported_flipper_desc,
        buttonTextId = R.string.remotecontrols_dialog_not_supported_flipper_btn,
        onDismissRequest = onDismiss,
        onClickButton = {
            onOpenDeviceTab()
            onDismiss()
        },
        imageId = DesignSystem.drawable.ic_firmware_flipper_deprecated
    )
}
