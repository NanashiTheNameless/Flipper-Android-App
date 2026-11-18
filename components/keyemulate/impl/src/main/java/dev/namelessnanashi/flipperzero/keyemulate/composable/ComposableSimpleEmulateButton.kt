package dev.namelessnanashi.flipperzero.keyemulate.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.model.EmulateConfig
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.image.Picture
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.keyemulate.composable.common.ComposableActionDisable
import dev.namelessnanashi.flipperzero.keyemulate.composable.common.ComposableActionLoading
import dev.namelessnanashi.flipperzero.keyemulate.composable.common.ComposableErrorDialogs
import dev.namelessnanashi.flipperzero.keyemulate.composable.common.InternalComposableEmulateButtonWithText
import dev.namelessnanashi.flipperzero.keyemulate.impl.R
import dev.namelessnanashi.flipperzero.keyemulate.model.DisableButtonReason
import dev.namelessnanashi.flipperzero.keyemulate.model.EmulateButtonState
import dev.namelessnanashi.flipperzero.keyemulate.viewmodel.SimpleEmulateViewModel
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig

@Composable
fun ComposableSimpleEmulateButton(
    emulateConfig: EmulateConfig,
    isSynchronized: Boolean,
    emulateViewModel: SimpleEmulateViewModel,
    modifier: Modifier = Modifier
) {
    val rootNavigation = LocalRootNavigation.current
    val emulateButtonState by emulateViewModel.getEmulateButtonStateFlow().collectAsState()

    val buttonActiveModifier = Modifier.clickableRipple {
        if (emulateButtonState is EmulateButtonState.Inactive) {
            emulateViewModel.onStartEmulate(emulateConfig)
        } else if (emulateButtonState is EmulateButtonState.Active) {
            emulateViewModel.onStopEmulate()
        }
    }

    if (!isSynchronized) {
        ComposableActionDisable(
            modifier = modifier,
            textId = R.string.keyscreen_emulate,
            iconId = R.drawable.ic_emulate,
            reason = DisableButtonReason.NOT_SYNCHRONIZED
        )
        return
    }

    ComposableErrorDialogs(emulateButtonState, emulateViewModel::closeDialog) {
        rootNavigation.push(RootScreenConfig.ScreenStreaming)
    }

    when (emulateButtonState) {
        is EmulateButtonState.Disabled -> ComposableActionDisable(
            modifier = modifier,
            textId = R.string.keyscreen_emulate,
            iconId = R.drawable.ic_emulate,
            reason = (emulateButtonState as EmulateButtonState.Disabled).reason
        )

        is EmulateButtonState.Active -> InternalComposableEmulateButtonWithText(
            modifier = modifier,
            buttonModifier = buttonActiveModifier,
            buttonTextId = R.string.keyscreen_emulating,
            color = LocalPallet.current.actionOnFlipperProgress,
            progressColor = LocalPallet.current.actionOnFlipperEnable,
            progress = (emulateButtonState as EmulateButtonState.Active).progress,
            picture = Picture.LottieRes(
                R.raw.ic_emulating,
                R.drawable.ic_emulate
            ),
            textId = R.string.keyscreen_emulating_desc
        )

        is EmulateButtonState.Inactive -> InternalComposableEmulateButtonWithText(
            modifier = modifier,
            buttonTextId = R.string.keyscreen_emulate,
            buttonModifier = buttonActiveModifier,
            color = LocalPallet.current.actionOnFlipperEnable,
            picture = Picture.StaticRes(R.drawable.ic_emulate)
        )

        is EmulateButtonState.Loading -> ComposableActionLoading(
            modifier = modifier,
            loadingState = (emulateButtonState as EmulateButtonState.Loading).state
        )
    }
}
