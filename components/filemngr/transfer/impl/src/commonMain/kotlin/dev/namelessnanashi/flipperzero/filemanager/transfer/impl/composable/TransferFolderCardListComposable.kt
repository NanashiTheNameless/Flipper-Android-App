package dev.namelessnanashi.flipperzero.filemanager.transfer.impl.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.core.ktx.jre.toFormattedSize
import dev.namelessnanashi.flipperzero.filemanager.listing.api.model.ExtendedListingItem
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.FolderCardListComposable
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.components.asPainter
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.components.asTint
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.model.ItemUiSelectionState
import flipperapp.components.filemngr.transfer.impl.generated.resources.Res
import flipperapp.components.filemngr.transfer.impl.generated.resources.fmt_items_in_folder
import okio.Path
import org.jetbrains.compose.resources.stringResource

@Composable
fun TransferFolderCardListComposable(
    fullDirPath: Path,
    item: ExtendedListingItem,
    isMoving: Boolean,
    onPathChange: (Path) -> Unit,
    modifier: Modifier = Modifier
) {
    FolderCardListComposable(
        modifier = modifier,
        painter = item.asListingItem().asPainter(),
        iconTint = item.asListingItem().asTint(),
        title = item.itemName,
        subtitle = when (item) {
            is ExtendedListingItem.File -> item.size.toFormattedSize()
            is ExtendedListingItem.Folder -> stringResource(
                resource = Res.string.fmt_items_in_folder,
                item.itemsCount.toString()
            )
        },
        isSubtitleLoading = when (item) {
            is ExtendedListingItem.File -> false
            is ExtendedListingItem.Folder -> item.itemsCount == null
        },
        selectionState = ItemUiSelectionState.NONE,
        onClick = onClick@{
            if (item.itemType != FileType.DIR) return@onClick
            if (isMoving) return@onClick
            onPathChange.invoke(fullDirPath.resolve(item.itemName))
        },
        onCheckChange = null,
        onMoreClick = null
    )
}
