package dev.namelessnanashi.flipperzero.archive.search.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.archive.api.SearchDecomposeComponent
import dev.namelessnanashi.flipperzero.archive.api.SelectKeyPathListener
import dev.namelessnanashi.flipperzero.archive.search.model.SearchNavigationConfig
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, SearchDecomposeComponent.Factory::class)
class SearchDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onItemSelected: SelectKeyPathListener?,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val searchScreenFactory: SearchScreenDecomposeComponentImpl.Factory
) : SearchDecomposeComponent<SearchNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<SearchNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = SearchNavigationConfig.serializer(),
        initialConfiguration = SearchNavigationConfig.Search,
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: SearchNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is SearchNavigationConfig.Search -> searchScreenFactory(
            componentContext = componentContext,
            onItemSelected = onItemSelected,
            onBack = { navigation.popOr(onBack::invoke) }
        )
    }
}
