package dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkBottomBarTab
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.R
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.dialogs.ComposableFlipperNotConnectedDialog
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.dialogs.ComposableFlipperNotSdCardDialog
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.elements.ComposableFlipperButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.elements.ComposableInProgressFapButton
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalDeeplinkHandler

@Composable
fun ComposableFapInstallButton(
    fapButtonSize: FapButtonSize,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComposableFlipperButton(
        modifier = modifier,
        text = stringResource(R.string.faphub_installation_install),
        color = LocalPallet.current.accent,
        fapButtonSize = fapButtonSize,
        onClick = onClick
    )
}

@Composable
fun ComposableFlipperNoSdCardButton(
    fapButtonSize: FapButtonSize,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    ComposableFapInstallButton(
        modifier = modifier,
        fapButtonSize = fapButtonSize,
        onClick = {
            showDialog = true
        }
    )
    if (showDialog) {
        ComposableFlipperNotSdCardDialog(
            onDismiss = {
                showDialog = false
            },
        )
    }
}

@Composable
fun ComposableFlipperNotConnectedButton(
    fapButtonSize: FapButtonSize,
    modifier: Modifier = Modifier
) {
    val deeplinkHandler = LocalDeeplinkHandler.current

    var showDialog by remember { mutableStateOf(false) }
    ComposableFapInstallButton(
        modifier = modifier,
        fapButtonSize = fapButtonSize,
        onClick = {
            showDialog = true
        }
    )
    if (showDialog) {
        ComposableFlipperNotConnectedDialog(
            onDismiss = {
                showDialog = false
            },
            onOpenDeviceTab = {
                deeplinkHandler.handleDeeplink(
                    Deeplink.BottomBar.OpenTab(
                        DeeplinkBottomBarTab.DEVICE
                    )
                )
            }
        )
    }
}

@Composable
fun ComposableFapNoInstallButton(
    fapButtonSize: FapButtonSize,
    modifier: Modifier = Modifier
) {
    ComposableFlipperButton(
        modifier = modifier,
        text = stringResource(R.string.faphub_installation_install),
        color = LocalPallet.current.text20,
        fapButtonSize = fapButtonSize
    )
}

@Composable
fun ComposableFapInstalledButton(
    fapButtonSize: FapButtonSize,
    modifier: Modifier = Modifier
) {
    ComposableFlipperButton(
        modifier = modifier,
        text = stringResource(R.string.faphub_installation_installed),
        color = LocalPallet.current.text20,
        fapButtonSize = fapButtonSize
    )
}

@Composable
fun ComposableFapUpdateButton(
    fapButtonSize: FapButtonSize,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ComposableFlipperButton(
        modifier = modifier,
        text = stringResource(R.string.faphub_installation_update),
        color = LocalPallet.current.updateProgressGreen,
        fapButtonSize = fapButtonSize,
        onClick = onClick
    )
}

@Composable
fun ComposableFapInstallingButton(
    percent: Float,
    fapButtonSize: FapButtonSize,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundDownload: Boolean = false
) {
    ComposableInProgressFapButton(
        fapButtonModifier = modifier,
        percent = percent,
        backgroundDownload = backgroundDownload,
        color = LocalPallet.current.accent,
        installLabelCompact = R.string.faphub_installation_step_install_compact,
        installLabelLarge = R.string.faphub_installation_step_install,
        fapButtonSize = fapButtonSize,
        onCancel = onCancel
    )
}

@Composable
fun ComposableFapUpdatingButton(
    percent: Float,
    fapButtonSize: FapButtonSize,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundDownload: Boolean = false
) {
    ComposableInProgressFapButton(
        fapButtonModifier = modifier,
        percent = percent,
        backgroundDownload = backgroundDownload,
        color = LocalPallet.current.updateProgressGreen,
        installLabelCompact = R.string.faphub_installation_step_update_compact,
        installLabelLarge = R.string.faphub_installation_step_update,
        fapButtonSize = fapButtonSize,
        onCancel = onCancel
    )
}

@Composable
fun ComposableFapCancelingButton(
    fapButtonSize: FapButtonSize,
    modifier: Modifier = Modifier
) {
    ComposableFlipperButton(
        modifier = modifier,
        text = stringResource(R.string.faphub_installation_canceling),
        color = LocalPallet.current.updateProgressGreen,
        fapButtonSize = fapButtonSize
    )
}

@Preview
@Composable
private fun ComposableFapInstallButtonPreview() {
    FlipperThemeInternal {
        Column {
            ComposableFapInstallButton(fapButtonSize = FapButtonSize.LARGE, onClick = {})
            ComposableFapInstalledButton(fapButtonSize = FapButtonSize.LARGE)
            ComposableFapUpdateButton(fapButtonSize = FapButtonSize.LARGE, onClick = {})
            ComposableFapInstallingButton(
                percent = 0.5f,
                fapButtonSize = FapButtonSize.LARGE,
                onCancel = {}
            )
            ComposableFapUpdatingButton(
                percent = 0.5f,
                fapButtonSize = FapButtonSize.LARGE,
                onCancel = {}
            )
        }
    }
}
