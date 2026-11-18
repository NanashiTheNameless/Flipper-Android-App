package dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api

import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import kotlinx.coroutines.CoroutineScope

/**
 * A basic primitive for requesting features from a device
 *
 * If you are a consumer of features:
 * @see dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
 *
 * If you are creating a feature, you need to do the following:
 * 1) Add enum to FDeviceFeature:
 * @see FDeviceFeature
 * 2) Add qualifier to FDeviceFeature#Factory implementation:
 * @see FDeviceFeatureQualifier
 * 3) Add feature to device-specific api. For example:
 * @see dev.namelessnanashi.flipperzero.bridge.connection.device.fzero.impl.utils.FZeroFeatureClassToEnumMapper
 */
interface FDeviceFeatureApi {
    fun interface Factory {
        suspend operator fun invoke(
            unsafeFeatureDeviceApi: FUnsafeDeviceFeatureApi,
            scope: CoroutineScope,
            connectedDevice: FConnectedDeviceApi
        ): FDeviceFeatureApi?
    }
}
