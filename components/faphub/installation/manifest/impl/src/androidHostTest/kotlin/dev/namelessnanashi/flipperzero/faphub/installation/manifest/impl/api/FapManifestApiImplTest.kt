package dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.api

import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.model.FapManifestLoaderState
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils.FapManifestDeleter
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils.FapManifestUploader
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils.FapManifestsLoader
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val TIMEOUT_MS = 5_000L

class FapManifestApiImplTest {
    private val oldItem = manifest(versionUid = "v1")
    private val newItem = manifest(versionUid = "v2")

    private lateinit var loaderState: MutableStateFlow<FapManifestLoaderState>
    private lateinit var underTest: FapManifestApiImpl

    @Before
    fun setUp() {
        loaderState = MutableStateFlow(FapManifestLoaderState.Loaded(persistentListOf(), isLoading = true))
        val loader = mockk<FapManifestsLoader> {
            every { invalidate() } returns mockk(relaxed = true)
            every { getManifestLoaderState() } returns loaderState
        }
        val uploader = mockk<FapManifestUploader> {
            coEvery { save(any(), any()) } just runs
        }
        val deleter = mockk<FapManifestDeleter> {
            coEvery { delete(any()) } just runs
        }
        underTest = FapManifestApiImpl(
            loaderFactory = mockk { every { this@mockk.invoke(any()) } returns loader },
            manifestUploader = uploader,
            manifestDeleter = deleter
        )
    }

    @Test
    fun `an update survives a reload that read the old manifest`() = runBlocking {
        loaderState.value = FapManifestLoaderState.Loaded(persistentListOf(oldItem), isLoading = true)
        awaitItems { it == listOf(oldItem) }

        underTest.add(pathToFap = "/ext/.tmp/tmp.fap", fapManifestItem = newItem)
        // The reload finishes with the .fim it read before the update
        loaderState.value = FapManifestLoaderState.Loaded(persistentListOf(oldItem), isLoading = false)

        val finished = withTimeout(TIMEOUT_MS) {
            underTest.getManifestFlow().first { it is FapManifestState.Loaded && !it.inProgress }
        } as FapManifestState.Loaded
        assertEquals(listOf(newItem), finished.items)
    }

    @Test
    fun `a reload started after the change reads the Flipper again`() = runBlocking {
        loaderState.value = FapManifestLoaderState.Loaded(persistentListOf(oldItem), isLoading = false)
        awaitItems { it == listOf(oldItem) }
        underTest.add(pathToFap = "/ext/.tmp/tmp.fap", fapManifestItem = newItem)

        // Fresh reload: the app was deleted on the Flipper meanwhile
        loaderState.value = FapManifestLoaderState.Loaded(persistentListOf(), isLoading = true)
        underTest.invalidateAsync()
        loaderState.value = FapManifestLoaderState.Loaded(persistentListOf(), isLoading = false)

        val items = awaitItems { it.isEmpty() }
        assertEquals(emptyList<FapManifestItem>(), items)
    }

    @Test
    fun `a disconnect drops local changes`() = runBlocking {
        loaderState.value = FapManifestLoaderState.Loaded(persistentListOf(oldItem), isLoading = false)
        awaitItems { it == listOf(oldItem) }
        underTest.add(pathToFap = "/ext/.tmp/tmp.fap", fapManifestItem = newItem)

        loaderState.value = FapManifestLoaderState.Failed(IllegalStateException("disconnected"))
        withTimeout(TIMEOUT_MS) {
            underTest.getManifestFlow().first { it is FapManifestState.NotLoaded }
        }
        loaderState.value = FapManifestLoaderState.Loaded(persistentListOf(oldItem), isLoading = false)

        awaitItems { it == listOf(oldItem) }
        Unit
    }

    private suspend fun awaitItems(predicate: (List<FapManifestItem>) -> Boolean) =
        withTimeout(TIMEOUT_MS) {
            underTest.getManifestFlow()
                .first { it is FapManifestState.Loaded && predicate(it.items) }
                .let { (it as FapManifestState.Loaded).items }
        }

    private fun manifest(versionUid: String) = FapManifestItem(
        applicationAlias = "app",
        uid = "uid",
        versionUid = versionUid,
        path = "/ext/apps/Tools/app.fap",
        fullName = "App",
        iconBase64 = null,
        sdkApi = null,
        isDevCatalog = false
    )
}
