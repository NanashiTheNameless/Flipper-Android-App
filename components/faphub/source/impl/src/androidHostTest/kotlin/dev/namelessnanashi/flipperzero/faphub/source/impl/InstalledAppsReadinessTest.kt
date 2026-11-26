package dev.namelessnanashi.flipperzero.faphub.source.impl

import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.awaitLoadedApps
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InstalledAppsReadinessTest {
    private val state = MutableStateFlow<FapManifestState>(FapManifestState.Loaded(persistentListOf(), true))
    private val manifests = mockk<FapManifestApi> { every { getManifestFlow() } returns state }

    @Test
    fun `installation waits for the complete list instead of checking partial ownership`() = runTest {
        val result = async { manifests.awaitLoadedApps() }
        runCurrent()
        assertFalse(result.isCompleted)
        state.value = FapManifestState.Loaded(persistentListOf(mockk()), true)
        runCurrent()
        assertFalse(result.isCompleted)
        state.value = FapManifestState.Loaded(persistentListOf(mockk(), mockk()), false)
        assertEquals(2, result.await().items.size)
    }

    @Test
    fun `already loaded apps are reused`() = runTest {
        state.value = FapManifestState.Loaded(persistentListOf(), false)
        assertEquals(state.value, manifests.awaitLoadedApps())
    }

    @Test
    fun `storage failure is reported instead of waiting forever`() = runTest {
        val failure = IllegalStateException("SD card missing")
        state.value = FapManifestState.NotLoaded(failure)
        assertSame(failure, runCatching { manifests.awaitLoadedApps() }.exceptionOrNull()?.cause)
    }

    @Test
    fun `stalled app loading times out with connection guidance`() = runTest {
        assertTrue(runCatching { manifests.awaitLoadedApps(100) }.exceptionOrNull()?.message.orEmpty().contains("Installed apps did not finish loading"))
    }

    @Test
    fun `waiting for installed apps can be cancelled`() = runTest {
        val result = async { manifests.awaitLoadedApps() }
        runCurrent()
        result.cancelAndJoin()
        assertTrue(result.isCancelled)
    }
}
