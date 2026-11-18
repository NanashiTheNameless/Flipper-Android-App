package dev.namelessnanashi.flipperzero.widget.screen.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.archive.api.SearchDecomposeComponent
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.namelessnanashi.flipperzero.widget.api.WidgetDecomposeComponent
import dev.namelessnanashi.flipperzero.widget.screen.model.WidgetNavigationConfig
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, WidgetDecomposeComponent.Factory::class)
class WidgetDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted widgetId: Int,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val searchScreenFactory: SearchDecomposeComponent.Factory,
    private val widgetOptionsFactory: WidgetOptionsDecomposeComponentImpl.Factory
) : WidgetDecomposeComponent<WidgetNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<WidgetNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = WidgetNavigationConfig.serializer(),
        initialConfiguration = WidgetNavigationConfig.WidgetOptions(widgetId),
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: WidgetNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is WidgetNavigationConfig.SearchScreen -> searchScreenFactory(
            componentContext = componentContext,
            onItemSelected = config.listener,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is WidgetNavigationConfig.WidgetOptions -> widgetOptionsFactory(
            componentContext = componentContext,
            widgetId = config.widgetId,
            navigation = navigation
        )
    }
}
