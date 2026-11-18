package dev.namelessnanashi.flipperzero.faphub.report.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.faphub.report.api.FapReportArgument
import dev.namelessnanashi.flipperzero.faphub.report.api.FapReportDecomposeComponent
import dev.namelessnanashi.flipperzero.faphub.report.impl.model.FapReportNavigationConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, FapReportDecomposeComponent.Factory::class)
class FapReportDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted fapReportArgument: FapReportArgument,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val reportSelectFactory: FapReportSelectDecomposeComponentImpl.Factory,
    private val reportConcernFactory: FapReportConcernDecomposeComponentImpl.Factory,
    private val reportBugFactory: FapReportBugDecomposeComponentImpl.Factory
) : FapReportDecomposeComponent<FapReportNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<FapReportNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = FapReportNavigationConfig.serializer(),
        initialConfiguration = FapReportNavigationConfig.ReportSelect(fapReportArgument),
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: FapReportNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is FapReportNavigationConfig.ReportBug -> reportBugFactory(
            componentContext = componentContext,
            reportUrl = config.reportUrl,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is FapReportNavigationConfig.ReportConcern -> reportConcernFactory(
            componentContext = componentContext,
            applicationUid = config.applicationUid,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is FapReportNavigationConfig.ReportSelect -> reportSelectFactory(
            componentContext = componentContext,
            fapReportArgument = config.fapReportArgument,
            navigation = navigation,
            onBack = { navigation.popOr(onBack::invoke) }
        )
    }
}
