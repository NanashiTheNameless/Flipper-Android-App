package dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FListingStorageApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.ListingItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val LS_DELAY_MS = 100L

class FapExistCheckerTest {
    private lateinit var listingApi: FListingStorageApi
    private lateinit var underTest: FapExistChecker

    @Before
    fun setUp() {
        listingApi = mockk()
        val storageApi = mockk<FStorageFeatureApi> {
            every { listingApi() } returns listingApi
        }
        val featureProvider = mockk<FFeatureProvider> {
            coEvery { getSync(FStorageFeatureApi::class) } returns storageApi
        }
        coEvery { listingApi.ls(any()) } coAnswers {
            delay(LS_DELAY_MS)
            Result.success(listOf(ListingItem(fileName = "app.fap", fileType = null, size = 1)))
        }
        underTest = FapExistChecker(featureProvider)
    }

    @Test
    fun `checks in one folder share one listing`() = runTest {
        val first = async { underTest.checkExist("/ext/apps/Tools/app.fap") }
        val second = async { underTest.checkExist("/ext/apps/Tools/other.fap") }

        assertTrue(first.await())
        assertFalse(second.await())
        coVerify(exactly = 1) { listingApi.ls("/ext/apps/Tools") }
    }

    @Test
    fun `prefetch lists different folders in parallel`() = runTest {
        underTest.prefetch(
            listOf("/ext/apps/Tools/app.fap", "/ext/apps/GPIO/app.fap", "/ext/apps/NFC/app.fap")
        )

        assertEquals(LS_DELAY_MS, testScheduler.currentTime)
        assertTrue(underTest.checkExist("/ext/apps/GPIO/app.fap"))
        coVerify(exactly = 1) { listingApi.ls("/ext/apps/GPIO") }
    }

    @Test
    fun `a waiter lists again when the listing coroutine is cancelled`() = runTest {
        val listingStarted = CompletableDeferred<Unit>()
        coEvery { listingApi.ls(any()) } coAnswers {
            listingStarted.complete(Unit)
            awaitCancellation()
        } andThenAnswer {
            Result.success(listOf(ListingItem(fileName = "app.fap", fileType = null, size = 1)))
        }

        val owner = launch { underTest.checkExist("/ext/apps/Tools/app.fap") }
        listingStarted.await()
        val waiter = async { underTest.checkExist("/ext/apps/Tools/app.fap") }
        testScheduler.runCurrent()
        owner.cancel()

        assertTrue(waiter.await())
        coVerify(exactly = 2) { listingApi.ls("/ext/apps/Tools") }
    }

    @Test
    fun `invalidate lists the folder again`() = runTest {
        underTest.checkExist("/ext/apps/Tools/app.fap")
        underTest.invalidate()
        underTest.checkExist("/ext/apps/Tools/app.fap")

        coVerify(exactly = 2) { listingApi.ls("/ext/apps/Tools") }
    }
}
