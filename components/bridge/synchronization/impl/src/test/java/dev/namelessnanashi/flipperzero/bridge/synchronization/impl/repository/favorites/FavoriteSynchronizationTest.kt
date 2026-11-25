package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.favorites

import android.content.Context
import android.os.Build
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.FlipperKeyStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyAction
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyDiff
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FavoriteSynchronization
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FavoriteSynchronizationImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.FlipperFavoritesRepository
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestRepository
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestRepositoryImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestStorageImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.utils.detailedProgressWrapperTrackerStub
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
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class FavoriteSynchronizationTest(
    val param: FavoriteSynchronizationTestParam
) {
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
            coEvery { updateFavorites(any()) } coAnswers {
                (args[0] as List<*>).map { it as FlipperKeyPath }.filterNot {
                    it.path.nameWithoutExtension.startsWith("notExistedKey")
                }
            }
        }
        flipperStorage = mockk()
        favoritesRepository = mockk {
            coEvery { reorderFavorites(any(), any(), any()) } coAnswers {
                val old = secondArg<List<FlipperFilePath>>()
                val ordered = thirdArg<List<FlipperFilePath>>().filter { it in old }
                val iterator = ordered.iterator()
                old.map { if (it in ordered) iterator.next() else it }
            }
            coEvery { applyDiff(any(), any(), any()) } coAnswers {
                val oldFavorites = (args[1] as List<*>).map { it as FlipperFilePath }
                val favoritesDiff = (args[2] as List<*>).map { it as KeyDiff }
                val resultFavoritesList = ArrayList(oldFavorites)
                for (diff in favoritesDiff) {
                    when (diff.action) {
                        KeyAction.ADD -> resultFavoritesList.add(diff.newHash.keyPath)
                        KeyAction.MODIFIED -> resultFavoritesList.add(diff.newHash.keyPath)
                        KeyAction.DELETED -> resultFavoritesList.remove(diff.newHash.keyPath)
                    }
                }
                return@coAnswers resultFavoritesList
            }
        }

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
    fun `universal manifest test`() = runTest {
        manifestStorage.update {
            it.copy(
                favorites = param.initialFavoriteManifest,
                favoritesFromFlipper = param.initialFlipperFavoriteManifest
            )
        }

        val favoritesFromFlipper = param.flipperFavorites
        val favoritesFromAndroid = param.androidFavorites
        coEvery { favoritesRepository.getFavorites(any()) } returns favoritesFromFlipper
        coEvery { favoriteApi.getFavorites() } returns favoritesFromAndroid.map { filePath ->
            FlipperKey(
                mainFile = FlipperFile(filePath, FlipperKeyContent.RawData(byteArrayOf())),
                synchronized = false,
                deleted = false
            )
        }

        underTest.syncFavorites(detailedProgressWrapperTrackerStub())

        if (param.expectedDiffOnFlipper != null) {
            coVerify {
                favoritesRepository.applyDiff(
                    flipperKeyStorage = eq(flipperStorage),
                    oldFavorites = eq(favoritesFromFlipper),
                    favoritesDiff = eq(param.expectedDiffOnFlipper)
                )
            }
        }

        if (param.expectedFavoritesOnAndroid != null) {
            coVerify {
                favoriteApi.updateFavorites(
                    eq(
                        param.expectedFavoritesOnAndroid.map {
                            FlipperKeyPath(it, false)
                        }
                    )
                )
            }
        }

        val manifestFile = manifestStorage.load()
        Assert.assertNotNull(manifestFile)
        Assert.assertEquals(param.expectedFavoritesOnAndroidManifest, manifestFile!!.favorites)
        Assert.assertEquals(
            param.expectedFavoritesOnFlipperManifest,
            manifestFile.favoritesFromFlipper
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data() = testRuns
    }
}
