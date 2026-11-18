package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.api.dialog.composable

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.busy.ComposableFlipperBusy
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi.DialogType
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig

@Composable
fun SetupFlipperDialogComposable(
    flipperDialog: DialogType?,
    onDismiss: () -> Unit,
    openDeviceTab: () -> Unit
) {
    val rootNavigation = LocalRootNavigation.current
    when (flipperDialog) {
        DialogType.FLIPPER_IS_BUSY -> {
            ComposableFlipperBusy(
                onDismiss = onDismiss,
                goToRemote = {
                    onDismiss.invoke()
                    rootNavigation.push(RootScreenConfig.ScreenStreaming)
                }
            )
        }

        DialogType.FLIPPER_NOT_CONNECTED -> {
            ComposableFlipperNotConnectedDialog(
                onDismiss = onDismiss,
                onOpenDeviceTab = openDeviceTab
            )
        }

        DialogType.FLIPPER_NOT_SUPPORTED -> {
            ComposableFlipperNotSupportedDialog(
                onDismiss = onDismiss,
                onOpenDeviceTab = openDeviceTab
            )
        }

        null -> Unit
    }
}
