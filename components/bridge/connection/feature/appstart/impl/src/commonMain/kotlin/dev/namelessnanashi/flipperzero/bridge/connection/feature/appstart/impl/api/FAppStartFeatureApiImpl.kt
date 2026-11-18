package dev.namelessnanashi.flipperzero.bridge.connection.feature.appstart.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.appstart.api.FAppStartFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.app.StartRequest
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import okio.Path

class FAppStartFeatureApiImpl @AssistedInject constructor(
    @Assisted private val rpcFeatureApi: FRpcFeatureApi,
) : FAppStartFeatureApi,
    LogTagProvider {
    override val TAG = "FAlarmFeatureApi"

    override suspend fun startApp(path: Path): Result<Unit> {
        info { "#startApp path: $path" }
        return rpcFeatureApi.requestOnce(
            Main(
                app_start_request = StartRequest(path.toString())
            ).wrapToRequest()
        ).map { }
    }

    @AssistedFactory
    fun interface InternalFactory {
        operator fun invoke(
            rpcFeatureApi: FRpcFeatureApi,
        ): FAppStartFeatureApiImpl
    }
}
