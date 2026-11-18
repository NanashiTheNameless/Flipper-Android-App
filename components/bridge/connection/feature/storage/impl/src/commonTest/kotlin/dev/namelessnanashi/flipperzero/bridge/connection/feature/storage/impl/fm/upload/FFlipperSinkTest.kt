package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.upload

import com.flipperdevices.protobuf.Main
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okio.Buffer
import okio.ByteString
import okio.buffer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

class FFlipperSinkTest {
    private lateinit var writeLooper: WriteRequestLooper
    private lateinit var underTest: FFlipperSink
    private var receiveBytes: ByteArray = byteArrayOf()
    private var hasNext = true

    @BeforeTest
    fun setUp() {
        writeLooper = mockk(relaxed = true) {
            every { writeSync(any(), any()) } answers {
                receiveBytes += (it.invocation.args.first() as ByteString).toByteArray()
                hasNext = it.invocation.args[1] as Boolean
            }
            every { awaitResult() } returns Result.success(Main())
        }
        underTest = FFlipperSink(writeLooper)
    }

    @Test
    fun `write one message`() {
        val bytes = ByteArray(512) { it.toByte() }

        val buffer = Buffer()
        buffer.write(bytes)

        underTest.write(buffer, 512)
        underTest.flush()

        assertContentEquals(bytes, receiveBytes)
        assert(hasNext)
    }

    @Test
    fun `write one message completly`() {
        val bytes = ByteArray(512) { it.toByte() }

        val buffer = Buffer()
        buffer.write(bytes)

        underTest.write(buffer, 512)
        underTest.close()

        assertContentEquals(bytes, receiveBytes)
        assert(hasNext.not())
        verify(exactly = 1) { writeLooper.writeSync(any(), any()) }
    }

    @Test
    fun `write two messages`() {
        val bytes = ByteArray(1000) { it.toByte() }

        val buffer = Buffer()
        buffer.write(bytes)

        underTest.write(buffer, 1000)
        underTest.flush()

        assertContentEquals(bytes, receiveBytes)
        assert(hasNext)
        verify(exactly = 2) { writeLooper.writeSync(any(), any()) }
    }

    @Test
    fun `write one message complete`() {
        val bytes = ByteArray(512) { it.toByte() }

        val buffer = Buffer()
        buffer.write(bytes)

        underTest.write(buffer, 512)
        underTest.close()

        assertContentEquals(bytes, receiveBytes)
        assert(hasNext.not())
    }

    @Test
    fun `write two messages complete`() {
        val bytes = ByteArray(1000) { it.toByte() }

        val buffer = Buffer()
        buffer.write(bytes)

        underTest.write(buffer, 1000)

        assertContentEquals(bytes.take(512).toByteArray(), receiveBytes)

        underTest.close()

        assertContentEquals(bytes, receiveBytes)
        assert(hasNext.not())
        verify(exactly = 2) { writeLooper.writeSync(any(), any()) }
    }

    @Test
    fun `write huge message`() {
        val bytes = ByteArray(5000) { it.toByte() }

        val buffer = underTest.buffer()
        buffer.write(bytes)
        buffer.close()

        assertContentEquals(bytes, receiveBytes)
        assert(hasNext.not())
        verify(exactly = 10) { writeLooper.writeSync(any(), any()) }
    }

    @Test
    fun `buffered writeAll of a small file sends every byte when the buffered sink is closed`() {
        // favorites.txt-sized: smaller than one okio segment, so it stays buffered until close
        val bytes = "/ext/subghz/Garage.sub\n".encodeToByteArray()

        underTest.buffer().use { sink -> sink.writeAll(Buffer().write(bytes)) }

        assertContentEquals(bytes, receiveBytes)
        assert(hasNext.not())
    }

    @Test
    fun `second close sends nothing`() {
        underTest.write(Buffer().write(ByteArray(10)), 10)
        underTest.close()
        underTest.close()

        verify(exactly = 1) { writeLooper.writeSync(any(), false) }
    }

    @Test
    fun `successful write commits once and does not fail`() {
        var commits = 0
        var fails = 0
        every { writeLooper.awaitResult() } returns Result.success(Main())
        val sink = FFlipperSink(writeLooper, onCommit = { commits++ }, onFail = { fails++ })

        sink.write(Buffer().write(ByteArray(10)), 10)
        sink.close()
        sink.close()

        assertEquals(1, commits)
        assertEquals(0, fails)
    }

    @Test
    fun `failed write does not commit`() {
        var commits = 0
        var fails = 0
        every { writeLooper.awaitResult() } returns Result.failure(IllegalStateException("interrupted"))
        val sink = FFlipperSink(writeLooper, onCommit = { commits++ }, onFail = { fails++ })

        sink.write(Buffer().write(ByteArray(10)), 10)
        assertFailsWith<IllegalStateException> { sink.close() }

        assertEquals(0, commits)
        assertEquals(1, fails)
    }
}
