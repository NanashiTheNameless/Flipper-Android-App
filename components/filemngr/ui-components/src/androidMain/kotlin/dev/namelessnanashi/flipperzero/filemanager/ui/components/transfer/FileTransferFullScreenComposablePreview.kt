package dev.namelessnanashi.flipperzero.filemanager.ui.components.transfer

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal

@Preview
@Composable
private fun FileTransferFullScreenComposablePreview() {
    FlipperThemeInternal {
        FileTransferFullScreenComposable(
            title = "Downloading",
            actionText = "Cancel",
            onActionClick = {},
            progressTitle = "flipperfile.txt",
            progress = 0.3f,
            progressText = "Downloading 3KiB/5KiB",
            speedText = "Speed: 10KiB/s",
            progressDetailText = "1/3 items finished"
        )
    }
}
