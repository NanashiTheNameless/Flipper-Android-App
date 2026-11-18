package dev.namelessnanashi.flipperzero.archive.category.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import dev.namelessnanashi.flipperzero.archive.category.composable.ComposableCategory
import dev.namelessnanashi.flipperzero.archive.category.composable.ComposableDeleted
import dev.namelessnanashi.flipperzero.archive.category.model.CategoryNavigationConfig
import dev.namelessnanashi.flipperzero.archive.category.viewmodels.CategoryViewModel
import dev.namelessnanashi.flipperzero.archive.category.viewmodels.DeleteViewModel
import dev.namelessnanashi.flipperzero.archive.model.CategoryType
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider

@Suppress("LongParameterList")
class CategoryScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val categoryType: CategoryType,
    @Assisted private val navigation: StackNavigation<CategoryNavigationConfig>,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val deleteViewModelProvider: Provider<DeleteViewModel>,
    private val categoryViewModelFactory: CategoryViewModel.Factory,
    private val synchronizationUiApi: SynchronizationUiApi,
) : ScreenDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val deleteViewModel = viewModelWithFactory(key = null) {
            deleteViewModelProvider.invoke()
        }
        val categoryViewModel = viewModelWithFactory(key = categoryType.toString()) {
            categoryViewModelFactory(categoryType)
        }
        val categoryState by categoryViewModel.getState().collectAsState()
        val synchronizationState by categoryViewModel.getSynchronizationState().collectAsState()

        val rootNavigation = LocalRootNavigation.current

        val onOpenKeyScreen: (FlipperKeyPath) -> Unit = { flipperKeyPath ->
            rootNavigation.push(RootScreenConfig.OpenKey(flipperKeyPath))
        }
        when (categoryType) {
            is CategoryType.ByFileType -> ComposableCategory(
                categoryType = categoryType,
                synchronizationUiApi = synchronizationUiApi,
                onBack = { navigation.popOr(onBack::invoke) },
                onOpenKeyScreen = onOpenKeyScreen,
                categoryState = categoryState,
                synchronizationState = synchronizationState,
            )

            CategoryType.Deleted -> ComposableDeleted(
                onBack = { navigation.popOr(onBack::invoke) },
                onOpenKeyScreen = onOpenKeyScreen,
                onRestoreAll = deleteViewModel::onRestoreAll,
                onDeleteAll = deleteViewModel::onDeleteAll,
                categoryState = categoryState,
                synchronizationState = synchronizationState
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            categoryType: CategoryType,
            navigation: StackNavigation<CategoryNavigationConfig>,
            onBack: DecomposeOnBackParameter
        ): CategoryScreenDecomposeComponentImpl
    }
}
