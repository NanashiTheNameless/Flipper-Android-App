package dev.namelessnanashi.flipperzero.filemanager.listing.impl.composable.modal

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import okio.Path.Companion.toPath

@Preview
@Composable
private fun BottomSheetOptionsPreview() {
    FlipperThemeInternal {
        BottomSheetOptionsContent(
            fileType = FileType.DIR,
            path = "some_file.ir".toPath(),
            onExport = {},
            onDelete = {},
            onCopyTo = {},
            onRename = {},
            onSelect = {},
            onMoveTo = {}
        )
    }
}
