package dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api.GATTCharacteristicAddress
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.serial.FSerialRestartApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FTransportConnectionStatusListener
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.TransportMetaInfoKey
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialRestartApi
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt

class FBleApiWithSerial(
    deviceScope: CoroutineScope,
    client: ClientBleGatt,
    metaInfoGattMap: ImmutableMap<TransportMetaInfoKey, GATTCharacteristicAddress>,
    statusListener: FTransportConnectionStatusListener,
    serialDeviceApi: FSerialDeviceApi,
    serialRestartApi: FSerialRestartApiImpl,
) : FBleApiImpl(deviceScope, client, statusListener, metaInfoGattMap),
    FSerialDeviceApi by serialDeviceApi,
    FSerialRestartApi by serialRestartApi
