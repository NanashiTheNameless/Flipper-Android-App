package dev.namelessnanashi.flipperzero.filemanager.listing.impl.composable

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.core.ktx.jre.toFormattedSize
import dev.namelessnanashi.flipperzero.core.preference.pb.FileManagerOrientation
import dev.namelessnanashi.flipperzero.filemanager.listing.api.model.ExtendedListingItem
import dev.namelessnanashi.flipperzero.filemanager.listing.impl.model.PathWithType
import dev.namelessnanashi.flipperzero.filemanager.listing.impl.viewmodel.DeleteFilesViewModel
import dev.namelessnanashi.flipperzero.filemanager.listing.impl.viewmodel.FilesViewModel
import dev.namelessnanashi.flipperzero.filemanager.listing.impl.viewmodel.SelectionViewModel
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.FolderCardComposable
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.FolderCardPlaceholderComposable
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.components.asPainter
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.components.asTint
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.model.ItemUiSelectionState
import flipperapp.components.filemngr.listing.impl.generated.resources.fml_items_in_folder
import okio.Path
import org.jetbrains.compose.resources.stringResource
import flipperapp.components.filemngr.listing.impl.generated.resources.Res as FML

@Suppress("FunctionNaming", "LongParameterList", "LongMethod")
fun LazyGridScope.LoadedFilesComposable(
    path: Path,
    deleteFileState: DeleteFilesViewModel.State,
    selectionState: SelectionViewModel.State,
    filesState: FilesViewModel.State.Loaded,
    orientation: FileManagerOrientation,
    canDeleteFiles: Boolean,
    onPathChanged: (Path) -> Unit,
    onEditFileClick: (Path) -> Unit,
    onCheckToggle: (PathWithType) -> Unit,
    onDelete: (Path) -> Unit,
    onFileMoreClick: (PathWithType) -> Unit
) {
    items(filesState.files) { file ->
        val isFileLoading = remember(deleteFileState.fileNamesOrNull) {
            deleteFileState.fileNamesOrNull.orEmpty().contains(file.itemName)
        }
        Crossfade(
            targetState = isFileLoading,
            modifier = Modifier.animateItem()
        ) { animatedIsFileLoading ->
            if (animatedIsFileLoading) {
                FolderCardPlaceholderComposable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    orientation = orientation,
                )
            } else {
                val filePathWithType = remember(path, file.itemName) {
                    val fullPath = path.resolve(file.itemName)
                    PathWithType(
                        fileType = file.itemType,
                        fullPath = fullPath,
                        size = file.sizeOrNull ?: 0
                    )
                }
                FolderCardComposable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    painter = file.asListingItem().asPainter(),
                    iconTint = file.asListingItem().asTint(),
                    title = file.itemName,
                    canDeleteFiles = canDeleteFiles,
                    subtitle = when (file) {
                        is ExtendedListingItem.File -> file.size.toFormattedSize()
                        is ExtendedListingItem.Folder -> stringResource(
                            resource = FML.string.fml_items_in_folder,
                            file.itemsCount ?: 0
                        )
                    },
                    isSubtitleLoading = when (file) {
                        is ExtendedListingItem.File -> false
                        is ExtendedListingItem.Folder -> file.itemsCount == null
                    },
                    selectionState = when {
                        selectionState.selected.contains(filePathWithType) -> ItemUiSelectionState.SELECTED
                        selectionState.isEnabled -> ItemUiSelectionState.UNSELECTED
                        else -> ItemUiSelectionState.NONE
                    },
                    onClick = {
                        when (file.itemType) {
                            FileType.DIR -> {
                                onPathChanged.invoke(filePathWithType.fullPath)
                            }

                            FileType.FILE -> {
                                onEditFileClick(filePathWithType.fullPath)
                            }
                        }
                    },
                    onCheckChange = { onCheckToggle.invoke(filePathWithType) },
                    onMoreClick = { onFileMoreClick.invoke(filePathWithType) },
                    onDelete = { onDelete.invoke(path.resolve(file.itemName)) },
                    orientation = orientation
                )
            }
        }
    }
}
