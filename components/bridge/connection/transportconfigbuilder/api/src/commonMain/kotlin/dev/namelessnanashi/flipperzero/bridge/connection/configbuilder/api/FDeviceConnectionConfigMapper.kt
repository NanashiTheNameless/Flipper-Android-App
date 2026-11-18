package dev.namelessnanashi.flipperzero.bridge.connection.configbuilder.api

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceBaseModel
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FDeviceConnectionConfig

interface FDeviceConnectionConfigMapper {
    fun getConnectionConfig(device: FDeviceBaseModel): FDeviceConnectionConfig<*>
}
