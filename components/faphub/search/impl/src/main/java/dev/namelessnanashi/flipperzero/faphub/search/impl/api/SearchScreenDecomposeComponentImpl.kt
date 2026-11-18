package dev.namelessnanashi.flipperzero.faphub.search.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapInstallationUIApi
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.toFapButtonConfig
import dev.namelessnanashi.flipperzero.faphub.search.impl.composable.ComposableSearchScreen
import dev.namelessnanashi.flipperzero.faphub.search.impl.model.FapHubSearchNavigationConfig
import dev.namelessnanashi.flipperzero.faphub.search.impl.viewmodel.FapHubSearchViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider

@Suppress("LongParameterList")
class SearchScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val navigation: StackNavigation<FapHubSearchNavigationConfig>,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val fapInstallationUIApi: FapInstallationUIApi,
    private val errorsRenderer: FapHubComposableErrorsRenderer,
    private val searchViewModelProvider: Provider<FapHubSearchViewModel>
) : ScreenDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val searchViewModel = viewModelWithFactory(key = null) {
            searchViewModelProvider.invoke()
        }
        val fapsList = searchViewModel.faps.collectAsLazyPagingItems()
        val searchRequest by searchViewModel.getSearchRequest().collectAsState()

        ComposableSearchScreen(
            onBack = onBack::invoke,
            onFapItemClick = {
                navigation.pushToFront(FapHubSearchNavigationConfig.FapScreen(it.id))
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
            fapsList = fapsList,
            onChangeText = searchViewModel::onChangeSearchText,
            searchRequest = searchRequest
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            navigation: StackNavigation<FapHubSearchNavigationConfig>,
            onBack: DecomposeOnBackParameter
        ): SearchScreenDecomposeComponentImpl
    }
}
