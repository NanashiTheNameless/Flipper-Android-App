package dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api.BleDeviceConnectionApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api.FBleApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api.FBleDeviceConnectionConfig
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.api.FBleApiImpl
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.api.FBleApiWithSerialFactory
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.model.BLEConnectionPermissionException
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.model.FailedConnectToDeviceException
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.utils.BLEConnectionDeviceHelper
import dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.impl.utils.BleConstants
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FInternalTransportConnectionStatus
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FTransportConnectionStatusListener
import dev.namelessnanashi.flipperzero.core.log.info
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionPriority

class BleDeviceConnectionApiImpl(
    private val context: Context,
    private val bleApiWithSerialFactory: FBleApiWithSerialFactory,
    private val connectionHelper: BLEConnectionDeviceHelper
) : BleDeviceConnectionApi {

    override suspend fun connect(
        scope: CoroutineScope,
        config: FBleDeviceConnectionConfig,
        listener: FTransportConnectionStatusListener
    ): Result<FBleApi> = runCatching {
        return@runCatching connectUnsafe(scope, config, listener)
    }

    private suspend fun connectUnsafe(
        scope: CoroutineScope,
        config: FBleDeviceConnectionConfig,
        listener: FTransportConnectionStatusListener
    ): FBleApi {
        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            throw BLEConnectionPermissionException()
        }
        listener.onStatusUpdate(FInternalTransportConnectionStatus.Connecting)
        info { "Finding device with ${BleConstants.CONNECT_TIME_MS} timeout..." }
        val device = withTimeout(BleConstants.CONNECT_TIME_MS) {
            connectionHelper.acceleratedBleConnect(
                context = context,
                macAddress = config.macAddress,
                scope = scope
            )
        }
        info { "Find device" }
        if (!device.isConnected) {
            info { "Device failed to connect, so throw exception" }
            throw FailedConnectToDeviceException()
        }
        listener.onStatusUpdate(FInternalTransportConnectionStatus.Pairing)

        info { "Wait for bonding ${BleConstants.PAIR_TIME_MS}" }
        withTimeout(BleConstants.PAIR_TIME_MS) {
            device.waitForBonding()
        }
        info { "Bonding completed!" }

        device.requestMtu(BleConstants.MAX_MTU)
        device.requestConnectionPriority(BleGattConnectionPriority.HIGH)
        device.discoverServices()

        val serialConfig = config.serialConfig
        return if (serialConfig == null) {
            FBleApiImpl(
                scope = scope,
                client = device,
                statusListener = listener,
                metaInfoGattMap = config.metaInfoGattMap
            )
        } else {
            bleApiWithSerialFactory.build(
                deviceScope = scope,
                scope = scope,
                serialConfig = serialConfig,
                metaInfoGattMap = config.metaInfoGattMap,
                client = device,
                statusListener = listener
            )
        }
    }
}
