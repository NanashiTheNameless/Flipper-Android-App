package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.storage

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequestPriority
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.storage.ReadRequest
import com.flipperdevices.protobuf.storage.WriteRequest
import com.flipperdevices.protobuf.system.PingRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FRequestStorageTest {
    lateinit var subject: FRequestStorage

    @Before
    fun setUp() {
        subject = FRequestStorage()
    }

    @Test
    fun `Check that object with highest priority returns first`() = runBlocking {
        val lowPriority = Main().wrapToRequest(FlipperRequestPriority.BACKGROUND)
        val mediumPriority = Main().wrapToRequest()
        val highestPriority = Main().wrapToRequest(FlipperRequestPriority.FOREGROUND)

        subject.sendRequest(mediumPriority, lowPriority, highestPriority)

        assertEquals(highestPriority, subject.getNextRequest(timeout = 100))
        assertEquals(mediumPriority, subject.getNextRequest(timeout = 100))
        assertEquals(lowPriority, subject.getNextRequest(timeout = 100))
    }

    @Test
    fun `Check that oldest object returns first`() = runBlocking {
        val lowPriority = FlipperRequest(Main(), 300L)
        val mediumPriority = FlipperRequest(Main(), 200L)
        val highestPriority = FlipperRequest(Main(), 100L)

        subject.sendRequest(mediumPriority, lowPriority, highestPriority)

        assertEquals(highestPriority, subject.getNextRequest(timeout = 100))
        assertEquals(mediumPriority, subject.getNextRequest(timeout = 100))
        assertEquals(lowPriority, subject.getNextRequest(timeout = 100))
    }

    @Test
    fun `Return null if not present request`() = runBlocking {
        val request = Main().wrapToRequest(FlipperRequestPriority.BACKGROUND)

        subject.sendRequest(request)

        assertEquals(request, subject.getNextRequest(timeout = 100))
        assertNull(subject.getNextRequest(timeout = 100))
    }

    private fun writeChunk(id: Int, hasNext: Boolean, priority: FlipperRequestPriority = FlipperRequestPriority.DEFAULT) =
        Main(
            command_id = id,
            has_next = hasNext,
            storage_write_request = WriteRequest(path = "/ext/favorites.txt")
        ).wrapToRequest(priority)

    private fun storageRead(id: Int, priority: FlipperRequestPriority = FlipperRequestPriority.DEFAULT) =
        Main(command_id = id, storage_read_request = ReadRequest(path = "/ext/a.sub")).wrapToRequest(priority)

    private fun ping(id: Int) = Main(command_id = id, system_ping_request = PingRequest()).wrapToRequest()

    @Test
    fun `storage request waits while a multi-chunk write is open`() {
        val firstChunk = writeChunk(id = 1, hasNext = true)
        subject.sendRequest(firstChunk)
        assertEquals(firstChunk, subject.getNextRequest(timeout = 100))

        // A higher priority read must not slip between the chunks
        val read = storageRead(id = 2, priority = FlipperRequestPriority.RIGHT_NOW)
        subject.sendRequest(read)
        assertNull(subject.getNextRequest(timeout = 50))

        val lastChunk = writeChunk(id = 1, hasNext = false)
        subject.sendRequest(lastChunk)
        assertEquals(lastChunk, subject.getNextRequest(timeout = 100))
        assertEquals(read, subject.getNextRequest(timeout = 100))
    }

    @Test
    fun `non-storage requests keep flowing during a write`() {
        subject.sendRequest(writeChunk(id = 1, hasNext = true))
        subject.getNextRequest(timeout = 100)

        val pingRequest = ping(id = 3)
        subject.sendRequest(storageRead(id = 2), pingRequest)

        assertEquals(pingRequest, subject.getNextRequest(timeout = 100))
        assertNull(subject.getNextRequest(timeout = 50))
    }

    @Test
    fun `a second write waits for the first one`() {
        subject.sendRequest(writeChunk(id = 1, hasNext = true))
        subject.getNextRequest(timeout = 100)

        val otherWrite = writeChunk(id = 2, hasNext = true, priority = FlipperRequestPriority.FOREGROUND)
        subject.sendRequest(otherWrite)
        assertNull(subject.getNextRequest(timeout = 50))
    }

    @Test
    fun `aborting a write drops its queued chunks and releases the gate`() {
        subject.sendRequest(writeChunk(id = 1, hasNext = true))
        subject.getNextRequest(timeout = 100)
        val read = storageRead(id = 2)
        subject.sendRequest(writeChunk(id = 1, hasNext = true), read)

        subject.abortWriteSession(commandId = 1)

        assertEquals(read, subject.getNextRequest(timeout = 100))
        assertNull(subject.getNextRequest(timeout = 50))
    }

    @Test
    fun `single-chunk write does not open a session`() {
        subject.sendRequest(writeChunk(id = 1, hasNext = false))
        subject.getNextRequest(timeout = 100)

        val read = storageRead(id = 2)
        subject.sendRequest(read)
        assertEquals(read, subject.getNextRequest(timeout = 100))
    }

    @Test
    fun `remembers which command ids were sent`() {
        subject.sendRequest(ping(id = 42))
        subject.getNextRequest(timeout = 100)

        assert(subject.wasSent(42))
        assert(!subject.wasSent(43))
    }
}
