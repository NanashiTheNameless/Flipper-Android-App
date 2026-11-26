package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.FapHubHideItemApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.WidgetDataApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.core.preference.pb.NewPairSettings
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.awaitLoadedApps
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.ExternalAppSource
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapArtifact
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath
import java.io.File
import java.util.Base64
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Serializable
internal data class BackupFile(val path: FlipperFilePath, val entry: String, val sha256: String)

@Serializable
internal data class BackupKey(val main: BackupFile, val additional: List<BackupFile>, val notes: String?, val deleted: Boolean)

@Serializable
internal data class BackupApp(
    val uid: String, val alias: String, val version: String, val path: String,
    val name: String, val api: String?, val devCatalog: Boolean,
    val binary: String = "", val sha256: String = "", val sourceRecord: String = ""
) {
    fun manifest() = FapManifestItem(alias, uid, version, path, name, null, api?.let(SemVer::fromString), isDevCatalog = devCatalog)
}

@Serializable
internal data class BackupDocument(
    val version: Int = 1, val settings: String, val preferences: SourcePreferences,
    val favorites: List<FlipperKeyPath>, val keys: List<BackupKey>,
    val records: List<SourceRecord>, val apps: List<BackupApp>, val sourceArchives: Map<String, String> = emptyMap(),
    val savedDevices: String = "", val hiddenApps: Set<String> = emptySet(), val widgetPresets: List<WidgetPreset> = emptyList()
)

