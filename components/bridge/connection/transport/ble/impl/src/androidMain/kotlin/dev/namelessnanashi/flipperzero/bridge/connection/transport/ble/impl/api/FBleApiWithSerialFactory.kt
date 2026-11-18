package dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.actionnotifier.api.FlipperActionNotifier
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api.FBleDeviceSerialConfig
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api.GATTCharacteristicAddress
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.serial.FSerialDeviceApiWrapper
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.serial.FSerialRestartApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FTransportConnectionStatusListener
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.TransportMetaInfoKey
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import dev.zacsweers.metro.Inject

class FBleApiWithSerialFactory @Inject constructor(
    private val serialDeviceApiWrapperFactory: FSerialDeviceApiWrapper.Factory,
    private val fSerialRestartApiFactory: FSerialRestartApiImpl.Factory,
    private val flipperActionNotifierFactory: FlipperActionNotifier.Factory
) {
    @Suppress("LongParameterList")
    fun build(
        deviceScope: CoroutineScope,
        scope: CoroutineScope,
        client: ClientBleGatt,
        serialConfig: FBleDeviceSerialConfig,
        metaInfoGattMap: ImmutableMap<TransportMetaInfoKey, GATTCharacteristicAddress>,
        statusListener: FTransportConnectionStatusListener
    ): FBleApiWithSerial {
        val flipperActionNotifier = flipperActionNotifierFactory.invoke(scope)
        val serialDeviceApi = serialDeviceApiWrapperFactory(
            scope = scope,
            config = serialConfig,
            serviceFlow = client.services,
            flipperActionNotifier = flipperActionNotifier
        )
        val restartApi = fSerialRestartApiFactory(
            services = client.services,
            serialServiceUuid = serialConfig.serialServiceUuid,
            resetCharUUID = serialConfig.resetCharUUID
        )
        return FBleApiWithSerial(
            deviceScope = deviceScope,
            client = client,
            metaInfoGattMap = metaInfoGattMap,
            statusListener = statusListener,
            serialDeviceApi = serialDeviceApi,
            serialRestartApi = restartApi,
        )
    }
}
