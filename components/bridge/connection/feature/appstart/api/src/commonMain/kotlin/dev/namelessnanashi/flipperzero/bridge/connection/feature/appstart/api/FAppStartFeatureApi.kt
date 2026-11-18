package dev.namelessnanashi.flipperzero.bridge.connection.feature.appstart.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi

interface FAppStartFeatureApi : FDeviceFeatureApi {
    suspend fun startApp(path: okio.Path): Result<Unit>
}