@SingleIn(AppGraph::class)
class SourceBackup @Inject constructor(
    private val context: Context,
    private val jobs: SourceJobs,
    private val settings: DataStore<Settings>,
    private val keys: SimpleKeyApi,
    private val favorites: FavoriteApi,
    private val manifests: FapManifestApi,
    private val features: FFeatureProvider,
    private val queue: FapInstallationQueueApi,
    private val pairedDevices: DataStore<NewPairSettings>,
    private val hiddenApps: FapHubHideItemApi,
    private val widgets: WidgetDataApi
) {
    private val store get() = jobs.store

    fun export(uri: Uri): String {
        val job = store.create("backup://export")
        jobs.operation(job.id, "DOWNLOADING") {
            val temporary = File(store.directory(job.id), "backup").apply { mkdirs() }
            try {
                val allKeys = keys.getAllKeys(includeDeleted = true)
                val keyRecords = mutableListOf<BackupKey>()
                suspend fun saveKeyFile(file: FlipperFile, entry: String): BackupFile {
                    val output = File(temporary, entry).apply { parentFile?.mkdirs() }
                    file.content.openStream().use { input ->
                        output.outputStream().use { sink -> copyBounded(input, sink, 32L * 1024 * 1024) }
                    }
                    return BackupFile(file.path, entry, FapArtifact.sha256(output))
                }
                allKeys.forEachIndexed { index, key ->
                    keyRecords += BackupKey(
                        saveKeyFile(key.mainFile, "keys/$index/main"),
                        key.additionalFiles.mapIndexed { extra, file -> saveKeyFile(file, "keys/$index/$extra") },
                        key.notes, key.deleted
                    )
                }
                val sourceRecords = store.records.value.filter { it.appId.isNotBlank() }.toMutableList()
                val loaded = manifests.awaitLoadedApps()
                sourceRecords.removeAll { record -> record.tracking == "catalog" && loaded.items.any { it.uid == record.applicationUid } }
                val storage = features.getSync<FStorageFeatureApi>() ?: error("Connect your Flipper to back up installed apps")
                val apps = mutableListOf<BackupApp>()
                for (item in loaded.items) {
                    currentCoroutineContext().ensureActive()
                    var record = sourceRecords.lastOrNull { it.applicationUid == item.uid && it.sha256 == item.externalSource?.sha256 }
                    if (record == null && item.externalSource != null) {
                        record = portableRecord(item)
                        sourceRecords += record
                    }
                    val canRebuild = record?.canRebuild == true
                    val binary = if (canRebuild) "" else "apps/${apps.size}.fap"
                    var hash = record?.sha256.orEmpty()
                    if (binary.isNotBlank()) {
                        val file = File(temporary, binary).apply { parentFile?.mkdirs() }
                        storage.downloadApi().download(item.path, file.absolutePath.toPath()).getOrThrow()
                        FapArtifact.inspect(file)
                        require(file.length() <= FapArtifact.MAX_BYTES)
                        val actual = FapArtifact.sha256(file)
                        require(hash.isEmpty() || hash == actual) { "Installed binary differs from its recorded source: ${item.fullName}" }
                        hash = actual
                    }
                    apps += BackupApp(item.uid, item.applicationAlias, item.versionUid, item.path, item.fullName,
                        item.sdkApi?.toString(), item.isDevCatalog, binary, hash, record?.id.orEmpty())
                }
                // Include local imports that have not been installed yet, if they cannot be rebuilt.
                for (record in sourceRecords.filter { !it.canRebuild && apps.none { app -> app.sourceRecord == it.id } }) {
                    val local = File(store.directory(record.id), "artifact.fap")
                    if (!local.isFile) continue
                    val binary = "apps/${apps.size}.fap"
                    local.copyTo(File(temporary, binary).apply { parentFile?.mkdirs() })
                    apps += BackupApp(record.applicationUid, "", "", record.installedPath, record.name, record.api, false,
                        binary, FapArtifact.sha256(local), record.id)
                }
                val sourceArchives = mutableMapOf<String, String>()
                sourceRecords.filter { it.retainedSource }.forEach { record ->
                    val source = File(store.directory(record.id), "source.zip")
                    require(source.isFile && FapArtifact.sha256(source) == record.sourceSha256) { "Local source archive is missing: ${record.name}" }
                    val entry = "sources/${record.id}.zip"
                    source.copyTo(File(temporary, entry).apply { parentFile?.mkdirs() })
                    sourceArchives[record.id] = entry
                }
                val document = BackupDocument(
                    settings = Base64.getEncoder().encodeToString(Settings.ADAPTER.encode(settings.data.first().copy(uuid = ""))),
                    preferences = store.preferences.value.copy(trustedBuilds = false), favorites = favorites.getFavoritePaths(),
                    keys = keyRecords, records = sourceRecords.map { it.copy(log = "", deviceSerial = "", phase = if (it.isBusy) "INTERRUPTED" else it.phase) }, apps = apps, sourceArchives = sourceArchives,
                    savedDevices = Base64.getEncoder().encodeToString(NewPairSettings.ADAPTER.encode(pairedDevices.data.first())),
                    hiddenApps = hiddenApps.getHiddenItems().first(),
                    widgetPresets = (store.widgetPresets.value + widgets.getAll().map { WidgetPreset(it.flipperKeyPath, it.widgetType.name) }).distinct()
                )
                File(temporary, "backup.json").writeText(store.json.encodeToString(document))
                context.contentResolver.openOutputStream(uri, "wt")?.use { destination ->
                    ZipOutputStream(destination).use { zip ->
                        var total = 0L
                        temporary.walkTopDown().filter { it.isFile }.forEach { file ->
                            currentCoroutineContext().ensureActive()
                            total += file.length()
                            require(total <= 1024L * 1024 * 1024) { "Backup exceeds 1 GiB" }
                            zip.putNextEntry(ZipEntry(file.relativeTo(temporary).invariantSeparatorsPath))
                            file.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                } ?: error("Could not write backup")
                store.update(job.id) { it.copy(phase = "EXPORTED", message = "Backup exported: ${allKeys.size} keys, ${apps.size} apps, settings and favorites") }
            } finally { temporary.deleteRecursively() }
        }
        return job.id
    }

    fun import(uri: Uri, restoreApps: Boolean): String {
        val job = store.create("backup://import")
        jobs.operation(job.id, "DOWNLOADING") {
            val directory = store.directory(job.id)
            val archive = File(directory, "backup.zip")
            val temporary = File(directory, "restore")
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    archive.outputStream().use { copyBounded(input, it, 1024L * 1024 * 1024) }
                } ?: error("Could not read backup")
                SafeSourceZip.extract(archive, temporary, 1024L * 1024 * 1024)
                val metadata = File(temporary, "backup.json")
                require(metadata.isFile && metadata.length() <= 32L * 1024 * 1024) { "Missing or oversized backup metadata" }
                val backup = store.json.decodeFromString<BackupDocument>(metadata.readText())
                require(backup.version == 1) { "Unsupported backup version ${backup.version}" }
                require(backup.settings.length <= 2 * 1024 * 1024 && backup.savedDevices.length <= 2 * 1024 * 1024) { "Oversized settings in backup" }
                val preferences = Settings.ADAPTER.decode(Base64.getDecoder().decode(backup.settings))
                val savedDevices = backup.savedDevices.takeIf { it.isNotEmpty() }?.let { NewPairSettings.ADAPTER.decode(Base64.getDecoder().decode(it)) }
                if (backup.preferences.sdkUrl.isNotBlank()) SourceHttp.https(backup.preferences.sdkUrl)
                savedDevices?.devices?.forEach { device ->
                    require(device.id.length in 1..128 && device.name.length <= 256 && device.name.none(Char::isISOControl)) { "Invalid saved device" }
                    device.flipper_zero_ble?.let { require(it.address.matches(Regex("([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}"))) { "Invalid Bluetooth address" } }
                }
                fun checkedFile(entry: String, hash: String): File {
                    require(entry.isNotEmpty())
                    val file = File(temporary, entry)
                    require(file.canonicalPath.startsWith(temporary.canonicalPath + File.separator) && file.isFile &&
                        hash.matches(Regex("[a-f0-9]{64}")) && FapArtifact.sha256(file) == hash) { "Missing or damaged backup file: $entry" }
                    return file
                }
                fun checkedPath(path: FlipperFilePath) {
                    require(path.pathToKey.length in 1..1024 && path.pathToKey.split('/').none { it == ".." } &&
                        path.pathToKey.none { it == '\\' || it.isISOControl() }) { "Invalid key path in backup" }
                }
                // Validate every byte and recipe before making any local changes.
                backup.keys.forEach { key ->
                    (listOf(key.main) + key.additional).forEach { checkedPath(it.path); checkedFile(it.entry, it.sha256) }
                }
                backup.favorites.forEach { checkedPath(it.path) }
                backup.widgetPresets.forEach { preset ->
                    preset.key?.let { checkedPath(it.path) }
                    require(preset.type in listOf("SIMPLE", "ONE_CLICK")) { "Unknown widget preset" }
                }
                require(backup.hiddenApps.all { it.length in 1..500 && it.none(Char::isISOControl) })
                backup.records.forEach { record ->
                    UUID.fromString(record.id)
                    if (record.inputUrl.startsWith("https://")) SourceHttp.https(record.inputUrl)
                    if (record.resolvedUrl.isNotBlank()) SourceHttp.https(record.resolvedUrl)
                    if (record.sdkUrl.isNotBlank()) SourceHttp.https(record.sdkUrl)
                    require(!record.isBusy) { "Backup contains an unfinished operation" }
                    require(record.project.split('/').none { it == ".." } && !record.project.startsWith('/') && '\\' !in record.project)
                    if (record.retainedSource) checkedFile(backup.sourceArchives[record.id] ?: error("Missing source snapshot"), record.sourceSha256)
                }
                backup.apps.filter { it.binary.isNotBlank() }.forEach { FapArtifact.inspect(checkedFile(it.binary, it.sha256)) }
                if (restoreApps) {
                    val (target, _) = jobs.device()
                    backup.apps.filter { it.binary.isNotBlank() }.forEach { FapArtifact.inspect(checkedFile(it.binary, it.sha256)).requireCompatible(target) }
                    require(backup.records.none { it.canRebuild } || store.preferences.value.trustedBuilds) { "Complete builder setup and enable trusted-source builds before restoring source-built apps" }
                }
                val oldSettings = settings.data.first()
                val oldFavorites = favorites.getFavoritePaths()
                // Keep a local rollback snapshot for preferences if a database import fails.
                try {
                    val restoredKeys = backup.keys.map { key ->
                        fun restore(file: BackupFile) = FlipperFile(file.path, FlipperKeyContent.InternalFile(checkedFile(file.entry, file.sha256).absolutePath))
                        FlipperKey(restore(key.main), key.additional.map(::restore).toImmutableList(), key.notes, synchronized = false, deleted = key.deleted)
                    }
                    settings.updateData { preferences.copy(uuid = it.uuid) }
                    keys.restoreKeys(restoredKeys)
                    favorites.updateFavorites(backup.favorites)
                } catch (error: Exception) {
                    settings.updateData { oldSettings }
                    favorites.updateFavorites(oldFavorites)
                    throw error
                }
                store.savePreferences(backup.preferences.copy(trustedBuilds = store.preferences.value.trustedBuilds))
                savedDevices?.let { restoredDevices ->
                    pairedDevices.updateData { current ->
                        current.copy(devices = (current.devices + restoredDevices.devices.filter { it.flipper_zero_ble != null }).distinctBy { it.id })
                    }
                }
                val previouslyHidden = hiddenApps.getHiddenItems().first()
                (previouslyHidden - backup.hiddenApps).forEach { hiddenApps.unHideItem(it) }
                (backup.hiddenApps - previouslyHidden).forEach { hiddenApps.hideItem(it) }
                store.saveWidgetPresets(backup.widgetPresets)
                val restored = mutableListOf<SourceRecord>()
                for (record in backup.records) {
                    var new = record.copy(id = UUID.randomUUID().toString(), phase = if (record.sourceProject) "SOURCE_READY" else "READY", deviceSerial = "", log = "", updateAvailable = false,
                        message = "Imported backup record. Review or restore to install on the connected Flipper.")
                    val app = backup.apps.lastOrNull { it.sourceRecord == record.id && it.binary.isNotBlank() }
                    if (app != null) {
                        val binary = checkedFile(app.binary, app.sha256)
                        binary.copyTo(File(store.directory(new.id), "artifact.fap"), overwrite = true)
                        new = new.copy(sha256 = app.sha256, api = FapArtifact.inspect(binary).api.toString())
                    }
                    if (record.retainedSource) checkedFile(backup.sourceArchives.getValue(record.id), record.sourceSha256).copyTo(File(store.directory(new.id), "source.zip"), overwrite = true)
                    store.save(new)
                    restored += new
                }
                for (app in backup.apps.filter { it.sourceRecord.isBlank() && it.binary.isNotBlank() }) {
                        val file = checkedFile(app.binary, app.sha256)
                        val path = File(app.path)
                        val record = SourceRecord(
                            id = UUID.randomUUID().toString(), inputUrl = "https://lab.flipper.net/apps/${app.uid}",
                            tracking = "catalog", appId = path.nameWithoutExtension, name = app.name,
                            category = path.parentFile?.name ?: "Tools", api = app.api.orEmpty(), sha256 = app.sha256,
                            phase = "READY", installedPath = app.path, applicationUid = app.uid,
                            catalogManifest = store.json.encodeToString(app), message = "Catalog app retained from backup"
                        )
                        file.copyTo(File(store.directory(record.id), "artifact.fap"))
                        store.save(record)
                        restored += record
                }
                if (restoreApps) {
                    restored.forEach { record ->
                        val fileName = record.installedPath.takeIf { it.isNotBlank() }?.let { File(it).nameWithoutExtension } ?: record.appId
                        if (record.canRebuild) jobs.buildAndInstall(record.id, record.category, fileName)
                        else if (File(store.directory(record.id), "artifact.fap").isFile) jobs.install(record.id, record.category, fileName)
                    }
                }
                store.update(job.id) { it.copy(phase = "IMPORTED", message = "Imported ${backup.keys.size} keys, settings, favorites and ${backup.apps.size} app records. ${if (restoreApps) "App restores are queued." else "Apps can be restored individually."}") }
            } finally { archive.delete(); temporary.deleteRecursively() }
        }
        return job.id
    }

    private fun portableRecord(item: FapManifestItem): SourceRecord {
        val source = requireNotNull(item.externalSource)
        val uri = runCatching { Uri.parse(source.url) }.getOrNull()
        val repository = uri?.takeIf { it.host == "github.com" }?.pathSegments?.take(2)?.joinToString("/").orEmpty()
        val rebuild = source.backend == "termux-ufbt" && repository.isNotBlank() && source.revision.matches(Regex("[a-f0-9]{40}"))
        return SourceRecord(
            id = UUID.randomUUID().toString(), inputUrl = source.url,
            resolvedUrl = if (rebuild) "https://codeload.github.com/$repository/zip/${source.revision}" else source.url,
            repository = if (rebuild) "https://github.com/$repository" else "", revision = source.revision,
            tracking = source.tracking, project = source.project, appId = File(item.path).nameWithoutExtension,
            name = item.fullName, category = File(item.path).parentFile?.name ?: "Tools", sdkUrl = source.sdkUrl,
            sdkSha256 = source.sdkSha256, firmware = source.firmware, api = item.sdkApi?.toString().orEmpty(),
            sha256 = source.sha256, sourceSha256 = source.sourceSha256, backend = source.backend, phase = "INSTALLED",
            sourceProject = rebuild, installedPath = item.path, applicationUid = item.uid
        )
    }

    private suspend fun copyBounded(input: java.io.InputStream, output: java.io.OutputStream, limit: Long) {
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            currentCoroutineContext().ensureActive()
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            require(total <= limit) { "Backup data exceeds size limit" }
            output.write(buffer, 0, count)
        }
    }
}
