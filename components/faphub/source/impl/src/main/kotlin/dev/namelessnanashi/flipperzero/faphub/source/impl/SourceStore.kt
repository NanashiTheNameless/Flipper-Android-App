package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.content.Context
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@SingleIn(AppGraph::class)
class SourceStore @Inject constructor(context: Context) {
    val root = File(context.filesDir, "app_sources").apply { mkdirs() }
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val recordsFile = File(root, "records.json")
    val records = MutableStateFlow(runCatching {
        json.decodeFromString<List<SourceRecord>>(recordsFile.readText())
    }.getOrDefault(emptyList()).map {
        if (it.isBusy) it.copy(phase = "INTERRUPTED", message = "Interrupted. Retry to resume safely.") else it
    })
    val preferences = MutableStateFlow(runCatching {
        json.decodeFromString<SourcePreferences>(File(root, "preferences.json").readText())
    }.getOrDefault(SourcePreferences()))
    val widgetPresets = MutableStateFlow(runCatching {
        json.decodeFromString<List<WidgetPreset>>(File(root, "widget_presets.json").readText())
    }.getOrDefault(emptyList()))

    init {
        // Recover cleanup if Android stopped the app just after a verified install.
        records.value.filter { it.phase == "INSTALLED" }.forEach { record ->
            runCatching { cleanSuccess(record.id) }
        }
    }

    fun saveWidgetPresets(presets: List<WidgetPreset>) {
        atomicWrite(File(root, "widget_presets.json"), json.encodeToString(presets))
        widgetPresets.value = presets
    }

    fun directory(id: String): File {
        require(id.matches(Regex("[a-f0-9-]{36}"))) { "Invalid job identity" }
        return File(root, id).apply { mkdirs() }
    }

    @Synchronized
    fun save(record: SourceRecord) {
        val updated = record.copy(updatedAt = System.currentTimeMillis(), log = record.log.takeLast(64 * 1024))
        val all = records.value.filterNot { it.id == record.id } + updated
        atomicWrite(recordsFile, json.encodeToString(all))
        records.value = all
    }

    @Synchronized
    fun update(id: String, transform: (SourceRecord) -> SourceRecord) {
        save(transform(get(id)))
    }

    fun get(id: String) = records.value.first { it.id == id }

    fun create(url: String): SourceRecord = SourceRecord(UUID.randomUUID().toString(), url).also(::save)

    @Synchronized
    fun savePreferences(value: SourcePreferences) {
        atomicWrite(File(root, "preferences.json"), json.encodeToString(value))
        preferences.value = value
    }

    fun clearTemporaryFiles(id: String) {
        directory(id).listFiles()?.filter { it.name != "artifact.fap" }?.forEach { it.deleteRecursively() }
    }

    fun cleanSuccess(id: String) {
        val record = get(id)
        directory(id).listFiles()?.forEach {
            val keepSource = record.retainedSource && it.name == "source.zip"
            val keepBinary = !record.canRebuild && it.name == "artifact.fap"
            if (!keepSource && !keepBinary) it.deleteRecursively()
        }
    }

    companion object {
        fun atomicWrite(file: File, value: String) {
            val temporary = File(file.parentFile, "${file.name}.part")
            temporary.outputStream().use { output -> output.write(value.toByteArray()); output.fd.sync() }
            check(temporary.renameTo(file)) { "Could not save ${file.name}" }
        }
    }
}
