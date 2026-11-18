package dev.namelessnanashi.flipperzero.bridge.connection.screens.search

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceBaseModel

data class ConnectionSearchItem(
    val address: String,
    val deviceModel: FDeviceBaseModel,
    val isAdded: Boolean
)
