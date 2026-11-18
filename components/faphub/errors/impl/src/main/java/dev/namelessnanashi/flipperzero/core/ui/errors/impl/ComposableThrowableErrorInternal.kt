package dev.namelessnanashi.flipperzero.core.ui.errors.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable.ComposableFlipperFirmwareNotSupported
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable.ComposableFlipperNotConnectedError
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable.ComposableGeneralError
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable.ComposableNoNetworkError
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable.ComposableNoSdCard
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable.ComposableNoServerError
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable.ComposableNotFound
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable.ComposableWrongRequestError
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapErrorSize
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError

@Composable
fun ComposableThrowableErrorInternal(
    throwable: FapHubError,
    onRetry: () -> Unit,
    onOpenDeviceScreen: () -> Unit,
    fapErrorSize: FapErrorSize,
    modifier: Modifier = Modifier
) {
    when (throwable) {
        FapHubError.NO_NETWORK -> ComposableNoNetworkError(
            modifier = modifier,
            onRetry = onRetry,
            fapErrorSize = fapErrorSize
        )

        FapHubError.FIRMWARE_NOT_SUPPORTED -> ComposableFlipperFirmwareNotSupported(
            modifier = modifier,
            onOpenDeviceScreen = onOpenDeviceScreen
        )

        FapHubError.WRONG_REQUEST -> ComposableWrongRequestError(
            modifier = modifier,
            onRetry = onRetry,
            fapErrorSize = fapErrorSize
        )

        FapHubError.FLIPPER_NOT_CONNECTED -> ComposableFlipperNotConnectedError(
            modifier = modifier,
            onRetry = onRetry,
            fapErrorSize = fapErrorSize
        )

        FapHubError.NO_SERVER -> ComposableNoServerError(
            modifier = modifier,
            onRetry = onRetry,
            fapErrorSize = fapErrorSize
        )

        FapHubError.GENERAL -> ComposableGeneralError(
            modifier = modifier,
            onRetry = onRetry,
            fapErrorSize = fapErrorSize
        )

        FapHubError.NO_SD_CARD -> ComposableNoSdCard(
            modifier = modifier,
            onRetry = onRetry,
            fapErrorSize = fapErrorSize
        )

        FapHubError.NOT_FOUND_REQUEST -> ComposableNotFound(
            modifier = modifier,
            onRetry = onRetry,
            fapErrorSize = fapErrorSize
        )
    }
}
