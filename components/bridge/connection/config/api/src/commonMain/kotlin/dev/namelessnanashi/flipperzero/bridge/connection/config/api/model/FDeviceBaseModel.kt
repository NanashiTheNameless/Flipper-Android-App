package dev.namelessnanashi.flipperzero.bridge.connection.config.api.model

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDeviceType

sealed interface FDeviceBaseModel {
    val humanReadableName: String
    val type: FDeviceType
    val uniqueId: String
}
