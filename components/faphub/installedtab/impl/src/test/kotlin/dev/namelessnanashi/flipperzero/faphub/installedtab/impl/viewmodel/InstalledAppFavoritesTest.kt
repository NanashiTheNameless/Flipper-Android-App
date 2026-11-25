package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledAppFavoritesTest {
    private val path = FlipperKeyPath(FlipperFilePath.fromFavoritePath("/ext/apps/Tools/test.fap"), false)

    @Test
    fun `app stars persist real file paths and can remove favorites`() = runTest {
        withViewModel {
            vm.toggleFavorite("app")
            runCurrent()
            coVerify(exactly = 1) { favorites.setFavorite(path, true) }
            assertEquals(setOf(path), vm.favoritePaths.value)
            vm.toggleFavorite("app")
            runCurrent()
            coVerify(exactly = 1) { favorites.setFavorite(path, false) }
            verify(exactly = 2) { sync.startSynchronization(force = true) }
            assertTrue(vm.favoritePaths.value.isEmpty())
        }
    }

    @Test
    fun `repeated taps while saving cannot toggle the same app twice`() = runTest {
        withViewModel {
            val gate = CompletableDeferred<Unit>()
            coEvery { favorites.setFavorite(path, true) } coAnswers { gate.await() }
            vm.toggleFavorite("app")
            runCurrent()
            vm.toggleFavorite("app")
            assertEquals(setOf("app"), vm.favoriteChanges.value)
            gate.complete(Unit)
            runCurrent()
            coVerify(exactly = 1) { favorites.setFavorite(path, true) }
            assertTrue(vm.favoriteChanges.value.isEmpty())
        }
    }

    @Test
    fun `syncing or missing apps cannot change favorites`() = runTest {
        withViewModel {
            syncState.value = SynchronizationState.InProgress.Default(0.5f)
            vm.toggleFavorite("app")
            runCurrent()
            syncState.value = SynchronizationState.Finished
            vm.toggleFavorite("missing")
            runCurrent()
            coVerify(exactly = 0) { favorites.setFavorite(any(), any()) }
            verify(exactly = 0) { sync.startSynchronization(any()) }
        }
    }

    @Test
    fun `failed favorite changes show an error and allow retry`() = runTest {
        withViewModel {
            coEvery { favorites.setFavorite(path, true) } throws IllegalStateException("storage failed")
            vm.toggleFavorite("app")
            runCurrent()
            assertTrue(vm.favoriteError.value)
            assertTrue(vm.favoriteChanges.value.isEmpty())
            verify(exactly = 0) { sync.startSynchronization(any()) }

            coEvery { favorites.setFavorite(path, true) } returns Unit
            vm.toggleFavorite("app")
            runCurrent()
            assertFalse(vm.favoriteError.value)
            verify(exactly = 1) { sync.startSynchronization(force = true) }
        }
    }

    private class Fixture(
        val vm: InstalledFapsViewModel,
        val favorites: FavoriteApi,
        val sync: SynchronizationApi,
        val syncState: MutableStateFlow<SynchronizationState>
    )

    private suspend fun TestScope.withViewModel(block: suspend Fixture.() -> Unit) {
        mockkObject(FlipperDispatchers)
        var vm: InstalledFapsViewModel? = null
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val syncState = MutableStateFlow<SynchronizationState>(SynchronizationState.Finished)
            val favoritePaths = MutableStateFlow<List<FlipperKeyPath>>(emptyList())
            val favorites = mockk<FavoriteApi> {
                every { getFavoritePathsFlow() } returns favoritePaths
                coEvery { isFavorite(any()) } coAnswers { firstArg<FlipperKeyPath>() in favoritePaths.value }
                coEvery { setFavorite(any(), any()) } coAnswers {
                    val key = firstArg<FlipperKeyPath>()
                    favoritePaths.value = if (secondArg()) favoritePaths.value + key else favoritePaths.value - key
                }
            }
            val sync = mockk<SynchronizationApi>(relaxed = true) {
                every { getSynchronizationState() } returns syncState
            }
            val manifest = mockk<FapManifestApi> {
                every { getManifestFlow() } returns MutableStateFlow(
                    FapManifestState.Loaded(persistentListOf(getTestManifest("app").copy(path = path.path.getPathOnFlipper())), false)
                )
            }
            val producer = mockk<InstalledFapsFromNetworkProducer>(relaxed = true) {
                every { getLoadedFapsFlow() } returns emptyFlow()
            }
            vm = InstalledFapsViewModel(manifest, mockk(relaxed = true), { producer }, favorites, sync)
            runCurrent()
            Fixture(vm, favorites, sync, syncState).block()
        } finally {
            vm?.onDestroy()
            unmockkObject(FlipperDispatchers)
        }
    }
}
