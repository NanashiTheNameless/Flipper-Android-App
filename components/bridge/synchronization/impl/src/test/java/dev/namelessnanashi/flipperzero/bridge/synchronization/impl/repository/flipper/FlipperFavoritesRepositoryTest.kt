package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.FlipperKeyStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.DiffSource
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyAction
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyDiff
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyWithHash
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class FlipperFavoritesRepositoryTest {
    private val garage = FlipperFilePath("subghz", "Garage.sub")
    private val gate = FlipperFilePath("subghz", "Gate.sub")
    private val underTest = FlipperFavoritesRepositoryImpl()

    private fun diff(path: FlipperFilePath, action: KeyAction) = KeyDiff(
        newHash = KeyWithHash(path, hash = ""),
        action = action,
        source = DiffSource.ANDROID
    )

    private fun storageRemembering(): Pair<FlipperKeyStorage, () -> ByteArray?> {
        var written: ByteArray? = null
        val content = slot<FlipperKeyContent>()
        val storage = mockk<FlipperKeyStorage> {
            coEvery { saveFile(any(), capture(content)) } answers {
                written = (content.captured as FlipperKeyContent.RawData).bytes
            }
            coEvery { loadFile(any()) } answers { FlipperKeyContent.RawData(written ?: byteArrayOf()) }
        }
        return storage to { written }
    }

    @Test
    fun `no flipper-side changes means no write`() = runTest {
        val storage = mockk<FlipperKeyStorage>(relaxed = true)

        val result = underTest.applyDiff(storage, listOf(garage), favoritesDiff = emptyList())

        Assert.assertEquals(listOf(garage), result)
        coVerify(exactly = 0) { storage.saveFile(any(), any()) }
    }

    @Test
    fun `adding a favorite writes the full list and verifies it`() = runTest {
        val (storage, written) = storageRemembering()

        val result = underTest.applyDiff(storage, listOf(garage), listOf(diff(gate, KeyAction.ADD)))

        Assert.assertEquals(listOf(garage, gate), result)
        Assert.assertEquals(
            "/ext/subghz/Garage.sub\n/ext/subghz/Gate.sub\n",
            written()?.decodeToString()
        )
    }

    @Test
    fun `explicit removal of the last favorite is allowed`() = runTest {
        val (storage, written) = storageRemembering()

        val result = underTest.applyDiff(storage, listOf(garage), listOf(diff(garage, KeyAction.DELETED)))

        Assert.assertTrue(result.isEmpty())
        Assert.assertEquals("\n", written()?.decodeToString())
    }

    @Test(expected = IllegalStateException::class)
    fun `a write that does not read back the same fails`() = runTest {
        val storage = mockk<FlipperKeyStorage>(relaxed = true) {
            coEvery { loadFile(any()) } returns FlipperKeyContent.RawData(byteArrayOf())
        }

        underTest.applyDiff(storage, listOf(garage), listOf(diff(gate, KeyAction.ADD)))
    }
}
