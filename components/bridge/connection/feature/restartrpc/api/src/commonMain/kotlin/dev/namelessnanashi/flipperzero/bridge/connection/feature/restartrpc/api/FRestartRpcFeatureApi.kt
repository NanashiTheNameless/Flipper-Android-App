package dev.namelessnanashi.flipperzero.bridge.connection.feature.restartrpc.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi

interface FRestartRpcFeatureApi : FDeviceFeatureApi {
    suspend fun restartRpc()
    suspend fun sendTrashBytesAndBrokeSession()
}
