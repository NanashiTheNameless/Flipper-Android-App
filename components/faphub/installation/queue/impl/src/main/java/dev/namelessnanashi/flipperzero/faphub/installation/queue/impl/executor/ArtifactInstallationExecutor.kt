package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.FRpcInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.awaitRpcInformation
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageNotExistException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.awaitLoadedApps
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapArtifact
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.actions.FapActionUpload
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import dev.zacsweers.metro.Inject
import java.io.File

class ArtifactInstallationExecutor @Inject constructor(
    private val upload: FapActionUpload,
    private val manifests: FapManifestApi,
    private val features: FFeatureProvider,
    private val targets: FlipperTargetProviderApi
) {
    suspend fun install(request: FapActionRequest.InstallArtifact, progress: ProgressListener) {
        val file = File(request.localPath)
        require(FapArtifact.sha256(file) == request.source.sha256) { "App file changed after validation" }
        val artifact = FapArtifact.inspect(file)
        artifact.requireCompatible(request.target)
        FapArtifact.requireSegment(request.category)
        FapArtifact.requireSegment(request.fileName)
        require(request.applicationUid.matches(Regex("external:[a-f0-9]{64}")) ||
            (request.restoredManifest?.uid == request.applicationUid && request.applicationUid.matches(Regex("[A-Za-z0-9_-]{1,100}")))) { "Invalid app identity" }
        request.restoredManifest?.let { FapArtifact.requireSegment(it.applicationAlias) }
        val destination = "/ext/apps/${request.category}/${request.fileName}.fap"
        val current = manifests.awaitLoadedApps()
        require(current.items.none { it.path == destination && it.uid != request.applicationUid }) {
            "This path belongs to another app source. Choose a different filename."
        }
        val existing = current.items.firstOrNull { it.uid == request.applicationUid }
        require(existing == null || existing.path == destination) { "Keep the existing app path when updating" }
        fun validateDevice() {
            require(targets.getFlipperTarget().value == request.target) { "Firmware API changed; review this app again" }
        }
        suspend fun validateIdentity() {
            validateDevice()
            val info = features.getSync<FRpcInfoFeatureApi>()?.awaitRpcInformation()
                ?: error("Device information is unavailable for this Flipper")
            validateDevice()
            require(info.flipperDeviceInfo.serialNumber == request.deviceSerial &&
                info.firmware.softwareRevision == request.firmwareRevision) { "Device or firmware changed; review this app again" }
        }
        validateIdentity()
        val storage = features.getSync<FStorageFeatureApi>() ?: error("Connect your Flipper")
        // An untracked file must also be protected from an accidental replacement.
        if (existing == null) {
            val listing = storage.listingApi().ls("/ext/apps/${request.category}")
            val files = if (listing.exceptionOrNull() is FRpcStorageNotExistException) emptyList() else listing.getOrThrow()
            require(files.none { it.fileName == "${request.fileName}.fap" }) { "An app already exists at this path. Choose a different filename." }
        }
        val staged = upload.upload(file, progress)
        validateIdentity()
        val manifest = request.restoredManifest?.also {
            require(it.path == destination) { "Restored manifest destination does not match" }
        }?.copy(sdkApi = artifact.api, sourceFileHash = null) ?: FapManifestItem(
            applicationAlias = request.applicationUid.replace(':', '_'),
            uid = request.applicationUid,
            versionUid = request.source.sha256,
            path = destination,
            fullName = artifact.name,
            iconBase64 = null,
            sdkApi = artifact.api,
            isDevCatalog = false,
            externalSource = request.source
        )
        manifests.add(staged, manifest)
        val finalMd5 = storage.md5Api().md5(destination).getOrThrow()
        val localMd5 = java.security.MessageDigest.getInstance("MD5").digest(file.readBytes())
            .joinToString("") { "%02x".format(it) }
        check(finalMd5.equals(localMd5, true)) { "Installed app verification failed" }
    }
}
