package dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model

import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemVersion
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.ExternalAppSource
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget

sealed class FapActionRequest {
    abstract val applicationUid: String

    data class InstallArtifact(
        override val applicationUid: String,
        val localPath: String,
        val fileName: String,
        val category: String,
        val source: ExternalAppSource,
        val target: FlipperTarget.Received,
        val deviceSerial: String,
        val firmwareRevision: String,
        val restoredManifest: FapManifestItem? = null
    ) : FapActionRequest()

    data class Install(
        val applicationAlias: String,
        override val applicationUid: String,
        val applicationName: String,
        val toVersion: FapItemVersion,
        val categoryAlias: String,
        val iconUrl: String
    ) : FapActionRequest()

    data class Update(
        val from: FapManifestItem,
        val toVersion: FapItemVersion,
        val iconUrl: String,
        val applicationName: String
    ) : FapActionRequest() {
        override val applicationUid = from.uid
    }

    data class Delete(
        override val applicationUid: String
    ) : FapActionRequest()

    data class Cancel(
        override val applicationUid: String
    ) : FapActionRequest()
}
