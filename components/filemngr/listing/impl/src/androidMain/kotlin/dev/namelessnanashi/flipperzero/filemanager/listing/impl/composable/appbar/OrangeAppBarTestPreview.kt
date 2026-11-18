package dev.namelessnanashi.flipperzero.filemanager.listing.impl.composable.appbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal

@Preview
@Composable
private fun OrangeAppBarTestPreview() {
    FlipperThemeInternal {
        CloseSelectionAppBar(
            onClose = {},
            onDeselectAll = {},
            onSelectAll = {}
        )
    }
}
