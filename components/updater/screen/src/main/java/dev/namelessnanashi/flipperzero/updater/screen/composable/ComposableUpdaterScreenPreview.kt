package dev.namelessnanashi.flipperzero.updater.screen.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.preference.pb.HardwareColor
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.updater.model.DistributionFile
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.updater.model.OfficialFirmware
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.namelessnanashi.flipperzero.updater.screen.model.FailedReason
import dev.namelessnanashi.flipperzero.updater.screen.model.UpdaterScreenState

private val updateRequest = UpdateRequest(
    updateFrom = FirmwareVersion(
        channel = FirmwareChannel.RELEASE,
        version = "0.65.2"
    ),
    updateTo = FirmwareVersion(
        channel = FirmwareChannel.DEV,
        version = "0.65.2 1234"
    ),
    changelog = "",
    content = OfficialFirmware(DistributionFile(url = "", sha256 = ""))
)

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenNotStartedPreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState = UpdaterScreenState.NotStarted,
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenCancelingSynchronizationPreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState = UpdaterScreenState.CancelingSynchronization(updateRequest),
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenDownloadingFromNetworkPreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState = UpdaterScreenState.DownloadingFromNetwork(
                updateRequest = updateRequest,
                percent = 0.5f
            ),
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenUploadOnFlipperPreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState =
            UpdaterScreenState.UploadOnFlipper(updateRequest = updateRequest, percent = 0.5f),
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenFailedNetworkPreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState = UpdaterScreenState.Failed(FailedReason.DOWNLOAD_FROM_NETWORK),
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenFailedOnFlipperPreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState = UpdaterScreenState.Failed(FailedReason.UPLOAD_ON_FLIPPER),
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenRebootingPreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState = UpdaterScreenState.Rebooting,
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenCancelingUpdatePreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState = UpdaterScreenState.CancelingUpdate,
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true
)
private fun ComposableUpdaterScreenFinishPreview() {
    FlipperThemeInternal {
        ComposableUpdaterScreen(
            updaterScreenState = UpdaterScreenState.Finish,
            flipperColor = HardwareColor.BLACK,
            onCancel = {},
            onRetry = {},
            changelog = null
        )
    }
}
