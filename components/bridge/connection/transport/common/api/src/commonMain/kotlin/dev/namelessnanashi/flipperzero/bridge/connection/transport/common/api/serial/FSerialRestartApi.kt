package dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial

interface FSerialRestartApi {
    suspend fun restartRpc()
    suspend fun sendTrashBytesAndBrokeSession()
}
