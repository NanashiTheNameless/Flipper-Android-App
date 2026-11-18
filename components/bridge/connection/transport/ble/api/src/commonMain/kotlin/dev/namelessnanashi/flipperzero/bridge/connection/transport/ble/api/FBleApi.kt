package dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api

import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.meta.FTransportMetaInfoApi

interface FBleApi : FConnectedDeviceApi, FTransportMetaInfoApi
