package dev.namelessnanashi.flipperzero.faphub.report.impl.model

import dev.namelessnanashi.flipperzero.faphub.report.api.FapReportArgument
import kotlinx.serialization.Serializable

@Serializable
sealed class FapReportNavigationConfig {
    @Serializable
    data class ReportSelect(val fapReportArgument: FapReportArgument) : FapReportNavigationConfig()

    @Serializable
    data class ReportConcern(val applicationUid: String) : FapReportNavigationConfig()

    @Serializable
    data class ReportBug(val reportUrl: String) : FapReportNavigationConfig()
}
