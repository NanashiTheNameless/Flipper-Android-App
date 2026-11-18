package dev.namelessnanashi.flipperzero.filemanager.search.impl.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal

@Preview
@Composable
private fun NoFilesComposablePreview() {
    FlipperThemeInternal {
        NoFilesComposable()
    }
}
