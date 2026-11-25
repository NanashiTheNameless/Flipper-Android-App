package dev.namelessnanashi.flipperzero.archive.impl.api

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.archive.impl.composable.ComposableArchive
import dev.namelessnanashi.flipperzero.archive.impl.model.ArchiveNavigationConfig
import dev.namelessnanashi.flipperzero.archive.impl.viewmodel.CategoryViewModel
import dev.namelessnanashi.flipperzero.archive.impl.viewmodel.GeneralTabViewModel
import dev.namelessnanashi.flipperzero.bottombar.handlers.ResetTabDecomposeHandler
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import dev.zacsweers.metro.Provider

class ArchiveScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val navigation: StackNavigation<ArchiveNavigationConfig>,
    private val synchronizationUiApi: SynchronizationUiApi,
    private val generalTabviewModelProvider: Provider<GeneralTabViewModel>,
    private val categoryViewModelProvider: Provider<CategoryViewModel>,
) : ScreenDecomposeComponent(componentContext), ResetTabDecomposeHandler {
    private val requestScrollToTopFlow = MutableStateFlow(false)

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val tabViewModel = viewModelWithFactory(key = null) {
            generalTabviewModelProvider.invoke()
        }

        val keys by tabViewModel.getKeys().collectAsState()
        val favoriteKeys by tabViewModel.getFavoriteKeys().collectAsState()
        val favoritePaths by tabViewModel.getFavoritePaths().collectAsState()
        val synchronizationState by tabViewModel.getSynchronizationState().collectAsState()
        val favoriteSaveState by tabViewModel.favoriteSaveState.collectAsState()

        val categoryViewModel = viewModelWithFactory(key = null) {
            categoryViewModelProvider.invoke()
        }

        val categories by categoryViewModel.getCategoriesFlow().collectAsState()
        val deletedCategory by categoryViewModel.getDeletedFlow().collectAsState()

        val lazyListState = rememberLazyListState()
        val requestScrollToTop by requestScrollToTopFlow.collectAsState()
        LaunchedEffect(requestScrollToTop) {
            if (requestScrollToTop) {
                lazyListState.animateScrollToItem(0)
                requestScrollToTopFlow.emit(false)
            }
        }

        val rootNavigation = LocalRootNavigation.current

        ComposableArchive(
            synchronizationUiApi = synchronizationUiApi,
            onOpenSearchScreen = {
                navigation.pushToFront(ArchiveNavigationConfig.OpenSearch)
            },
            onOpenKeyScreen = { flipperKeyPath ->
                rootNavigation.push(RootScreenConfig.OpenKey(flipperKeyPath))
            },
            onOpenCategory = { categoryType ->
                navigation.pushToFront(ArchiveNavigationConfig.OpenCategory(categoryType, null))
            },
            keys = keys,
            synchronizationState = synchronizationState,
            favoriteKeys = favoriteKeys,
            favoritePaths = favoritePaths,
            onRefresh = tabViewModel::refresh,
            favoriteSaveState = favoriteSaveState,
            onSaveFavorites = tabViewModel::saveFavorites,
            categories = categories,
            deletedCategory = deletedCategory,
            lazyListState = lazyListState,
        )
    }

    override fun onResetTab() {
        requestScrollToTopFlow.update { true }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            navigation: StackNavigation<ArchiveNavigationConfig>,
        ): ArchiveScreenDecomposeComponentImpl
    }
}
