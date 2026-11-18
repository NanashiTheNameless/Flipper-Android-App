package dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.model

import dev.namelessnanashi.flipperzero.faphub.report.api.FapReportArgument
import kotlinx.serialization.Serializable

@Serializable
sealed class FapScreenNavigationConfig {

    @Serializable
    data class Main(val id: String) : FapScreenNavigationConfig()

    @Serializable
    data class FapReport(
        val fapReportArgument: FapReportArgument
    ) : FapScreenNavigationConfig()
}
