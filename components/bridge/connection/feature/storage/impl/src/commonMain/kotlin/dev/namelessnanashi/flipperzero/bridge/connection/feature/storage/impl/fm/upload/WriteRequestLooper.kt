package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.upload

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequestPriority
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.storage.File
import com.flipperdevices.protobuf.storage.WriteRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.ByteString

internal class WriteRequestLooper(
    private val rpcFeatureApi: FRpcFeatureApi,
    private val pathOnFlipper: String,
    private val priority: FlipperRequestPriority,
    scope: CoroutineScope
) : LogTagProvider {
    override val TAG = "WriteRequestLooper"

    // We can't send to channel when it has zero subscribers
    private val commands = Channel<FlipperRequest>()
    private val result = MutableStateFlow<Result<Main>?>(null)

    init {
        scope.launch(FlipperDispatchers.workStealingDispatcher) {
            result.emit(
                rpcFeatureApi.request(
                    commandFlow = commands.consumeAsFlow(),
                    // Upstream closed the write here with an empty has_next=false request. The
                    // firmware opens that as a new write with FSOM_CREATE_ALWAYS, which truncated
                    // the file to 0 bytes. Writes go to a temp file instead (see FFlipperSink),
                    // so an abandoned write leaves the real file untouched.
                    onCancel = { commandId ->
                        info { "Write to $pathOnFlipper ($commandId) cancelled" }
                    }
                )
            )
        }
    }

    fun awaitResult() = runBlocking { result.filterNotNull().first() }

    fun writeSync(
        data: ByteString,
        hasNext: Boolean = true
    ): Unit = runBlocking {
        val waiter = Channel<Unit>()
        commands.send(
            FlipperRequest(
                data = Main(
                    has_next = hasNext,
                    storage_write_request = WriteRequest(
                        path = pathOnFlipper,
                        file_ = File(data_ = data)
                    )
                ),
                priority = priority,
                onSendCallback = {
                    waiter.send(Unit)
                }
            )
        )
        waiter.receive()
    }
}
