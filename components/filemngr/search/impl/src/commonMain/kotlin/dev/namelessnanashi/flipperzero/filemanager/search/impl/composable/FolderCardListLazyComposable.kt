package dev.namelessnanashi.flipperzero.filemanager.search.impl.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.core.ktx.jre.toFormattedSize
import dev.namelessnanashi.flipperzero.filemanager.search.impl.viewmodel.SearchViewModel
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.FolderCardListComposable
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.components.asPainter
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.components.asTint
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.model.ItemUiSelectionState
import okio.Path

@Suppress("FunctionNaming")
fun LazyListScope.FolderCardListLazyComposable(
    searchState: SearchViewModel.State.Loaded,
    onFolderSelect: (Path) -> Unit,
) {
    items(searchState.items, key = { it.fullPath.toString() }) { file ->
        FolderCardListComposable(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            painter = file.instance.asPainter(),
            iconTint = file.instance.asTint(),
            title = file.instance.fileName,
            subtitle = file.fullPath.parent
                ?.toString()
                ?: file.instance.size.toFormattedSize(),
            isSubtitleLoading = false,
            selectionState = ItemUiSelectionState.NONE,
            onClick = {
                when (file.instance.fileType) {
                    FileType.DIR -> onFolderSelect.invoke(file.fullPath)

                    FileType.FILE ->
                        file.fullPath
                            .parent
                            ?.run(onFolderSelect::invoke)

                    else -> Unit
                }
            },
            showEndBox = false,
            onCheckChange = {},
            onMoreClick = {},
        )
    }
}
