package dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGattInformation
import kotlinx.coroutines.flow.StateFlow

interface FGattInfoFeatureApi : FDeviceFeatureApi {
    fun getGattInfoFlow(): StateFlow<FGattInformation>
}
