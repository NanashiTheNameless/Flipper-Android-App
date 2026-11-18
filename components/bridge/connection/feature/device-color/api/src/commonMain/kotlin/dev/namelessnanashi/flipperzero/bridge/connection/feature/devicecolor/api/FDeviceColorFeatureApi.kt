package dev.namelessnanashi.flipperzero.bridge.connection.feature.devicecolor.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.core.preference.pb.FlipperZeroBle.HardwareColor
import kotlinx.coroutines.flow.Flow

interface FDeviceColorFeatureApi : FDeviceFeatureApi {
    /**
     * Receive color from flipper and save it into settings
     * @param default default value on response failure
     */
    fun updateAndGetColorFlow(default: HardwareColor = HardwareColor.WHITE): Flow<HardwareColor>
}
