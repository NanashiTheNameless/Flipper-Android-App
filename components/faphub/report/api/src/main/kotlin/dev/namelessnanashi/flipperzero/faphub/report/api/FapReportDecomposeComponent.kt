package dev.namelessnanashi.flipperzero.faphub.report.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class FapReportDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            fapReportArgument: FapReportArgument,
            onBack: DecomposeOnBackParameter
        ): FapReportDecomposeComponent<*>
    }
}
