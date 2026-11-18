package dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.states

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.busy.ComposableFlipperBusy
import dev.namelessnanashi.flipperzero.core.ui.ktx.placeholderConnecting
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonConfig
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFapInstalledButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.buttons.ComposableFapOpenButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.buttons.ComposableFapOpeningButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.model.OpenFapState
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.viewmodel.OpenFapViewModel
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig

@Composable
internal fun ComposableInstalledButton(
    config: FapButtonConfig?,
    fapButtonSize: FapButtonSize,
    viewModel: OpenFapViewModel,
    modifier: Modifier = Modifier
) {
    val rootNavigation = LocalRootNavigation.current

    val stateFlow = remember(config) { viewModel.getOpenFapState(config) }
    val state by stateFlow.collectAsState(OpenFapState.Loading)

    val dialogState by viewModel.getDialogState().collectAsState()

    if (dialogState) {
        ComposableFlipperBusy(
            onDismiss = viewModel::closeDialog,
            goToRemote = { rootNavigation.push(RootScreenConfig.ScreenStreaming) }
        )
    }

    when (val localState = state) {
        is OpenFapState.InProgress ->
            if (localState.config == config) {
                ComposableFapOpeningButton(
                    modifier = modifier,
                    fapButtonSize = fapButtonSize,
                )
            } else {
                ComposableFapOpenButton(
                    modifier = modifier,
                    fapButtonSize = fapButtonSize,
                    onClick = {
                        viewModel.open(
                            config
                        ) { rootNavigation.push(RootScreenConfig.ScreenStreaming) }
                    }
                )
            }

        OpenFapState.NotSupported ->
            ComposableFapInstalledButton(
                modifier = modifier,
                fapButtonSize = fapButtonSize
            )

        OpenFapState.Loading ->
            ComposableFapInstalledButton(
                modifier = modifier.placeholderConnecting(),
                fapButtonSize = fapButtonSize
            )

        OpenFapState.Ready ->
            ComposableFapOpenButton(
                modifier = modifier,
                fapButtonSize = fapButtonSize,
                onClick = {
                    viewModel.open(
                        config
                    ) { rootNavigation.push(RootScreenConfig.ScreenStreaming) }
                }
            )
    }
}
