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
    val isDevCatalog: Boolean,
    val externalSource: ExternalAppSource? = null
)

/** Portable provenance. A null source identifies an existing official catalog installation. */
data class ExternalAppSource(
    val url: String,
    val revision: String,
    val tracking: String,
    val sha256: String,
    val project: String = "",
    val sdkUrl: String = "",
    val sdkSha256: String = "",
    val firmware: String = "",
    val backend: String = "",
    val sourceSha256: String = ""
)
