package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl.serial

import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FSerialRestartApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error

class NoopRestartApi : FSerialRestartApi, LogTagProvider {
    override val TAG = "NoopRestartApi"

    override suspend fun restartRpc() {
        error { "Restart rpc failed, because this is noop implementation" }
    }

    override suspend fun sendTrashBytesAndBrokeSession() {
        error { "Restart rpc failed, because this is noop implementation" }
    }
}
