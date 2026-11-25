package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.restartrpc.api.FRestartRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.reader.PeripheralResponseReader
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.storage.FRequestStorage
import dev.namelessnanashi.flipperzero.bridge.connection.pbutils.encodeWithDelimitedSize
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialDeviceApi
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.log.warn
import com.flipperdevices.protobuf.CommandStatus
import com.flipperdevices.protobuf.Main
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FProtobufMessageCollector @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted restartApiFeature: FRestartRpcFeatureApi?,
    @Assisted private val serialApi: FSerialDeviceApi,
    @Assisted private val requestListeners: MutableMap<Int, OnReceiveResponse>,
    @Assisted private val requestStorage: FRequestStorage,
    readerFactory: PeripheralResponseReader.Factory
) {
    private val reader: PeripheralResponseReader = readerFactory(
        scope = scope,
        restartRPCApi = restartApiFeature
    )
    private val notificationMutableFlow = MutableSharedFlow<Main>()
    val notificationFlow: Flow<Main> = notificationMutableFlow

    private val foreignClientDetector = ForeignClientDetector()
    private val foreignClientMutableFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val foreignClientFlow: Flow<Unit> = foreignClientMutableFlow

    init {
        subscribeToAnswers(scope)
    }

    private fun subscribeToAnswers(scope: CoroutineScope) {
        scope.launch(FlipperDispatchers.blockingDispatcher) {
            serialApi.getReceiveBytesFlow().collect {
                reader.onReceiveBytes(it)
            }
        }
        scope.launch(FlipperDispatchers.blockingDispatcher) {
            reader.getResponses().collect {
                val listener = requestListeners[it.command_id]
                if (listener == null) {
                    warn { "Receive package without id $it" }
                    // Id 0 is used for unsolicited messages; any other id this app never sent
                    // belongs to another app talking to the same Flipper
                    if (it.command_id != 0 && !requestStorage.wasSent(it.command_id) &&
                        foreignClientDetector.onUnknownAnswer(System.currentTimeMillis())
                    ) {
                        warn { "Answers to commands this app never sent: another app is connected" }
                        foreignClientMutableFlow.tryEmit(Unit)
                    }
                    notificationMutableFlow.emit(it)
                } else {
                    listener.invoke(it)
                }
            }
        }

        scope.launch(FlipperDispatchers.blockingDispatcher) {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) { onClose() }
            }
        }
        scope.launch(FlipperDispatchers.blockingDispatcher) {
            while (isActive) {
                val request = requestStorage.getNextRequest()
                if (request != null) {
                    serialApi.sendBytes(request.data.encodeWithDelimitedSize())
                    runCatching {
                        request.onSendCallback?.invoke()
                    }.onFailure {
                        error(it) { "Failed execute callback on send for $request" }
                    }
                }
            }
        }
    }

    private suspend fun onClose() {
        var counter = 0
        info { "Found ${requestListeners.size} request, start clean" }
        while (requestListeners.isNotEmpty()) {
            info { "Start iteration for clean ${requestListeners.size} requests" }
            val listeners = ArrayList(requestListeners.keys)
            listeners.forEach { id ->
                counter++
                val listener = requestListeners.remove(id)
                info { "Invoke close for $listener" }
                listener?.invoke(
                    Main(
                        command_status = CommandStatus.ERROR,
                        has_next = false
                    )
                )
            }
        }

        info { "Complete reset and finish $counter tasks" }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            @Assisted scope: CoroutineScope,
            @Assisted restartApiFeature: FRestartRpcFeatureApi?,
            @Assisted serialApi: FSerialDeviceApi,
            @Assisted requestListeners: MutableMap<Int, OnReceiveResponse>,
            @Assisted requestStorage: FRequestStorage,
        ): FProtobufMessageCollector
    }
}
