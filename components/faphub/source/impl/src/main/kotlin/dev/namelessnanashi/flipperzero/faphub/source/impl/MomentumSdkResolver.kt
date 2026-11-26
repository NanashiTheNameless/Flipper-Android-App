package dev.namelessnanashi.flipperzero.faphub.source.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File

internal data class MomentumSdk(val url: String, val sha256: String = "", val version: String)

internal class MomentumSdkResolver(
    private val directory: File,
    private val metadata: suspend (String) -> JsonObject = { SourceHttp.json(it, directory) }
) {
    suspend fun resolve(info: FlipperRpcInformation, target: FlipperTarget.Received): MomentumSdk {
        val fields = (info.allFields + info.otherFields).mapKeys { it.key.replace('_', '.').lowercase() }
        fun field(vararg suffixes: String) = fields.entries.firstOrNull { entry ->
            suffixes.any { entry.key == it || entry.key.endsWith(".$it") }
        }?.value.orEmpty()
        require(field("firmware.commit.dirty").lowercase() !in listOf("true", "1")) {
            "This firmware has local changes. Its exact SDK must be supplied in builder setup."
        }
        val firmware = info.firmware.softwareRevision.orEmpty()
        val tag = listOf(field("firmware.version"), field("firmware.branch.name", "firmware.branch"), firmware)
            .flatMap { Regex("(?<![A-Za-z0-9-])mntm-\\d+(?![A-Za-z0-9-])").findAll(it).map { match -> match.value }.toList() }
            .distinct().singleOrNull()
        val commit = field("firmware.commit.hash", "firmware.commit").ifBlank {
            Regex("(?i)(?<![a-z0-9])[a-f0-9]{7,40}(?![a-z0-9])").find(firmware)?.value.orEmpty()
        }.lowercase().takeIf { it.matches(Regex("[a-f0-9]{7,40}")) }.orEmpty()
        require(tag != null || commit.isNotBlank()) {
            "Cannot identify the Momentum firmware revision ($firmware). Reconnect the Flipper to reload its information."
        }
        val index = metadata(INDEX)
        val versions = index["channels"]?.jsonArray.orEmpty().flatMap { channel ->
            channel.jsonObject["versions"]?.jsonArray.orEmpty().map { it.jsonObject }
        }
        val matching = versions.filter { version ->
            val id = version.string("version")
            if (tag != null) id == tag else id.matches(Regex("(?i)[a-f0-9]{7,40}")) &&
                (id.lowercase().startsWith(commit) || commit.startsWith(id.lowercase()))
        }.flatMap { version ->
            version["files"]?.jsonArray.orEmpty().map { it.jsonObject }
                .filter { it.string("type") == "sdk_zip" && it.string("target") == target.target }
                .map { asset ->
                    val url = asset.string("url")
                    require(SourceHttp.https(url).host == "up.momentum-fw.dev") { "Unexpected Momentum SDK download host" }
                    val hash = asset.string("sha256").lowercase()
                    require(hash.matches(Regex("[a-f0-9]{64}"))) { "Momentum SDK is missing its SHA-256" }
                    MomentumSdk(url, hash, version.string("version"))
                }
        }.distinct()
        if (matching.isNotEmpty()) {
            require(matching.size == 1) { "Multiple SDKs match this firmware. Select an exact SDK in builder setup." }
            return matching.single()
        }
        // The rolling index can drop older stable releases; their tagged assets remain immutable by version.
        if (tag != null) {
            val release = metadata("https://api.github.com/repos/Next-Flip/Momentum-Firmware/releases/tags/$tag")
            require(release.string("tag_name") == tag) { "Momentum release does not match the connected firmware" }
            val asset = release["assets"]?.jsonArray.orEmpty().map { it.jsonObject }.singleOrNull {
                it.string("name").contains("${target.target}-sdk-") && it.string("name").endsWith("-$tag.zip")
            } ?: error("Momentum has no published SDK for $tag. Supply its exact SDK in builder setup.")
            val url = asset.string("browser_download_url")
            require(url.startsWith("https://github.com/Next-Flip/Momentum-Firmware/releases/download/$tag/")) { "Unexpected Momentum release SDK URL" }
            val digest = asset.string("digest").removePrefix("sha256:")
                .takeIf { it.matches(Regex("[a-f0-9]{64}")) }.orEmpty()
            return MomentumSdk(url, digest, tag)
        }
        error("Momentum no longer publishes an SDK for revision $commit (API ${target.sdk}). Supply that revision’s SDK in builder setup; a newer SDK will not be substituted.")
    }

    companion object {
        const val INDEX = "https://up.momentum-fw.dev/firmware/directory.json"
    }
}
