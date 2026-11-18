package dev.namelessnanashi.flipperzero.archive.search.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.archive.api.SelectKeyPathListener
import dev.namelessnanashi.flipperzero.archive.search.composable.ComposableSearch
import dev.namelessnanashi.flipperzero.archive.search.viewmodel.SearchViewModel
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider

class SearchScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onItemSelected: SelectKeyPathListener?,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val searchViewModelProvider: Provider<SearchViewModel>,
    private val synchronizationUiApi: SynchronizationUiApi,
) : ScreenDecomposeComponent(componentContext) {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val keyboard = LocalSoftwareKeyboardController.current
        val searchViewModel: SearchViewModel = viewModelWithFactory(key = null) {
            searchViewModelProvider.invoke()
        }

        val rootNavigation = LocalRootNavigation.current

        ComposableSearch(
            searchViewModel = searchViewModel,
            synchronizationUiApi = synchronizationUiApi,
            onBack = {
                keyboard?.hide()
                onBack()
            },
            onOpenKeyScreen = { flipperKeyPath ->
                keyboard?.hide()
                if (onItemSelected != null) {
                    onItemSelected.invoke(flipperKeyPath)
                } else {
                    rootNavigation.push(RootScreenConfig.OpenKey(flipperKeyPath))
                }
            }
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onItemSelected: SelectKeyPathListener?,
            onBack: DecomposeOnBackParameter
        ): SearchScreenDecomposeComponentImpl
    }
}
