package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.FapHubHideItemApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.WidgetDataApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.*
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.core.preference.pb.NewPairSettings
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.ExternalAppSource
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapArtifact
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import io.mockk.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SourceBackupTest {
    @get:Rule val temporary = TemporaryFolder()

    private class MemoryStore<T>(initial: T) : DataStore<T> {
        override val data = MutableStateFlow(initial)
        override suspend fun updateData(transform: suspend (T) -> T): T = transform(data.value).also { data.value = it }
    }

    @Test
    fun `backup round trip preserves user data and includes only binaries without rebuild recipes`() = runBlocking {
        val output = temporary.newFile("backup.zip")
        val uri = Uri.parse("content://backup/export")
        val resolver = mockk<ContentResolver> {
            every { openOutputStream(uri, "wt") } answers { output.outputStream() }
            every { openInputStream(uri) } answers { output.inputStream() }
        }
        val context = mockk<Context> {
            every { packageName } returns "test"
            every { filesDir } returns temporary.root
            every { contentResolver } returns resolver
            every { startForegroundService(any()) } returns ComponentName("test", "test.Service")
        }
        val store = SourceStore(context)
        val source = SourceRecord(
            UUID.randomUUID().toString(), "https://github.com/u/r", resolvedUrl = "https://codeload.github.com/u/r/zip/${"a".repeat(40)}",
            repository = "https://github.com/u/r", revision = "a".repeat(40), tracking = "branch:main",
            appId = "built", name = "Built app", sourceProject = true, sdkUrl = "https://example.com/sdk.zip",
            sdkSha256 = "b".repeat(64), sourceSha256 = "c".repeat(64), sha256 = "d".repeat(64),
            applicationUid = "external:" + "e".repeat(64), phase = "INSTALLED", installedPath = "/ext/apps/Tools/built.fap"
        )
        store.save(source)
        val local = store.create("content://local").copy(appId = "local", name = "Local app", phase = "READY")
        store.save(local)
        File(store.directory(local.id), "artifact.fap").writeBytes(fapFixture())
        val localSource = store.create("content://source").copy(
            appId = "local_source", sourceProject = true, retainedSource = true, phase = "SOURCE_READY",
            sdkUrl = "https://example.com/sdk.zip", sdkSha256 = "b".repeat(64)
        )
        File(store.directory(localSource.id), "source.zip").writeText("pinned local source archive")
        store.save(localSource.copy(sourceSha256 = FapArtifact.sha256(File(store.directory(localSource.id), "source.zip"))))
        val settings = MemoryStore(Settings(expert_mode = true, uuid = "local-install-id"))
        val pairSettings = MemoryStore(NewPairSettings())
        val key = FlipperKey(
            FlipperFile(FlipperFilePath("nfc", "key.nfc"), FlipperKeyContent.RawData("key bytes".toByteArray())),
            notes = "important notes", synchronized = true, deleted = false
        )
        var restored = emptyList<FlipperKey>()
        val keys = mockk<SimpleKeyApi> {
            coEvery { getAllKeys(true) } returns listOf(key)
            coEvery { restoreKeys(any()) } coAnswers {
                // The real Room converter streams these files to durable key storage during the transaction.
                restored = firstArg<List<FlipperKey>>().map { imported -> imported.copy(mainFile = imported.mainFile.copy(
                    content = FlipperKeyContent.RawData(imported.keyContent.openStream().readBytes())
                )) }
            }
        }
        val favoritePaths = MutableStateFlow(listOf(key.getKeyPath(), FlipperKeyPath(FlipperFilePath.fromFavoritePath("/app:setting/NFC"), false)))
        val favorites = mockk<FavoriteApi> {
            coEvery { getFavoritePaths() } answers { favoritePaths.value }
            coEvery { updateFavorites(any()) } coAnswers { firstArg<List<FlipperKeyPath>>().also { favoritePaths.value = it } }
        }
        val catalog = FapManifestItem("catalog", "catalog-id", "version-id", "/ext/apps/Tools/catalog.fap", "Catalog", null, SemVer(88, 2), isDevCatalog = false)
        val built = FapManifestItem("external_built", source.applicationUid, source.sha256, source.installedPath, source.name, null, SemVer(88, 2), isDevCatalog = false,
            externalSource = ExternalAppSource(source.inputUrl, source.revision, source.tracking, source.sha256))
        val manifests = mockk<FapManifestApi> { every { getManifestFlow() } returns MutableStateFlow<FapManifestState>(FapManifestState.Loaded(persistentListOf(built, catalog), false)) }
        val storage = mockk<FStorageFeatureApi> {
            every { downloadApi() } returns mockk {
                coEvery { download(any(), any(), any(), any()) } coAnswers {
                    File(secondArg<okio.Path>().toString()).writeBytes(fapFixture())
                    Result.success(Unit)
                }
            }
        }
        val features = mockk<FFeatureProvider> { coEvery { getSync(FStorageFeatureApi::class) } returns storage }
        val queue = mockk<FapInstallationQueueApi>(relaxed = true)
        val hidden = MutableStateFlow(setOf("hidden-app"))
        val hiddenApi = mockk<FapHubHideItemApi> {
            every { getHiddenItems() } returns hidden
            coEvery { hideItem(any()) } coAnswers { hidden.value = hidden.value + firstArg<String>() }
            coEvery { unHideItem(any()) } coAnswers { hidden.value = hidden.value - firstArg<String>() }
        }
        val widgets = mockk<WidgetDataApi> { coEvery { getAll() } returns listOf(WidgetData(42, key.getKeyPath(), WidgetType.SIMPLE)) }
        val jobs = SourceJobs(context, store, features, mockk<FlipperTargetProviderApi>(), queue)
        val backup = SourceBackup(context, jobs, settings, keys, favorites, manifests, features, queue, pairSettings, hiddenApi, widgets)
        suspend fun await(id: String, phase: String) {
            val completed = withTimeout(15_000) { store.records.first { list -> list.any { it.id == id && it.phase in listOf(phase, "FAILED") } }.first { it.id == id } }
            assertEquals(completed.message, phase, completed.phase)
        }
        await(backup.export(uri), "EXPORTED")
        ZipFile(output).use { zip ->
            val document = store.json.decodeFromString<BackupDocument>(zip.getInputStream(zip.getEntry("backup.json")).bufferedReader().readText())
            assertEquals(2, zip.entries().asSequence().count { it.name.endsWith(".fap") })
            assertEquals("important notes", document.keys.single().notes)
            assertEquals(setOf("hidden-app"), document.hiddenApps)
            assertEquals(1, document.widgetPresets.size)
            assertEquals(1, document.sourceArchives.size)
            assertTrue(document.apps.first { it.uid == source.applicationUid }.binary.isEmpty())
        }
        settings.updateData { it.copy(expert_mode = false) }
        favoritePaths.value = emptyList()
        hidden.value = emptySet()
        await(backup.import(uri, restoreApps = false), "IMPORTED")
        assertTrue(settings.data.value.expert_mode)
        assertEquals("local-install-id", settings.data.value.uuid)
        assertEquals("important notes", restored.single().notes)
        assertEquals("key bytes", restored.single().keyContent.openStream().readBytes().decodeToString())
        assertEquals(2, favoritePaths.value.size)
        assertEquals(setOf("hidden-app"), hidden.value)
        assertEquals(1, store.widgetPresets.value.size)
        val restoredSource = store.records.value.last { it.applicationUid == source.applicationUid }
        assertEquals(source.revision, restoredSource.revision)
        assertFalse(File(store.directory(restoredSource.id), "artifact.fap").exists())
        assertTrue(store.records.value.any { it.tracking == "catalog" && File(store.directory(it.id), "artifact.fap").isFile })
        coVerify(exactly = 0) { queue.installArtifact(any()) }
        assertTrue(store.root.walkTopDown().none { it.name in listOf("backup.zip", "restore", "backup") })
    }
}
