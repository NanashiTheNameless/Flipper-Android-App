package dev.namelessnanashi.flipperzero.faphub.category.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.faphub.category.impl.composable.ComposableFapHubCategory
import dev.namelessnanashi.flipperzero.faphub.category.impl.model.FapCategoryNavigationConfig
import dev.namelessnanashi.flipperzero.faphub.category.impl.viewmodel.FapHubCategoryViewModel
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapInstallationUIApi
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.toFapButtonConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@Suppress("LongParameterList")
class FapHubCategoryScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val fapCategory: FapCategory,
    @Assisted private val navigation: StackNavigation<FapCategoryNavigationConfig>,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val categoryViewModelFactory: FapHubCategoryViewModel.Factory,
    private val errorsRenderer: FapHubComposableErrorsRenderer,
    private val fapInstallationUIApi: FapInstallationUIApi
) : ScreenDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val categoryViewModel = viewModelWithFactory(key = fapCategory.toString()) {
            categoryViewModelFactory(fapCategory)
        }
        val fapsList = categoryViewModel.getFapsFlow().collectAsLazyPagingItems()
        val sortType by categoryViewModel.getSortTypeFlow().collectAsState()

        ComposableFapHubCategory(
            onBack = onBack::invoke,
            onOpenSearch = {
                navigation.pushToFront(FapCategoryNavigationConfig.Search)
            },
            onOpenFapItem = {
                navigation.pushToFront(FapCategoryNavigationConfig.FapScreen(it.id))
            },
            errorsRenderer = errorsRenderer,
            installationButton = { fapItem, modifier ->
                fapInstallationUIApi.ComposableButton(
                    config = fapItem?.toFapButtonConfig(),
                    modifier = modifier,
                    fapButtonSize = FapButtonSize.COMPACTED,
                    componentContext = this
                )
            },
            categoryName = fapCategory.name,
            onSelectSortType = categoryViewModel::onSelectSortType,
            fapsList = fapsList,
            sortType = sortType
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            fapCategory: FapCategory,
            navigation: StackNavigation<FapCategoryNavigationConfig>,
            onBack: DecomposeOnBackParameter,
        ): FapHubCategoryScreenDecomposeComponentImpl
    }
}
