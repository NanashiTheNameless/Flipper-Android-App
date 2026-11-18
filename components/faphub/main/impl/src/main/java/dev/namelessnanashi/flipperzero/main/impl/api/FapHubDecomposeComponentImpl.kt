package dev.namelessnanashi.flipperzero.main.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.faphub.category.api.FapHubCategoryDecomposeComponent
import dev.namelessnanashi.flipperzero.faphub.fapscreen.api.FapScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.faphub.main.api.FapHubDecomposeComponent
import dev.namelessnanashi.flipperzero.faphub.search.api.FapHubSearchDecomposeComponent
import dev.namelessnanashi.flipperzero.main.impl.model.FapHubNavigationConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, FapHubDecomposeComponent.Factory::class)
@Suppress("LongParameterList")
class FapHubDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted deeplink: Deeplink.BottomBar.AppsTab?,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val fapScreenFactory: FapScreenDecomposeComponent.Factory,
    private val fapSearchFactory: FapHubSearchDecomposeComponent.Factory,
    private val fapCategoryFactory: FapHubCategoryDecomposeComponent.Factory,
    private val mainScreenFactory: MainScreenDecomposeComponentImpl.Factory
) : FapHubDecomposeComponent<FapHubNavigationConfig>(), ComponentContext by componentContext {
    override val stack: Value<ChildStack<FapHubNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = FapHubNavigationConfig.serializer(),
        initialStack = { getInitialStack(deeplink) },
        handleBackButton = true,
        childFactory = ::child
    )

    private fun child(
        config: FapHubNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is FapHubNavigationConfig.Main -> mainScreenFactory(
            componentContext = componentContext,
            navigation = navigation,
            deeplink = config.deeplink
        )

        is FapHubNavigationConfig.FapScreen -> fapScreenFactory(
            componentContext = componentContext,
            id = config.id,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        FapHubNavigationConfig.Search -> fapSearchFactory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is FapHubNavigationConfig.Category -> fapCategoryFactory(
            componentContext = componentContext,
            category = config.fapCategory,
            onBack = { navigation.popOr(onBack::invoke) }
        )
    }

    override fun handleDeeplink(deeplink: Deeplink.BottomBar.AppsTab) {
        navigation.navigate { getInitialStack(deeplink) }
    }

    private fun getInitialStack(deeplink: Deeplink.BottomBar.AppsTab?): List<FapHubNavigationConfig> {
        return if (deeplink is Deeplink.BottomBar.AppsTab) {
            when (deeplink) {
                is Deeplink.BottomBar.AppsTab.Fap -> listOf(
                    FapHubNavigationConfig.Main(deeplink = null),
                    FapHubNavigationConfig.FapScreen(deeplink.appId)
                )

                is Deeplink.BottomBar.AppsTab.MainScreen -> listOf(
                    FapHubNavigationConfig.Main(
                        deeplink
                    )
                )
            }
        } else {
            listOf(FapHubNavigationConfig.Main(deeplink = null))
        }
    }
}
