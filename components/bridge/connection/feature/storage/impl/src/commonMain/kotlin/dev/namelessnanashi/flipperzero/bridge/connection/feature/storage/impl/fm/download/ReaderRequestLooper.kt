package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.download

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequestPriority
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.storage.ReadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okio.Closeable

class ReaderRequestLooper(
    private val rpcFeatureApi: FRpcFeatureApi,
    private val pathOnFlipper: String,
    private val priority: FlipperRequestPriority,
    private val scope: CoroutineScope
) : Closeable {
    private val queue = Channel<Main>(Channel.UNLIMITED)

    private val responseFlowJob: Job = scope.launch(FlipperDispatchers.workStealingDispatcher) {
        rpcFeatureApi.request(
            Main(
                storage_read_request = ReadRequest(
                    path = pathOnFlipper
                )
            ).wrapToRequest(priority)
        ).collect { responseResult ->
            responseResult.onFailure(queue::close).onSuccess { response ->
                queue.send(response)
            }
        }
    }

    /**
     * The queue is closed whenever reading stops (finished, failed or cancelled), so this
     * suspends instead of busy-polling and cannot hang
     */
    suspend fun getNextBytePack(): Main = queue.receive()

    override fun close() {
        responseFlowJob.cancel()
        queue.cancel()
    }

    init {
        responseFlowJob.invokeOnCompletion(queue::close)
    }
}
