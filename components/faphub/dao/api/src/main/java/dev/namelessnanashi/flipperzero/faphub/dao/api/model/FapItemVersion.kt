package dev.namelessnanashi.flipperzero.faphub.dao.api.model

import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget

data class FapItemVersion(
    val id: String,
    val version: SemVer,
    val target: FlipperTarget,
    val buildState: FapBuildState,
    val sdkApi: SemVer?
)
