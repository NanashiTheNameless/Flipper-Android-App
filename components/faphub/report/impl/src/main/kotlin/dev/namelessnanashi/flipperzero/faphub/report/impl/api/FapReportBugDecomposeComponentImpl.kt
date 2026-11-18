package dev.namelessnanashi.flipperzero.faphub.report.impl.api

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.faphub.report.impl.composable.bug.ComposableReportBugInformation
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

class FapReportBugDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val reportUrl: String,
    @Assisted private val onBack: DecomposeOnBackParameter
) : ScreenDecomposeComponent(componentContext) {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val context = LocalContext.current
        ComposableReportBugInformation(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reportUrl))
                context.startActivity(intent)
            },
            onBack = onBack::invoke
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            reportUrl: String,
            onBack: DecomposeOnBackParameter
        ): FapReportBugDecomposeComponentImpl
    }
}
