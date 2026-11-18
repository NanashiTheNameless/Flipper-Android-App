package dev.namelessnanashi.flipperzero.faphub.report.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.faphub.report.impl.composable.concern.ComposableReport
import dev.namelessnanashi.flipperzero.faphub.report.impl.viewmodel.ReportViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

class FapReportConcernDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val applicationUid: String,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val reportViewModelFactory: ReportViewModel.Factory
) : ScreenDecomposeComponent(componentContext) {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val reportViewModel = viewModelWithFactory(key = applicationUid) {
            reportViewModelFactory(applicationUid)
        }
        val state by reportViewModel.getFapReportState().collectAsState()
        ComposableReport(
            onBack = onBack::invoke,
            fapReportState = state,
            submit = { reportViewModel.submit(onBack::invoke, it) }
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            applicationUid: String,
            onBack: DecomposeOnBackParameter
        ): FapReportConcernDecomposeComponentImpl
    }
}
