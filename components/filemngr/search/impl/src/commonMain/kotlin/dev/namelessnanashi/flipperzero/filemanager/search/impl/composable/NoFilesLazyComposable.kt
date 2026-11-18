package dev.namelessnanashi.flipperzero.filemanager.search.impl.composable

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.filemanager.search.impl.viewmodel.SearchViewModel

@Suppress("FunctionNaming")
fun LazyListScope.NoFilesLazyComposable(searchState: SearchViewModel.State.Loaded) {
    if (!searchState.isSearching && searchState.items.isEmpty()) {
        item {
            NoFilesComposable(
                modifier = Modifier
                    .fillParentMaxSize()
                    .animateItem()
            )
        }
    }
}
