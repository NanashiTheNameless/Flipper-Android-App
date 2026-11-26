package dev.namelessnanashi.flipperzero.faphub.source.impl

import kotlinx.serialization.Serializable
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath

@Serializable
data class SourceRecord(
    val id: String,
    val inputUrl: String,
    val resolvedUrl: String = "",
    val repository: String = "",
    val repositoryId: String = "",
    val archiveRoot: String = "",
    val revision: String = "",
    val tracking: String = "file",
    val project: String = "",
    val appId: String = "",
    val name: String = "",
    val category: String = "Tools",
    val sdkUrl: String = "",
    val sdkSha256: String = "",
    val firmware: String = "",
    val deviceSerial: String = "",
    val api: String = "",
    val appVersion: Long = 0,
    val toolchainVersion: String = "",
    val sha256: String = "",
    val sourceSha256: String = "",
    val backend: String = "",
    val phase: String = "NEW",
    val log: String = "",
    val message: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val updateAvailable: Boolean = false,
    val sourceProject: Boolean = false,
    val retainedSource: Boolean = false,
    val selectedEntry: String = "",
    val choices: List<String> = emptyList(),
    val artifactName: String = "",
    val installedPath: String = "",
    val applicationUid: String = "",
    val builderVersion: String = "",
    val catalogManifest: String = ""
) {
    val isBusy: Boolean get() = phase in listOf("RESOLVING", "DOWNLOADING", "BUILDING", "INSTALLING", "CHECKING")
    val canRebuild: Boolean get() = sourceProject && sourceSha256.matches(Regex("[a-f0-9]{64}")) &&
        (retainedSource || resolvedUrl.startsWith("https://")) && sdkUrl.isNotBlank() && sdkSha256.matches(Regex("[a-f0-9]{64}"))
}

@Serializable
data class SourcePreferences(val sdkUrl: String = "", val trustedBuilds: Boolean = false)

@Serializable
data class WidgetPreset(val key: FlipperKeyPath?, val type: String)
