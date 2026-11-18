package dev.namelessnanashi.flipperzero.faphub.installation.manifest.model

import dev.namelessnanashi.flipperzero.core.data.SemVer

data class FapManifestVersion(
    val versionUid: String,
    val semVer: SemVer
)
