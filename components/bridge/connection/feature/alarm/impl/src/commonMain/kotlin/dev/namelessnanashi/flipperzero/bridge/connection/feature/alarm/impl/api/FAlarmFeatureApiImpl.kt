package dev.namelessnanashi.flipperzero.bridge.connection.feature.alarm.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.alarm.api.FAlarmFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.system.PlayAudiovisualAlertRequest
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

class FAlarmFeatureApiImpl @AssistedInject constructor(
    @Assisted private val rpcFeatureApi: FRpcFeatureApi,
) : FAlarmFeatureApi,
    LogTagProvider {
    override val TAG = "FAlarmFeatureApi"

    override suspend fun makeSound() {
        rpcFeatureApi.requestWithoutAnswer(
            Main(
                system_play_audiovisual_alert_request = PlayAudiovisualAlertRequest()
            ).wrapToRequest()
        )
    }

    @AssistedFactory
    fun interface InternalFactory {
        operator fun invoke(
            rpcFeatureApi: FRpcFeatureApi,
        ): FAlarmFeatureApiImpl
    }
}
