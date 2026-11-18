package dev.namelessnanashi.flipperzero.bridge.connection.transport.ble.api

import java.util.UUID

data class GATTCharacteristicAddress(
    val serviceAddress: UUID,
    val characteristicAddress: UUID
)
