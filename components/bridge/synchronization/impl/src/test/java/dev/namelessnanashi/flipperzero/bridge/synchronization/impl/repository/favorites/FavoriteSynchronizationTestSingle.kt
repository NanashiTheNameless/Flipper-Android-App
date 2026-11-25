package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.favorites

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.FlipperFavoritesRepositoryImpl
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.FlipperKeyStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FavoriteSynchronization
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FavoriteSynchronizationImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.FlipperFavoritesRepository
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestRepository
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestRepositoryImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestStorageImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.utils.detailedProgressWrapperTrackerStub
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [BuildKonfig.ROBOELECTRIC_SDK_VERSION])
class FavoriteSynchronizationTestSingle {
    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var underTest: FavoriteSynchronization
    private lateinit var favoriteApi: FavoriteApi
    private lateinit var manifestRepository: ManifestRepository
    private lateinit var manifestStorage: ManifestStorage
    private lateinit var flipperStorage: FlipperKeyStorage
    private lateinit var favoritesRepository: FlipperFavoritesRepository

    @Before
    fun setUp() {
        favoriteApi = mockk {
            coEvery { getFavoritePaths() } coAnswers { getFavorites().map { it.getKeyPath() } }
        }
        flipperStorage = mockk()
        favoritesRepository = mockk()

        val testFolder = folder.newFolder("manifest")
        val context = mockk<Context> {
            every { filesDir } returns testFolder
        }
        manifestStorage = ManifestStorageImpl(context)
        manifestRepository = ManifestRepositoryImpl(manifestStorage)

        underTest = FavoriteSynchronizationImpl(
            favoriteApi = favoriteApi,
            manifestRepository = manifestRepository,
            flipperStorage = flipperStorage,
            favoritesRepository = favoritesRepository
        )
    }

    @Test
    @Suppress("LongMethod")
    fun `legacy key-only favorites import uncached paths without changing flipper order`() = runTest {
        val manifestFavorites = listOf(
            FlipperFilePath("test", "test.ibtn"),
            FlipperFilePath("test", "test2.nfc"),
            FlipperFilePath("test", "test3.nfc"),
            FlipperFilePath("test", "test4.nfc"),
            FlipperFilePath("test", "test5.nfc")
        )
        val manifestFlipper = listOf(
            FlipperFilePath("test", "test.ibtn"),
            FlipperFilePath("test", "test2.nfc"),
            FlipperFilePath("test", "test3.nfc"),
            FlipperFilePath("test", "notExistedKey2.nfc"),
            FlipperFilePath("test", "test4.nfc"),
            FlipperFilePath("test", "notExistedKey3.nfc"),
            FlipperFilePath("test", "test5.nfc")
        )
        manifestStorage.update {
            it.copy(
                favorites = manifestFavorites,
                favoritesFromFlipper = manifestFlipper
            )
        }

        val favoritesFromFlipper = listOf(
            FlipperFilePath("test", "test.ibtn"),
            FlipperFilePath("test", "test2.nfc"),
            FlipperFilePath("test", "test3.nfc"),
            FlipperFilePath("test", "notExistedKey2.nfc"),
            FlipperFilePath("test", "test4.nfc"),
            FlipperFilePath("test", "notExistedKey3.nfc"),
            FlipperFilePath("test", "test5.nfc")
        )
        val favoritesFromAndroid = listOf(
            FlipperFilePath("test", "test.ibtn"),
            FlipperFilePath("test", "test2.nfc"),
            FlipperFilePath("test", "test3.nfc"),
            FlipperFilePath("test", "test4.nfc"),
            FlipperFilePath("test", "test5.nfc")
        )
        coEvery { favoritesRepository.getFavorites(any()) } returns favoritesFromFlipper
        coEvery { favoriteApi.getFavorites() } returns favoritesFromAndroid.map { filePath ->
            FlipperKey(
                mainFile = FlipperFile(filePath, FlipperKeyContent.RawData(byteArrayOf())),
                synchronized = false,
                deleted = false
            )
        }

        coEvery { favoritesRepository.applyDiff(any(), any(), any()) } returns favoritesFromFlipper
        coEvery { favoriteApi.updateFavorites(any()) } answers { firstArg() }
        underTest.syncFavorites(detailedProgressWrapperTrackerStub())

        coVerify(exactly = 1) {
            favoritesRepository.applyDiff(
                flipperKeyStorage = any(),
                oldFavorites = any(),
                favoritesDiff = any()
            )
        }

        coVerify(exactly = 1) {
            favoriteApi.updateFavorites(favoritesFromFlipper.map { FlipperKeyPath(it, false) })
        }

        val manifestFile = manifestStorage.load()
        Assert.assertNotNull(manifestFile)
        Assert.assertEquals(manifestFlipper, manifestFile!!.favorites)
        Assert.assertEquals(manifestFlipper, manifestFile.favoritesFromFlipper)
    }
    @Test
    fun `phone reorder reaches flipper without adding or removing favorites`() = runTest {
        checkOrderSync(phoneReordered = true)
    }

