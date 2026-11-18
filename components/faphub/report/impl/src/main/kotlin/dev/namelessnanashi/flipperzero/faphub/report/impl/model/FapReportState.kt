package dev.namelessnanashi.flipperzero.faphub.report.impl.model

sealed class FapReportState {
    object ReadyToReport : FapReportState()

    object Uploading : FapReportState()
}
