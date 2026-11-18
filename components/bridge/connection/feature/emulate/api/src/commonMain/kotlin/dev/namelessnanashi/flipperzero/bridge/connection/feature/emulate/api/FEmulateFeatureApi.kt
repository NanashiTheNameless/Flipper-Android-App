package dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.helpers.AppEmulateHelper
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.helpers.EmulateHelper
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.helpers.FlipperAppErrorHelper
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.helpers.StartEmulateHelper
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.helpers.StopEmulateHelper
import kotlinx.coroutines.flow.Flow

interface FEmulateFeatureApi : FDeviceFeatureApi {
    fun getAppEmulateHelper(): AppEmulateHelper
    fun getEmulateHelper(): EmulateHelper
    fun getFlipperErrorHelper(): FlipperAppErrorHelper
    fun getStartEmulateHelper(): StartEmulateHelper
    fun getStopEmulateHelper(): StopEmulateHelper
    val isInfraredEmulationSupported: Flow<Boolean>
}