    @Test
    fun `flipper reorder reaches phone without rewriting flipper favorites`() = runTest {
        checkOrderSync(phoneReordered = false)
    }

    private suspend fun checkOrderSync(phoneReordered: Boolean) {
        val garage = FlipperFilePath("subghz", "Garage.sub")
        val gate = FlipperFilePath("subghz", "Gate.sub")
        val unknown = FlipperFilePath("apps", "Unknown.fap")
        val original = listOf(garage, gate)
        val reordered = listOf(gate, garage)
        manifestStorage.update {
            it.copy(favorites = original, favoritesFromFlipper = listOf(garage, unknown, gate))
        }
        var content = (if (phoneReordered) listOf(garage, unknown, gate) else listOf(gate, unknown, garage))
            .joinToString("\n", postfix = "\n") { it.getPathOnFlipper() }.toByteArray()
        coEvery { flipperStorage.loadFile(any()) } answers { FlipperKeyContent.RawData(content) }
        coEvery { flipperStorage.saveFile(any(), any()) } answers {
            content = secondArg<FlipperKeyContent.RawData>().bytes
        }
        coEvery { favoriteApi.getFavorites() } returns (if (phoneReordered) reordered else original).map {
            FlipperKey(FlipperFile(it, FlipperKeyContent.RawData(byteArrayOf())), synchronized = true, deleted = false)
        }
        coEvery { favoriteApi.updateFavorites(any()) } answers {
            firstArg<List<FlipperKeyPath>>().filter { it.path != unknown }
        }
        val sync = FavoriteSynchronizationImpl(favoriteApi, manifestRepository, flipperStorage, FlipperFavoritesRepositoryImpl())

        sync.syncFavorites(detailedProgressWrapperTrackerStub())

        Assert.assertEquals(reordered, manifestStorage.load()!!.favorites)
        Assert.assertEquals(listOf(gate, unknown, garage), manifestStorage.load()!!.favoritesFromFlipper)
        Assert.assertEquals("/ext/subghz/Gate.sub\n/ext/apps/Unknown.fap\n/ext/subghz/Garage.sub\n", content.decodeToString())
        coVerify(exactly = if (phoneReordered) 1 else 0) { flipperStorage.saveFile(any(), any()) }
    }

    @Test
    fun `remote favorites are imported even when no key membership changed`() = runTest {
        val bedroom = FlipperFilePath("subghz", "remote/BedRoom.txt")
        val workroom = FlipperFilePath("subghz", "remote/WorkRoom.txt")
        val garage = FlipperFilePath("subghz", "Garage.sub")
        val full = listOf(bedroom, garage, workroom)
        manifestStorage.update { it.copy(favorites = listOf(garage), favoritesFromFlipper = full) }
        coEvery { favoritesRepository.getFavorites(any()) } returns full
        coEvery { favoritesRepository.applyDiff(any(), any(), any()) } returns full
        coEvery { favoriteApi.getFavoritePaths() } returns listOf(FlipperKeyPath(garage, false))
        coEvery { favoriteApi.updateFavorites(any()) } answers { firstArg() }

        underTest.syncFavorites(detailedProgressWrapperTrackerStub())

        coVerify { favoriteApi.updateFavorites(full.map { FlipperKeyPath(it, false) }) }
        Assert.assertEquals(full, manifestStorage.load()!!.favorites)
    }

}
