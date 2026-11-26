package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.content.Context
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapArtifact
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal fun fapFixture(): ByteArray {
    val bytes = ByteArray(320)
    val data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    byteArrayOf(0x7f, 0x45, 0x4c, 0x46, 1, 1, 1).copyInto(bytes)
    data.putShort(16, 2); data.putShort(18, 40)
    data.putInt(32, 192); data.putShort(46, 40); data.putShort(48, 3); data.putShort(50, 1)
    "\u0000.shstrtab\u0000.fapmeta\u0000".toByteArray().copyInto(bytes, 140)
    data.putInt(232, 1); data.putInt(236, 3); data.putInt(248, 140); data.putInt(252, 20)
    data.putInt(272, 11); data.putInt(276, 1); data.putInt(288, 52); data.putInt(292, 85)
    data.putInt(52, 0x52474448); data.putInt(56, 1)
    data.putShort(60, 2); data.putShort(62, 88); data.putShort(64, 7)
    data.putInt(68, 0x00020001)
    "Test app".toByteArray().copyInto(bytes, 72)
    return bytes
}

class SourceCoreTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun `all three TagTinker URLs pin the requested revision without confusing tags and branches`() = runTest {
        val requested = mutableListOf<String>()
        val resolver = SourceResolver(temporary.root) { url ->
            requested += url
            Json.parseToJsonElement(if ("/commits/" in url) {
                val tag = URLDecoder.decode(url.substringAfter("/commits/"), "UTF-8").startsWith("refs/tags/")
                "{\"sha\":\"${(if (tag) "b" else "a").repeat(40)}\"}"
            } else "{\"default_branch\":\"main\",\"id\":123}").jsonObject
        }
        val root = resolver.resolve("https://github.com/i12bp8/TagTinker")
        val main = resolver.resolve("https://github.com/i12bp8/TagTinker/archive/refs/heads/main.zip")
        val tag = resolver.resolve("https://github.com/i12bp8/TagTinker/archive/refs/tags/v2.0.zip")
        assertEquals(root.downloadUrl, main.downloadUrl)
        assertEquals("branch:main", root.tracking)
        assertEquals("tag:v2.0", tag.tracking)
        assertNotEquals(root.revision, tag.revision)
        assertEquals("123", root.repositoryId)
        assertTrue(requested.any { "refs%2Ftags%2Fv2.0" in it })
    }

    @Test
    fun `repository default branch is discovered and slash refs stay intact`() = runTest {
        val requested = mutableListOf<String>()
        val resolver = SourceResolver(temporary.root) { url ->
            requested += url
            Json.parseToJsonElement(if ("/commits/" in url) "{\"sha\":\"${"a".repeat(40)}\"}" else "{\"default_branch\":\"trunk\",\"id\":42}").jsonObject
        }
        assertEquals("branch:trunk", resolver.resolve("https://github.com/user/repo").tracking)
        assertEquals("branch:feature/app", resolver.resolve("https://github.com/user/repo/archive/refs/heads/feature/app.zip").tracking)
        assertTrue(requested.any { it.endsWith("refs%2Fheads%2Ffeature%2Fapp") })
        val tracked = SourceRecord(UUID.randomUUID().toString(), "https://github.com/user/repo", repository = "https://github.com/user/repo", tracking = "branch:feature/app")
        assertEquals("branch:feature/app", resolver.resolve(trackedSourceUrl(tracked)).tracking)
    }

    @Test
    fun `release pages resolve downloadable assets rather than HTML`() = runTest {
        val resolver = SourceResolver(temporary.root) {
            Json.parseToJsonElement("""{"tag_name":"v1","assets":[{"name":"app.fap","browser_download_url":"https://github.com/u/r/releases/download/v1/app.fap"},{"name":"readme.txt","browser_download_url":"https://github.com/u/r/releases/download/v1/readme.txt"}]}""").jsonObject
        }
        val release = resolver.resolve("https://github.com/u/r/releases/latest")
        assertEquals("release:latest", release.tracking)
        assertEquals(listOf("asset:https://github.com/u/r/releases/download/v1/app.fap"), release.choices)
    }

    @Test
    fun `protocol links round trip encoded branch paths and never start commands`() {
        val record = SourceRecord(UUID.randomUUID().toString(), "https://github.com/u/r/archive/refs/heads/feature/app.zip", project = "apps/my app")
        assertEquals(record.inputUrl to record.project, SourceLinks.parse(SourceLinks.create(record)))
        listOf("flipper-nanashi://app?url=file%3A%2F%2Fetc%2Fpasswd", "flipper-nanashi://app?url=https%3A%2F%2Fx.com&project=..%2Fx", "flipper-nanashi://app?url=https%3A%2F%2Fx.com&url=https%3A%2F%2Fy.com").forEach {
            assertThrows(IllegalArgumentException::class.java) { SourceLinks.parse(it) }
        }
    }

    @Test
    fun `source ZIP discovery keeps its generated wrapper and rejects traversal before writing outside`() = runTest {
        val zip = archive("generated-hash/application.fam" to "App()", "generated-hash/shared/header.h" to "header")
        assertEquals(listOf("project:generated-hash"), SafeSourceZip.extract(zip, File(temporary.root, "source")))
        val unsafe = archive("../outside.txt" to "bad")
        assertThrows(IllegalArgumentException::class.java) { runBlocking { SafeSourceZip.extract(unsafe, File(temporary.root, "unsafe")) } }
        assertFalse(File(temporary.root, "outside.txt").exists())
    }

    @Test
    fun `expanded ZIP size is enforced while reading compressed entries`() {
        val zip = archive("large.txt" to "x".repeat(1024))
        assertThrows(IllegalArgumentException::class.java) { runBlocking { SafeSourceZip.extract(zip, File(temporary.root, "bounded"), 32) } }
    }

    @Test
    fun `FAP metadata and API are checked with bounded section tables`() {
        val file = File(temporary.root, "app.fap").apply { writeBytes(fapFixture()) }
        val artifact = FapArtifact.inspect(file)
        assertEquals("Test app", artifact.name)
        assertEquals(0x00020001L, artifact.version)
        artifact.requireCompatible(FlipperTarget.Received("f7", SemVer(88, 3)))
        assertThrows(IllegalArgumentException::class.java) { artifact.requireCompatible(FlipperTarget.Received("f7", SemVer(87, 99))) }
        assertThrows(IllegalArgumentException::class.java) { artifact.requireCompatible(FlipperTarget.Received("f7", SemVer(88, 1))) }
        val corrupted = fapFixture().also { ByteBuffer.wrap(it).order(ByteOrder.LITTLE_ENDIAN).putInt(32, Int.MAX_VALUE) }
        file.writeBytes(corrupted)
        assertThrows(IllegalArgumentException::class.java) { FapArtifact.inspect(file) }
    }

    @Test
    fun `cleanup preserves non-rebuildable binaries and local sources but deletes build intermediates`() {
        val context = mockk<Context> { every { filesDir } returns temporary.root }
        val store = SourceStore(context)
        val local = store.create("content://app").copy(appId = "app")
        store.save(local)
        File(store.directory(local.id), "artifact.fap").writeBytes(fapFixture())
        File(store.directory(local.id), "workspace").mkdirs()
        store.cleanSuccess(local.id)
        assertTrue(File(store.directory(local.id), "artifact.fap").exists())
        assertFalse(File(store.directory(local.id), "workspace").exists())

        val source = local.copy(id = UUID.randomUUID().toString(), sourceProject = true, retainedSource = true,
            sdkUrl = "https://example.com/sdk.zip", sdkSha256 = "a".repeat(64), sourceSha256 = "b".repeat(64))
        store.save(source)
        listOf("source.zip", "sdk.zip", "artifact.fap", "download").forEach { File(store.directory(source.id), it).writeText("test") }
        store.cleanSuccess(source.id)
        assertEquals(listOf("source.zip"), store.directory(source.id).listFiles()!!.map { it.name })
        assertEquals(source, SourceStore(context).get(source.id).copy(updatedAt = source.updatedAt))
    }

    @Test
    fun `Termux broker rejects missing capabilities and receives a validated binary without intent limits`() = runTest {
        val directory = temporary.newFolder()
        val record = SourceRecord(UUID.randomUUID().toString(), "https://github.com/u/r", sourceSha256 = "a".repeat(64), sdkSha256 = "b".repeat(64))
        TermuxBuildBroker(record, directory) {}.use { broker ->
            fun connect(path: String, token: String? = null) = URI(broker.url + path).toURL().openConnection().let { it as HttpURLConnection }.apply {
                connectTimeout = 2000; readTimeout = 2000
                token?.let { setRequestProperty("Authorization", "Bearer $it") }
            }
            val denied = connect("/recipe")
            assertEquals(403, denied.responseCode); denied.disconnect()
            val recipe = connect("/recipe", broker.token)
            assertTrue(recipe.inputStream.bufferedReader().use { it.readText() }.contains("\"protocol\":1")); recipe.disconnect()
            val upload = connect("/artifact", broker.token).apply {
                requestMethod = "POST"; doOutput = true
                setRequestProperty("X-App-Id", "app"); setRequestProperty("X-App-Category", "Tools")
                setRequestProperty("X-Builder-Version", "0.2.6")
                setFixedLengthStreamingMode(fapFixture().size)
            }
            upload.outputStream.use { it.write(fapFixture()) }
            assertEquals(200, upload.responseCode); upload.disconnect()
            assertEquals("app", broker.result.await().appId)
            assertArrayEquals(fapFixture(), File(directory, "artifact.fap").readBytes())
        }
    }

    private fun archive(vararg entries: Pair<String, String>): File = temporary.newFile().also { file ->
        ZipOutputStream(file.outputStream()).use { zip -> entries.forEach { (name, text) ->
            zip.putNextEntry(ZipEntry(name)); zip.write(text.toByteArray()); zip.closeEntry()
        } }
    }
}
