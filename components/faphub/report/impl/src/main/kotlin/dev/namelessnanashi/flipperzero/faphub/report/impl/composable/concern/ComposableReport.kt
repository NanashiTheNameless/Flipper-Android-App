package dev.namelessnanashi.flipperzero.faphub.report.impl.composable.concern

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.ktx.OrangeAppBar
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.faphub.report.impl.R
import dev.namelessnanashi.flipperzero.faphub.report.impl.model.FapReportState

@Composable
internal fun ComposableReport(
    onBack: () -> Unit,
    submit: (String) -> Unit,
    fapReportState: FapReportState,
    modifier: Modifier = Modifier
) = Column(
    modifier
        .fillMaxSize()
) {
    OrangeAppBar(titleId = R.string.fap_report_title, onBack = onBack)

    when (fapReportState) {
        FapReportState.ReadyToReport -> ComposableReadyToReport(submit)
        FapReportState.Uploading -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun PreviewComposableReport() {
    FlipperThemeInternal {
        ComposableReport(
            onBack = {},
            submit = { _ -> },
            fapReportState = FapReportState.ReadyToReport
        )
    }
}
