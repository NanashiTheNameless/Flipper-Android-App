package dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial

data class FlipperSerialSpeed(
    val receiveBytesInSec: Long = 0L,
    val transmitBytesInSec: Long = 0L
)
