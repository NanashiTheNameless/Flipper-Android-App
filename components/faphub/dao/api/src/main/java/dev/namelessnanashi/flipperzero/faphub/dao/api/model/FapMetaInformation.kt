package dev.namelessnanashi.flipperzero.faphub.dao.api.model

import dev.namelessnanashi.flipperzero.core.data.SemVer

data class FapMetaInformation(
    val version: SemVer,
    val sizeBytes: Long?
)
