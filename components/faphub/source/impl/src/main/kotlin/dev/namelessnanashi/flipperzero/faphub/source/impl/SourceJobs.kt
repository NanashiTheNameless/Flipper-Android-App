package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.FRpcInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.awaitRpcInformation
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.ExternalAppSource
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapArtifact
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID

@SingleIn(AppGraph::class)
class SourceJobs @Inject constructor(
    private val context: Context,
    val store: SourceStore,
    private val features: FFeatureProvider,
    private val targets: FlipperTargetProviderApi,
    private val queue: FapInstallationQueueApi
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<String, Job>()
    private val builders = mutableMapOf<String, TermuxBuildBroker>()
    private val serial = Semaphore(1)
    val active = MutableStateFlow(0)

    @Synchronized
    internal fun operation(id: String, phase: String, block: suspend () -> Unit) {
        if (jobs[id]?.isActive == true) return
        store.update(id) { it.copy(phase = phase, message = "") }
        active.value += 1
        try {
            ContextCompat.startForegroundService(context, Intent(context, SourceBuildService::class.java))
        } catch (error: Exception) {
            active.value -= 1
            store.update(id) { it.copy(phase = "FAILED", message = error.message.orEmpty()) }
            return
        }
        jobs[id] = scope.launch {
            try {
                serial.withPermit {
                    try { block() } finally {
                        val broker = synchronized(this@SourceJobs) { builders[id] }
                        if (broker?.launched == true && !broker.stopped.isCompleted) {
                            broker.requestCancel()
                            withContext(NonCancellable) { withTimeoutOrNull(10_000) { broker.stopped.await() } }
                        }
                    }
                }
            } catch (timeout: TimeoutCancellationException) {
                store.update(id) { it.copy(phase = "FAILED", message = "Termux worker timed out. Check builder setup and Termux’s background operation settings.") }
            } catch (cancelled: CancellationException) {
                store.update(id) { it.copy(phase = "CANCELLED", message = "Cancelled. Downloaded or built files are retained for retry.") }
                throw cancelled
            } catch (error: Throwable) {
                if (error is VirtualMachineError || error is ThreadDeath || error is LinkageError) throw error
                store.update(id) { it.copy(phase = "FAILED", message = error.message.orEmpty().take(4096)) }
            } finally {
                synchronized(this@SourceJobs) {
                    builders.remove(id)?.close()
                    jobs.remove(id)
                    active.value -= 1
                }
            }
        }
    }

    fun resolve(url: String, project: String = ""): String {
        val record = store.create(url.trim()).copy(project = project).also(store::save)
        startResolution(record, url.trim())
        return record.id
    }

    private fun startResolution(record: SourceRecord, lookupUrl: String) {
        operation(record.id, "RESOLVING") {
            val resolved = SourceResolver(store.directory(record.id)).resolve(lookupUrl)
            store.update(record.id) { it.copy(
                resolvedUrl = resolved.downloadUrl, repository = resolved.repository,
                repositoryId = resolved.repositoryId, revision = resolved.revision,
                tracking = resolved.tracking, sourceProject = resolved.source,
                choices = resolved.choices, artifactName = resolved.filename,
                phase = if (resolved.choices.isEmpty()) "DOWNLOADING" else "CHOOSE"
            ) }
            if (resolved.choices.isEmpty()) acquire(record.id)
        }
    }

    fun checkBuilder(): String {
        val record = store.create("builder://setup-check")
        operation(record.id, "CHECKING") {
            val broker = TermuxBuildBroker(record, store.directory(record.id)) {}
            synchronized(this@SourceJobs) { builders[record.id] = broker }
            TermuxBackend.start(context, record.id, broker.url, broker.token, probe = true)
            broker.launched = true
            withTimeout(30_000) { broker.connected.await() }
            val result = withTimeout(30_000) { broker.capabilities.await() }
            store.update(record.id) { it.copy(phase = "SETUP_READY", message = "Builder setup checked", log = result) }
        }
        return record.id
    }

    fun importFile(uri: Uri): String {
        val record = store.create(uri.toString())
        operation(record.id, "DOWNLOADING") {
            context.contentResolver.openInputStream(uri)?.use { input ->
                File(store.directory(record.id), "download").outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var size = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        size += count
                        require(size <= SourceHttp.MAX_DOWNLOAD) { "File is too large" }
                        output.write(buffer, 0, count)
                    }
                }
            } ?: error("Could not read selected file")
            val name = context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else "app.fap" }.orEmpty()
            store.update(record.id) { it.copy(artifactName = name) }
            inspectDownload(record.id)
        }
        return record.id
    }

    fun choose(id: String, choice: String) = operation(id, "RESOLVING") {
        require(choice in store.get(id).choices) { "Choose a listed artifact or project" }
        if (choice.startsWith("asset:")) {
            val url = choice.removePrefix("asset:")
            SourceHttp.https(url)
            store.update(id) { it.copy(resolvedUrl = url, artifactName = Uri.parse(url).lastPathSegment.orEmpty(), choices = emptyList()) }
            acquire(id)
        } else select(id, choice)
    }

    private suspend fun acquire(id: String) {
        val record = store.get(id)
        SourceHttp.download(record.resolvedUrl, File(store.directory(id), "download"))
        inspectDownload(id)
    }

    private suspend fun inspectDownload(id: String) {
        val directory = store.directory(id)
        val download = File(directory, "download")
        val hash = FapArtifact.sha256(download)
        val previous = store.get(id).sourceSha256
        require(previous.isBlank() || previous == hash) { "Source bytes changed. Inspect the link again to review and pin the new content." }
        store.update(id) { it.copy(sourceSha256 = hash) }
        val zip = download.inputStream().use { it.read() == 0x50 && it.read() == 0x4b }
        if (!zip) {
            val filename = store.get(id).artifactName.substringBeforeLast('.').replace(Regex("[^A-Za-z0-9_-]"), "_").take(80).ifBlank { "app" }
            stageArtifact(id, download)
            prepareArtifact(id, filename)
            return
        }
        val workspace = File(directory, "workspace")
        workspace.deleteRecursively()
        val all = SafeSourceZip.extract(download, workspace)
        val root = workspace.listFiles()?.singleOrNull()?.takeIf { it.isDirectory }?.name.orEmpty()
        val normalized = all.map { value -> value.substringBefore(':') + ":" + value.substringAfter(':').removePrefix("$root/").let { if (it == root) "" else it } }
        val sources = normalized.filter { it.startsWith("project:") }
        val binaries = normalized.filter { it.startsWith("fap:") }
        val choices = if (store.get(id).sourceProject || binaries.isEmpty()) sources else binaries
        require(choices.isNotEmpty()) { "ZIP has no standard application.fam project or FAP binary" }
        store.update(id) { it.copy(archiveRoot = root, choices = choices, phase = "CHOOSE") }
        val preferred = store.get(id).project.takeIf { it.isNotBlank() }?.let { "project:$it" }
        if (preferred != null) {
            require(preferred in choices) { "The requested project is missing from this revision. Inspect the source to select another project." }
            select(id, preferred)
        } else if (choices.size == 1) select(id, choices.single())
    }

    private fun select(id: String, choice: String) {
        val record = store.get(id)
        val base = File(store.directory(id), "workspace")
        val relative = listOf(record.archiveRoot, choice.substringAfter(':')).filter { it.isNotEmpty() && it != "." }.joinToString("/")
        val selected = File(base, relative)
        require(selected.canonicalPath == base.canonicalPath || selected.canonicalPath.startsWith(base.canonicalPath + File.separator)) { "Invalid project path" }
        if (choice.startsWith("project:")) {
            val manifest = File(selected, "application.fam")
            require(manifest.length() in 1..(1024 * 1024)) { "Invalid application manifest" }
            val text = manifest.readText()
            fun literal(name: String) = Regex("\\b$name\\s*=\\s*[\"']([^\"'\\r\\n]+)[\"']").find(text)?.groupValues?.get(1).orEmpty()
            val appId = literal("appid")
            val category = literal("fap_category").ifBlank { "Tools" }
            if (appId.isNotBlank()) FapArtifact.requireSegment(appId)
            FapArtifact.requireSegment(category)
            File(store.directory(id), "download").copyTo(File(store.directory(id), "source.zip"), overwrite = true)
            store.update(id) { it.copy(
                sourceProject = true, retainedSource = it.resolvedUrl.isBlank(),
                project = choice.substringAfter(':').removeSuffix("/.").let { if (it == ".") "" else it },
                selectedEntry = choice, appId = appId, name = literal("name").ifBlank { appId },
                category = if (it.installedPath.isNotBlank()) it.category else category,
                phase = "SOURCE_READY", message = "Source pinned to ${it.revision.ifBlank { "archive SHA-256 ${it.sourceSha256.take(12)}" }}"
            ) }
        } else {
            stageArtifact(id, selected)
            store.update(id) { it.copy(selectedEntry = choice, sourceProject = false) }
            prepareArtifact(id, selected.nameWithoutExtension)
        }
    }

    private fun prepareArtifact(id: String, appId: String) {
        val file = File(store.directory(id), "artifact.fap")
        val metadata = FapArtifact.inspect(file)
        FapArtifact.requireSegment(appId)
        store.update(id) { it.copy(appId = appId, name = metadata.name, appVersion = metadata.version, sha256 = FapArtifact.sha256(file), api = metadata.api.toString(), phase = "READY") }
    }

    private fun stageArtifact(id: String, source: File) {
        FapArtifact.inspect(source)
        val temporary = File(store.directory(id), "artifact.fap.part")
        source.inputStream().use { input -> temporary.outputStream().use { output -> input.copyTo(output); output.fd.sync() } }
        check(temporary.renameTo(File(store.directory(id), "artifact.fap"))) { "Could not finalize app file" }
    }

    internal suspend fun device(): Pair<FlipperTarget.Received, FlipperRpcInformation> {
        val target = targets.getFlipperTarget().value as? FlipperTarget.Received ?: error("Connect your Flipper and wait for its firmware API")
        val feature = (features.get<FRpcInfoFeatureApi>().first() as? FFeatureStatus.Supported)?.featureApi
            ?: error("Device information is unavailable. Check the Flipper connection and try again.")
        val information = feature.awaitRpcInformation()
        val currentFeature = (features.get<FRpcInfoFeatureApi>().first() as? FFeatureStatus.Supported)?.featureApi
        require(targets.getFlipperTarget().value == target && currentFeature === feature) {
            "Flipper connection changed while loading device information. Try again."
        }
        require(!information.flipperDeviceInfo.serialNumber.isNullOrBlank() && !information.firmware.softwareRevision.isNullOrBlank()) { "Device identity and firmware revision are required" }
        return target to information
    }

    fun buildAndInstall(id: String, category: String, filename: String) = operation(id, "BUILDING") {
        if (category.isNotBlank()) FapArtifact.requireSegment(category)
        if (filename.isNotBlank()) FapArtifact.requireSegment(filename)
        require(store.preferences.value.trustedBuilds) { "Enable trusted-source builds in builder setup first" }
        store.update(id) { it.copy(message = "Loading device information…") }
        val (target, info) = device()
        store.update(id) { it.copy(message = "", api = target.sdk.toString()) }
        var record = store.get(id)
        val directory = store.directory(id)
        if (!File(directory, "source.zip").isFile) {
            require(record.resolvedUrl.isNotBlank()) { "Choose the original source archive again" }
            SourceHttp.download(record.resolvedUrl, File(directory, "source.zip"))
            require(FapArtifact.sha256(File(directory, "source.zip")) == record.sourceSha256) { "Source archive changed; inspect it again" }
        }
        val sdkOverride = store.preferences.value.sdkUrl
        val sdk = if (sdkOverride.isBlank()) MomentumSdkResolver(directory).resolve(info, target) else null
        val sdkUrl = sdk?.url ?: sdkOverride
        store.update(id) { it.copy(message = "Downloading SDK for ${sdk?.version ?: info.firmware.softwareRevision}") }
        SourceHttp.download(sdkUrl, File(directory, "sdk.zip"))
        val sdkHash = FapArtifact.sha256(File(directory, "sdk.zip"))
        require(sdk?.sha256.isNullOrBlank() || sdk?.sha256 == sdkHash) { "Momentum SDK download did not match its published SHA-256. Try again." }
        if (record.sdkUrl == sdkUrl && record.sdkSha256.isNotBlank() && record.firmware == info.firmware.softwareRevision) {
            require(record.sdkSha256 == sdkHash) { "SDK bytes changed at the recorded URL. Review a new SDK profile before rebuilding." }
        }
        store.update(id) { it.copy(
            sdkUrl = sdkUrl, sdkSha256 = sdkHash, firmware = info.firmware.softwareRevision.orEmpty(),
            deviceSerial = info.flipperDeviceInfo.serialNumber.orEmpty(), backend = "termux-ufbt",
            log = "SDK: ${sdk?.version ?: "manual profile"} · $sdkUrl\n", message = "", phase = "BUILDING"
        ) }
        record = store.get(id)
        val broker = TermuxBuildBroker(record, directory) { chunk -> store.update(id) { it.copy(log = it.log + chunk) } }
        synchronized(this@SourceJobs) { builders[id] = broker }
        TermuxBackend.start(context, id, broker.url, broker.token)
        broker.launched = true
        withTimeout(30_000) { broker.connected.await() }
        val result = withTimeout(45 * 60_000L) { broker.result.await() }
        prepareArtifact(id, result.appId)
        store.update(id) { it.copy(builderVersion = result.builderVersion, toolchainVersion = result.toolchainVersion) }
        FapArtifact.inspect(File(directory, "artifact.fap")).requireCompatible(target)
        installNow(id, category.ifBlank { result.category }, filename.ifBlank { result.appId })
    }

    fun install(id: String, category: String, filename: String) = operation(id, "INSTALLING") { installNow(id, category, filename) }

    private suspend fun installNow(id: String, category: String, filename: String) {
        val record = store.get(id)
        FapArtifact.requireSegment(category)
        FapArtifact.requireSegment(filename)
        store.update(id) { it.copy(message = "Loading device information…") }
        val (target, info) = device()
        store.update(id) { it.copy(message = "") }
        if (record.sourceProject && record.deviceSerial.isNotBlank()) {
            require(record.deviceSerial == info.flipperDeviceInfo.serialNumber && record.firmware == info.firmware.softwareRevision) { "Device or firmware changed during build. Rebuild for the current device." }
        }
        val artifact = File(store.directory(id), "artifact.fap")
        FapArtifact.inspect(artifact).requireCompatible(target)
        val identity = (record.repositoryId.ifBlank { record.repository.ifBlank { record.inputUrl } }) + "|" + record.project + "|" + record.appId
        val restoredManifest = record.catalogManifest.takeIf { it.isNotBlank() }?.let { store.json.decodeFromString<BackupApp>(it).manifest() }
        val uid = restoredManifest?.uid ?: record.applicationUid.ifBlank { "external:" + MessageDigest.getInstance("SHA-256").digest(identity.toByteArray()).joinToString("") { "%02x".format(it) } }
        store.update(id) { it.copy(phase = "INSTALLING", category = category, artifactName = filename, applicationUid = uid) }
        queue.installArtifact(FapActionRequest.InstallArtifact(
            applicationUid = uid, localPath = artifact.absolutePath, fileName = filename, category = category,
            source = ExternalAppSource(record.inputUrl, record.revision, record.tracking, record.sha256,
                record.project, record.sdkUrl, record.sdkSha256, info.firmware.softwareRevision.orEmpty(), record.backend, record.sourceSha256),
            target = target, deviceSerial = info.flipperDeviceInfo.serialNumber.orEmpty(), firmwareRevision = info.firmware.softwareRevision.orEmpty(), restoredManifest = restoredManifest
        ))
        store.update(id) { it.copy(phase = "INSTALLED", installedPath = "/ext/apps/$category/$filename.fap", updateAvailable = false, message = "Installed and verified") }
        store.cleanSuccess(id)
    }

    fun checkUpdates(id: String) = operation(id, "CHECKING") {
        val old = store.get(id)
        require(old.tracking != "file" || old.inputUrl.startsWith("https://")) { "Local files have no update source. Import a newer file." }
        val resolved = SourceResolver(store.directory(id)).resolve(trackedSourceUrl(old))
        val available = if (old.sourceProject && resolved.source) resolved.revision != old.revision else {
            val url = if (resolved.choices.isNotEmpty()) {
                resolved.choices.singleOrNull { it.substringAfterLast('/') == old.resolvedUrl.substringAfterLast('/') }?.removePrefix("asset:")
                    ?: resolved.choices.singleOrNull()?.removePrefix("asset:")
                    ?: error("Release assets changed. Inspect the source link to choose the new app.")
            } else resolved.downloadUrl
            val temporary = File(store.directory(id), "update-check")
            try {
                SourceHttp.download(url, temporary)
                FapArtifact.sha256(temporary) != old.sourceSha256.ifBlank { old.sha256 }
            } finally { temporary.delete() }
        }
        store.update(id) { it.copy(phase = if (it.installedPath.isNotBlank()) "INSTALLED" else "READY", updateAvailable = available,
            message = if (available) "Update available. Review before installing." else if (it.tracking.startsWith("tag:")) "Pinned tag is unchanged" else "Up to date") }
    }

    fun prepareUpdate(id: String): String {
        val old = store.get(id)
        val record = store.create(old.inputUrl).copy(applicationUid = old.applicationUid, category = old.category,
            installedPath = old.installedPath, project = old.project).also(store::save)
        startResolution(record, trackedSourceUrl(old))
        return record.id
    }

    fun retry(id: String) = operation(id, "RESOLVING") {
        val record = store.get(id)
        if (record.resolvedUrl.isNotBlank()) acquire(id)
        else error("Inspect the original link or choose the file again")
    }

    @Synchronized
    fun cancel(id: String) { builders[id]?.requestCancel(); jobs[id]?.cancel() }

    @Synchronized
    fun cancelAll() { builders.values.toList().forEach { it.requestCancel() }; jobs.values.toList().forEach { it.cancel() } }

    @Synchronized
    fun termuxResult(id: String, error: String?) {
        val broker = builders[id] ?: return
        broker.stopped.complete(Unit)
        val failure = IllegalStateException(error ?: "Builder exited without returning an app")
        if (!broker.connected.isCompleted) broker.connected.completeExceptionally(failure)
        if (!broker.capabilities.isCompleted) broker.capabilities.completeExceptionally(failure)
        if (!broker.result.isCompleted) broker.result.completeExceptionally(failure)
    }
}

internal fun trackedSourceUrl(record: SourceRecord): String {
    if (record.repository.isNotBlank() && (record.tracking.startsWith("branch:") || record.tracking.startsWith("tag:"))) {
        val type = if (record.tracking.startsWith("branch:")) "heads" else "tags"
        val ref = java.net.URLEncoder.encode(record.tracking.substringAfter(':'), "UTF-8").replace("+", "%20")
        return "${record.repository}/archive/refs/$type/$ref.zip"
    }
    return record.inputUrl
}
