package dev.namelessnanashi.flipperzero.faphub.installation.manifest.model

import dev.namelessnanashi.flipperzero.core.data.SemVer

data class FapManifestItem(
    val applicationAlias: String,
    val uid: String,
    val versionUid: String,
    val path: String,
    val fullName: String,
    val iconBase64: String?,
    val sdkApi: SemVer?,
    val sourceFileHash: String? = null,
    val isDevCatalog: Boolean
)
