package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDownloadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDeleteApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.ForwardingSink
import org.junit.Assert
import org.junit.Test

class FlipperKeyStorageTest {
    @Test
    fun `small file reaches the flipper sink in full before it closes`() = runTest {
        val received = Buffer()
        var bytesAtClose = -1L
        val flipperSink = object : ForwardingSink(received) {
            override fun close() {
                bytesAtClose = received.size
            }
        }
        val uploadApi = mockk<FFileUploadApi> {
            coEvery { sink(any(), any()) } returns flipperSink
        }
        val storage = FlipperKeyStorage(mockk<FFileDownloadApi>(), uploadApi, mockk<FFileDeleteApi>())
        val content = "/ext/subghz/Garage.sub\n".encodeToByteArray()

        storage.saveFile(FlipperFilePath("/", "favorites.txt"), FlipperKeyContent.RawData(content))

        Assert.assertEquals(content.size.toLong(), bytesAtClose)
        Assert.assertArrayEquals(content, received.readByteArray())
    }
}
