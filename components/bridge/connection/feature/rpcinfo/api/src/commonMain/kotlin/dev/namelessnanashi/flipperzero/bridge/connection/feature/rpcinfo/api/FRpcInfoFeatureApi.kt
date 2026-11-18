package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface FRpcInfoFeatureApi : FDeviceFeatureApi {
    suspend fun invalidate(
        scope: CoroutineScope,
        force: Boolean = false
    )

    fun getRpcInformationFlow(): StateFlow<FlipperInformationStatus<FlipperRpcInformation>>

    suspend fun reset()
}
