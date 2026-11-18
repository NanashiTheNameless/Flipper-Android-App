package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.upload

import dev.namelessnanashi.flipperzero.bridge.connection.pbutils.ProtobufConstants
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.warn
import okio.Buffer
import okio.Sink
import okio.Timeout
import kotlin.math.min

/**
 * Streams a file to the Flipper in chunks. [onCommit] runs after the Flipper confirmed the whole
 * write (used to move a temp file into place); [onFail] runs when the write did not complete.
 */
internal class FFlipperSink(
    private val requestLooper: WriteRequestLooper,
    private val chunkSize: Long = ProtobufConstants.MAX_FILE_DATA.toLong(),
    private val onCommit: () -> Unit = {},
    private val onFail: () -> Unit = {}
) : Sink, LogTagProvider {
    override val TAG = "FFlipperSink"

    private val buffer = Buffer()
    private var isClosed = false

    @Synchronized
    override fun close() {
        // A second close would send another final chunk, which the firmware opens as a new,
        // empty write
        if (isClosed) return
        isClosed = true

        val result = runCatching {
            while (buffer.size > chunkSize) {
                requestLooper.writeSync(buffer.readByteString(min(buffer.size, chunkSize)))
            }

            requestLooper.writeSync(buffer.readByteString(), hasNext = false)

            requestLooper.awaitResult().getOrThrow()
        }
        result.onSuccess { onCommit() }
            .onFailure {
                warn { "Write failed: ${it.message}" }
                runCatching(onFail)
            }
            .getOrThrow()
    }

    @Synchronized
    override fun flush() {
        check(!isClosed) { "Sink is closed" }
        while (buffer.size != 0L) {
            requestLooper.writeSync(buffer.readByteString(min(buffer.size, chunkSize)))
        }
    }

    override fun timeout() = Timeout.NONE

    @Synchronized
    override fun write(source: Buffer, byteCount: Long) {
        check(!isClosed) { "Sink is closed" }
        var bytesRemaining = byteCount
        var leftToWrite = getLeftToWrite()
        while (bytesRemaining > leftToWrite) {
            buffer.write(source, leftToWrite)
            bytesRemaining -= leftToWrite
            flush()
            leftToWrite = getLeftToWrite()
        }
        buffer.write(source, bytesRemaining)
    }

    private fun getLeftToWrite(): Long {
        while (buffer.size > chunkSize) {
            warn { "It's not normal that we have a buffer overflow at this point." }
            requestLooper.writeSync(buffer.readByteString(min(buffer.size, chunkSize)))
        }

        return chunkSize - buffer.size
    }
}
