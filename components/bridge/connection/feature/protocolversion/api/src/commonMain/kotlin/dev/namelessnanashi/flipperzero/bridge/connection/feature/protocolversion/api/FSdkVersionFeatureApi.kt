package dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.core.data.SemVer

interface FSdkVersionFeatureApi : FDeviceFeatureApi {
    suspend fun getSdkVersion(): Result<SemVer>
}
