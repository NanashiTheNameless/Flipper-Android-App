package dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.states

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ui.ktx.placeholderConnecting
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonConfig
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFapCancelingButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFapInstallButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFapInstalledButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFapInstallingButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFapNoInstallButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFapUpdateButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFapUpdatingButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFlipperNoSdCardButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.ComposableFlipperNotConnectedButton
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.viewmodel.FapStatusViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.viewmodel.OpenFapViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.api.model.FapState
import dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.api.model.NotAvailableReason

@Composable
@Suppress("LongMethod")
internal fun ComposableFapButton(
    localState: FapState,
    fapButtonSize: FapButtonSize,
    statusViewModel: FapStatusViewModel,
    config: FapButtonConfig?,
    openViewModel: OpenFapViewModel,
    modifier: Modifier = Modifier
) {
    when (localState) {
        is FapState.InstallationInProgress -> ComposableFapInstallingButton(
            modifier = modifier,
            fapButtonSize = fapButtonSize,
            percent = localState.progress,
            backgroundDownload = !localState.active,
            onCancel = { statusViewModel.cancel(config) }
        )

        FapState.Installed ->
            ComposableInstalledButton(
                config = config,
                fapButtonSize = fapButtonSize,
                modifier = modifier,
                viewModel = openViewModel
            )

        FapState.ReadyToInstall -> ComposableFapInstallButton(
            modifier = modifier,
            fapButtonSize = fapButtonSize,
            onClick = { statusViewModel.install(config) }
        )

        FapState.NotInitialized,
        FapState.Deleting,
        FapState.RetrievingManifest -> ComposableFapInstalledButton(
            modifier = modifier.placeholderConnecting(),
            fapButtonSize = fapButtonSize
        )

        is FapState.ReadyToUpdate -> ComposableFapUpdateButton(
            modifier = modifier,
            fapButtonSize = fapButtonSize,
            onClick = { statusViewModel.update(config, localState.from) }
        )

        is FapState.UpdatingInProgress -> ComposableFapUpdatingButton(
            modifier = modifier,
            fapButtonSize = fapButtonSize,
            percent = localState.progress,
            backgroundDownload = !localState.active,
            onCancel = { statusViewModel.cancel(config) }
        )

        FapState.Canceling -> ComposableFapCancelingButton(
            fapButtonSize = fapButtonSize,
            modifier = modifier
        )

        is FapState.NotAvailableForInstall -> when (localState.reason) {
            NotAvailableReason.BUILD_RUNNING,
            NotAvailableReason.UNSUPPORTED_APP,
            NotAvailableReason.FLIPPER_OUTDATED,
            NotAvailableReason.NOT_AVAILABLE_ONLINE,
            NotAvailableReason.UNSUPPORTED_SDK -> ComposableFapNoInstallButton(
                modifier = modifier,
                fapButtonSize = fapButtonSize
            )

            NotAvailableReason.NO_SD_CARD -> ComposableFlipperNoSdCardButton(
                modifier = modifier,
                fapButtonSize = fapButtonSize,
            )

            NotAvailableReason.FLIPPER_NOT_CONNECTED -> ComposableFlipperNotConnectedButton(
                modifier = modifier,
                fapButtonSize = fapButtonSize
            )
        }
    }
}
