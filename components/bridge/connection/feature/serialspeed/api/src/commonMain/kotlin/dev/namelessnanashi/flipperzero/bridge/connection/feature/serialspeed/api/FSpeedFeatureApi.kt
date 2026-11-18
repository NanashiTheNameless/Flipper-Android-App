package dev.namelessnanashi.flipperzero.bridge.connection.feature.serialspeed.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FlipperSerialSpeed
import kotlinx.coroutines.flow.StateFlow

interface FSpeedFeatureApi : FDeviceFeatureApi {
    suspend fun getSpeed(): StateFlow<FlipperSerialSpeed>
}
