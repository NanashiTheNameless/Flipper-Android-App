package dev.namelessnanashi.flipperzero.filemanager.ui.components.sdcard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal

@Preview
@Composable
private fun SdCardMissingComposablePreview() {
    FlipperThemeInternal {
        SdCardMissingComposable()
    }
}

@Preview
@Composable
private fun SdCardOkComposablePreview() {
    FlipperThemeInternal {
        SdCardOkComposable(
            used = 1234562,
            total = 8929921
        )
    }
}

@Preview
@Composable
private fun SdCardLoadingComposablePreview() {
    FlipperThemeInternal {
        SdCardLoadingComposable()
    }
}
