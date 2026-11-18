package dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.faphub.fapscreen.api.FapScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.model.FapScreenNavigationConfig
import dev.namelessnanashi.flipperzero.faphub.report.api.FapReportDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, FapScreenDecomposeComponent.Factory::class)
@Suppress("LongParameterList")
class FapScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val id: String,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val fapReportFactory: FapReportDecomposeComponent.Factory,
    private val screenDecomposeFactory: ScreenDecomposeComponentImpl.Factory
) : FapScreenDecomposeComponent<FapScreenNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<FapScreenNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = FapScreenNavigationConfig.serializer(),
        initialConfiguration = FapScreenNavigationConfig.Main(id),
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: FapScreenNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is FapScreenNavigationConfig.Main -> screenDecomposeFactory(
            componentContext = componentContext,
            id = config.id,
            navigation = navigation,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is FapScreenNavigationConfig.FapReport -> fapReportFactory(
            componentContext = componentContext,
            fapReportArgument = config.fapReportArgument,
            onBack = { navigation.popOr(onBack::invoke) }
        )
    }
}
