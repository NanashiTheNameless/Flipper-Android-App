package dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.core.ktx.jre.md5
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.ExternalAppSource
import java.util.Base64
import java.io.File
import java.nio.charset.Charset
import dev.zacsweers.metro.Inject

private const val FAP_MANIFEST_FILETYPE_KEY = "Filetype"
private const val FAP_MANIFEST_FILETYPE_VALUE = "Flipper Application Installation Manifest"
private const val FAP_MANIFEST_VERSION_KEY = "Version"
private const val FAP_MANIFEST_VERSION_VALUE = "1"
private const val FAP_MANIFEST_UID_KEY = "UID"
private const val FAP_MANIFEST_VERSION_UID_KEY = "Version UID"
private const val FAP_MANIFEST_PATH_KEY = "Path"
private const val FAP_MANIFEST_FULL_NAME_KEY = "Full Name"
private const val FAP_MANIFEST_ICON_KEY = "Icon"
private const val FAP_MANIFEST_API_KEY = "Version Build API"
private const val FAP_MANIFEST_DEV_CATALOG = "DevCatalog"

class FapManifestParser @Inject constructor() {
    fun parse(fileContent: ByteArray, name: String): FapManifestItem? {
        val fff = FlipperFileFormat.fromFileContent(fileContent.toString(Charset.forName("UTF-8")))
        val applicationAlias = File(name).nameWithoutExtension

        val dict = fff.orderedDict.toMap()
        return FapManifestItem(
            applicationAlias = applicationAlias,
            uid = dict[FAP_MANIFEST_UID_KEY] ?: return null,
            versionUid = dict[FAP_MANIFEST_VERSION_UID_KEY] ?: return null,
            path = dict[FAP_MANIFEST_PATH_KEY] ?: return null,
            fullName = dict[FAP_MANIFEST_FULL_NAME_KEY] ?: applicationAlias,
            iconBase64 = dict[FAP_MANIFEST_ICON_KEY],
            sdkApi = dict[FAP_MANIFEST_API_KEY]?.let { SemVer.fromString(it) },
            sourceFileHash = fileContent.md5(),
            isDevCatalog = dict[FAP_MANIFEST_DEV_CATALOG]?.toBooleanStrictOrNull() ?: false,
            externalSource = dict["Nanashi Source URL"]?.let { encoded ->
                fun field(key: String): String = dict["Nanashi $key"]?.let {
                    String(Base64.getDecoder().decode(it), Charsets.UTF_8)
                }.orEmpty()
                ExternalAppSource(
                    url = String(Base64.getDecoder().decode(encoded), Charsets.UTF_8),
                    revision = field("Revision"), tracking = field("Tracking"), sha256 = field("SHA256"),
                    project = field("Project"), sdkUrl = field("SDK URL"), sdkSha256 = field("SDK SHA256"),
                    firmware = field("Firmware"), backend = field("Backend"), sourceSha256 = field("Source SHA256")
                )
            }
        )
    }

    fun encode(fapItem: FapManifestItem): FlipperFileFormat {
        val orderedDict = mutableListOf<Pair<String, String>>()
        orderedDict.add(FAP_MANIFEST_FILETYPE_KEY to FAP_MANIFEST_FILETYPE_VALUE)
        orderedDict.add(FAP_MANIFEST_VERSION_KEY to FAP_MANIFEST_VERSION_VALUE)
        orderedDict.add(FAP_MANIFEST_FULL_NAME_KEY to fapItem.fullName)

        fapItem.iconBase64?.let { icon ->
            orderedDict.add(FAP_MANIFEST_ICON_KEY to icon)
        }

        fapItem.sdkApi?.let {
            orderedDict.add(FAP_MANIFEST_API_KEY to it.toString())
        }

        orderedDict.add(FAP_MANIFEST_UID_KEY to fapItem.uid)
        orderedDict.add(FAP_MANIFEST_VERSION_UID_KEY to fapItem.versionUid)
        orderedDict.add(FAP_MANIFEST_PATH_KEY to fapItem.path)
        if (fapItem.isDevCatalog) {
            orderedDict.add(FAP_MANIFEST_DEV_CATALOG to fapItem.isDevCatalog.toString())
        }
        fapItem.externalSource?.let { source ->
            listOf(
                "Source URL" to source.url, "Revision" to source.revision,
                "Tracking" to source.tracking, "SHA256" to source.sha256,
                "Project" to source.project, "SDK URL" to source.sdkUrl,
                "SDK SHA256" to source.sdkSha256, "Firmware" to source.firmware,
                "Backend" to source.backend, "Source SHA256" to source.sourceSha256
            ).forEach { (key, value) ->
                orderedDict.add("Nanashi $key" to Base64.getEncoder().encodeToString(value.toByteArray(Charsets.UTF_8)))
            }
        }

        return FlipperFileFormat(orderedDict)
    }
}
