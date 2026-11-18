package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.reader

import dev.namelessnanashi.flipperzero.bridge.connection.feature.restartrpc.api.FRestartRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.pbutils.decodeDelimitedPackage
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.ktx.jre.withLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import com.flipperdevices.protobuf.Main
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class PeripheralResponseReader @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    @Assisted private val restartRPCApi: FRestartRpcFeatureApi?,
) : LogTagProvider {
    override val TAG = "PeripheralResponseReader"
    private val mutex = Mutex()
    private var byteInputStream: ByteEndlessInputStream? = null
    private val responses = MutableSharedFlow<Main>()
    private var responseReaderJob: Job? = null

    init {
        scope.launch {
            initialize()
        }
    }

    private suspend fun initialize() = withLock(mutex, "initialize") {
        responseReaderJob?.cancelAndJoin()
        responseReaderJob = scope.launch(FlipperDispatchers.workStealingDispatcher) {
            val byteInputStreamLocal = ByteEndlessInputStream(this)
            byteInputStream = byteInputStreamLocal
            parseLoopJob(byteInputStreamLocal)
        }
    }

    fun onReceiveBytes(byteArray: ByteArray) {
        byteInputStream?.write(byteArray)
    }

    fun getResponses(): Flow<Main> = responses

    private suspend fun CoroutineScope.parseLoopJob(byteInputStream: ByteEndlessInputStream) {
        while (this.isActive) {
            try {
                val main = Main.ADAPTER.decodeDelimitedPackage(byteInputStream)
                scope.launch(FlipperDispatchers.workStealingDispatcher) {
                    responses.emit(main)
                }
            } catch (ignored: CancellationException) {
                // ignore
            } catch (e: Exception) {
                error(e) { "Failed parse stream" }
                restartRPCApi?.restartRpc()
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            scope: CoroutineScope,
            restartRPCApi: FRestartRpcFeatureApi?,
        ): PeripheralResponseReader
    }
}
