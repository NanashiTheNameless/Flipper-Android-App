package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.storage

import com.flipperdevices.protobuf.Main
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequest
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.log.warn
import dev.zacsweers.metro.Inject
import java.util.PriorityQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val QUEUE_INITIAL_CAPACITY = 11
private const val REQUEST_POOL_TIMEOUT_MS = 100L

// A writer that stops sending chunks must not block every other storage request forever
private const val WRITE_SESSION_STALE_MS = 60_000L
private const val RECENT_SENT_IDS = 4096

/**
 * Queue of requests waiting to be sent to the Flipper.
 *
 * The firmware keeps one storage write open at a time. Any other storage command
 * that arrives between the chunks of a multi-chunk write closes it, and the next chunk
 * then reopens the file with FSOM_CREATE_ALWAYS, so only the tail of the file survives.
 * While such a write is in progress, only its own chunks and non-storage requests are sent.
 */
class FRequestStorage @Inject constructor() : LogTagProvider {
    override val TAG = "FlipperRequestStorage"

    private val lock = ReentrantLock()
    private val queueChanged = lock.newCondition()
    private val queue = PriorityQueue(QUEUE_INITIAL_CAPACITY, FRequestComparator())

    private var activeWriteId: Int? = null

    // Ids of the last sent requests, to tell this app's answers from another app's
    private val recentSentIds = object : LinkedHashMap<Int, Unit>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Unit>?) = size > RECENT_SENT_IDS
    }
    private var activeWriteLastSentNanos = 0L

    fun sendRequest(vararg requests: FlipperRequest) = lock.withLock {
        queue.addAll(requests)
        queueChanged.signalAll()
        info { "Add new request. New request storage size: ${queue.size}" }
    }

    fun removeRequest(request: FlipperRequest) = lock.withLock {
        val isRemoved = queue.remove(request)
        if (isRemoved) {
            info { "Remove request ($isRemoved). New request storage size: ${queue.size}" }
        }
    }

    fun removeIf(filter: (FlipperRequest) -> Boolean) = lock.withLock {
        info {
            "Start remove from storage by filter. Current request storage size is ${queue.size}"
        }
        if (queue.removeIf(filter)) {
            info { "Found request for deleted by filter and delete it" }
        }
        info {
            "Finish remove from storage by filter. Current request storage size is ${queue.size}"
        }
    }

    /**
     * Ends the write session for [commandId] when it will not send its final chunk,
     * e.g. after an error or cancellation. Its queued chunks are dropped: sending them
     * later would start a new write and truncate the file.
     */
    fun abortWriteSession(commandId: Int) = lock.withLock {
        queue.removeIf { it.data.command_id == commandId }
        if (activeWriteId == commandId) {
            info { "Abort write session $commandId" }
            activeWriteId = null
            queueChanged.signalAll()
        }
    }

    fun wasSent(commandId: Int): Boolean = lock.withLock { recentSentIds.containsKey(commandId) }

    fun getNextRequest(timeout: Long = REQUEST_POOL_TIMEOUT_MS): FlipperRequest? = lock.withLock {
        var remainingNanos = TimeUnit.MILLISECONDS.toNanos(timeout)
        var request = takeNextSendable()
        while (request == null && remainingNanos > 0L) {
            remainingNanos = queueChanged.awaitNanos(remainingNanos)
            request = takeNextSendable()
        }
        if (request != null) {
            info { "Remove request from queue and new queue size is ${queue.size}" }
        }
        request
    }

    private fun takeNextSendable(): FlipperRequest? {
        val writeId = activeWriteId
        if (writeId != null &&
            System.nanoTime() - activeWriteLastSentNanos > TimeUnit.MILLISECONDS.toNanos(WRITE_SESSION_STALE_MS)
        ) {
            warn { "Write session $writeId sent nothing for ${WRITE_SESSION_STALE_MS}ms, releasing it" }
            activeWriteId = null
        }

        val request = if (activeWriteId == null) {
            queue.poll()
        } else {
            queue.filter { it.canBeSentDuring(activeWriteId) }
                .minWithOrNull(queue.comparator())
                ?.also { queue.remove(it) }
        } ?: return null
        recentSentIds[request.data.command_id] = Unit

        val write = request.data.storage_write_request
        if (write != null) {
            if (request.data.has_next) {
                activeWriteId = request.data.command_id
                activeWriteLastSentNanos = System.nanoTime()
            } else if (activeWriteId == request.data.command_id) {
                activeWriteId = null
            }
        }
        return request
    }
}

private fun FlipperRequest.canBeSentDuring(writeId: Int?): Boolean =
    data.command_id == writeId || !data.isStorageRequest()

internal fun Main.isStorageRequest(): Boolean = storage_info_request != null ||
    storage_timestamp_request != null ||
    storage_stat_request != null ||
    storage_list_request != null ||
    storage_read_request != null ||
    storage_write_request != null ||
    storage_delete_request != null ||
    storage_mkdir_request != null ||
    storage_md5sum_request != null ||
    storage_rename_request != null ||
    storage_backup_create_request != null ||
    storage_backup_restore_request != null ||
    storage_tar_extract_request != null
