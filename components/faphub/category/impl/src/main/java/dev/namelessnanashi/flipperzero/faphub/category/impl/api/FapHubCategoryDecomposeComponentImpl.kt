package dev.namelessnanashi.flipperzero.faphub.category.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.faphub.category.api.FapHubCategoryDecomposeComponent
import dev.namelessnanashi.flipperzero.faphub.category.impl.model.FapCategoryNavigationConfig
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.fapscreen.api.FapScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.faphub.search.api.FapHubSearchDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, FapHubCategoryDecomposeComponent.Factory::class)
class FapHubCategoryDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val fapCategory: FapCategory,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val fapScreenFactory: FapScreenDecomposeComponent.Factory,
    private val fapSearchFactory: FapHubSearchDecomposeComponent.Factory,
    private val fapCategoryScreenFactory: FapHubCategoryScreenDecomposeComponentImpl.Factory
) : FapHubCategoryDecomposeComponent<FapCategoryNavigationConfig>(), ComponentContext by componentContext {
    override val stack: Value<ChildStack<FapCategoryNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = FapCategoryNavigationConfig.serializer(),
        initialConfiguration = FapCategoryNavigationConfig.CategoryList(fapCategory),
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: FapCategoryNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is FapCategoryNavigationConfig.CategoryList -> fapCategoryScreenFactory(
            componentContext = componentContext,
            fapCategory = config.fapCategory,
            navigation = navigation,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is FapCategoryNavigationConfig.FapScreen -> fapScreenFactory(
            componentContext = componentContext,
            id = config.id,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        FapCategoryNavigationConfig.Search -> fapSearchFactory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) }
        )
    }
}
