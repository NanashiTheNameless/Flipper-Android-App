package dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.impl.api

import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.system.ProtobufVersionRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.FTransportMetaInfoApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.TransportMetaInfoKey
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val ATTEMPTS = 5
private const val RETRY_DELAY_MS = 500L

/**
 * Fork: transports without BLE GATT metadata (USB) used to report a made-up protocol version.
 * This asks the Flipper itself (`system_protobuf_version_request`) and reports it in the same
 * "major.minor" form the GATT characteristic uses.
 */
internal class RpcApiVersionMetaInfoApi(
    private val rpcFeatureApi: FRpcFeatureApi
) : FTransportMetaInfoApi, LogTagProvider {
    override val TAG = "RpcApiVersionMetaInfoApi"

    override fun get(key: TransportMetaInfoKey): Result<Flow<ByteArray?>> {
        if (key != TransportMetaInfoKey.API_VERSION) {
            return Result.failure(IllegalArgumentException("$key is not available over RPC here"))
        }
        return Result.success(
            flow {
                repeat(ATTEMPTS) { attempt ->
                    val response = rpcFeatureApi.requestOnce(
                        Main(system_protobuf_version_request = ProtobufVersionRequest()).wrapToRequest()
                    ).mapCatching { requireNotNull(it.system_protobuf_version_response) }
                    response.onSuccess { version ->
                        emit("${version.major}.${version.minor}".toByteArray())
                        return@flow
                    }.onFailure {
                        error(it) { "Protobuf version request failed (attempt ${attempt + 1})" }
                    }
                    delay(RETRY_DELAY_MS)
                }
                emit(null)
            }
        )
    }
}
