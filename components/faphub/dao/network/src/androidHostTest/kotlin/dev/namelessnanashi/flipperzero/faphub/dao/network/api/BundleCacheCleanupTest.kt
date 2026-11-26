package dev.namelessnanashi.flipperzero.faphub.dao.network.api

import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.faphub.dao.network.network.api.FapNetworkBundleApi
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toOkioPath
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BundleCacheCleanupTest {
    @get:Rule val temporary = TemporaryFolder()
    private val target = FlipperTarget.Received("f7", SemVer(87, 1))

    @Test
    fun `completed bundle cleanup preserves other queued downloads`() = runTest {
        val cache = temporary.newFolder()
        val storage = mockk<FlipperStorageProvider> { every { getCacheFolder("fap_bundles") } returns cache.toOkioPath() }
        val completed = File(cache, "done_f7_87.1.fap").apply { writeText("binary") }
        val part = File(cache, completed.name + ".part").apply { writeText("partial") }
        val queued = File(cache, "pending_f7_87.1.fap").apply { writeText("pending") }
        FapDownloadApiImpl(mockk(), storage).discardBundle(target, "done")
        assertFalse(completed.exists())
        assertFalse(part.exists())
        assertEquals("pending", queued.readText())
    }

    @Test
    fun `abandoned cache files expire without deleting fresh or reused bundles`() = runTest {
        val cache = temporary.newFolder()
        val storage = mockk<FlipperStorageProvider> { every { getCacheFolder("fap_bundles") } returns cache.toOkioPath() }
        val network = mockk<FapNetworkBundleApi>()
        val staleTime = System.currentTimeMillis() - 25L * 60 * 60 * 1000
        val stale = File(cache, "old_f7_87.1.fap").apply { writeText("old"); setLastModified(staleTime) }
        val stalePart = File(cache, "abandoned.fap.part").apply { writeText("partial"); setLastModified(staleTime) }
        val fresh = File(cache, "pending_f7_87.1.fap").apply { writeText("pending") }
        val reused = File(cache, "current_f7_87.1.fap").apply { writeText("current"); setLastModified(staleTime) }
        assertEquals(reused, FapDownloadApiImpl(network, storage).downloadBundle(target, "current", null))
        assertFalse(stale.exists())
        assertFalse(stalePart.exists())
        assertTrue(fresh.exists())
        assertEquals("current", reused.readText())
        assertTrue(reused.lastModified() > staleTime)
        coVerify(exactly = 0) { network.downloadBundle(any(), any(), any()) }
    }

    @Test
    fun `cleanup rejects paths outside the bundle cache`() = runTest {
        val cache = temporary.newFolder()
        val outside = File(cache.parentFile, "important_f7_87.1.fap").apply { writeText("keep") }
        val storage = mockk<FlipperStorageProvider> { every { getCacheFolder("fap_bundles") } returns cache.toOkioPath() }
        val result = runCatching { FapDownloadApiImpl(mockk(), storage).discardBundle(target, "../important") }
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("keep", outside.readText())
    }
}
