package dev.namelessnanashi.flipperzero.filemanager.search.impl.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.preference.pb.FileManagerOrientation
import dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.FolderCardPlaceholderComposable

@Suppress("FunctionNaming")
fun LazyListScope.FolderCardPlaceholderLazyComposable() {
    items(count = 6) {
        FolderCardPlaceholderComposable(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            orientation = FileManagerOrientation.LIST,
        )
    }
}
