package dev.namelessnanashi.flipperzero.filemanager.search.impl.api

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.core.ui.searchbar.ComposableSearchBar
import dev.namelessnanashi.flipperzero.filemanager.search.api.SearchDecomposeComponent
import dev.namelessnanashi.flipperzero.filemanager.search.impl.composable.ComposableFilesSearchScreen
import dev.namelessnanashi.flipperzero.filemanager.search.impl.viewmodel.SearchViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import okio.Path

@ContributesAssistedFactory(AppGraph::class, SearchDecomposeComponent.Factory::class)
class SearchDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val path: Path,
    @Assisted private val onBack: DecomposeOnBackParameter,
    @Assisted private val onFolderSelect: FolderSelectCallback,
    private val searchViewModelFactory: SearchViewModel.Factory
) : SearchDecomposeComponent(componentContext) {

    @Composable
    override fun Render() {
        val searchViewModel = viewModelWithFactory(path) {
            searchViewModelFactory.invoke(path)
        }
        val rootSearchViewModel = path.root
            .takeIf { !path.isRoot }
            ?.let { rootPath ->
                viewModelWithFactory(rootPath) {
                    searchViewModelFactory.invoke(rootPath)
                }
            }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                val queryState by searchViewModel.queryState.collectAsState()
                ComposableSearchBar(
                    hint = "",
                    text = queryState.query,
                    onChangeText = {
                        searchViewModel.onQueryChange(it)
                        rootSearchViewModel?.onQueryChange(it)
                    },
                    onBack = onBack::invoke
                )
            }
        ) { contentPadding ->
            ComposableFilesSearchScreen(
                searchViewModel = searchViewModel,
                rootSearchViewModel = rootSearchViewModel,
                onFolderSelect = onFolderSelect::invoke,
                modifier = Modifier.padding(contentPadding)
            )
        }
    }
}
