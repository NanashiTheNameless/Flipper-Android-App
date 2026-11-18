package dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.preference.pb.FileManagerOrientation
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal

@Preview
@Composable
private fun FolderCardPlaceholderComposablePreview() {
    FlipperThemeInternal {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(
                FileManagerOrientation.LIST,
                FileManagerOrientation.GRID
            ).forEach { orientation ->
                FolderCardPlaceholderComposable(orientation = orientation)
            }
        }
    }
}
