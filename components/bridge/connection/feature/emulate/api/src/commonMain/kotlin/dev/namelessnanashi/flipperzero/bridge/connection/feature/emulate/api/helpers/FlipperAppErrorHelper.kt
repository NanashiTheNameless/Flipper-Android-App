package dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.helpers

import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.model.FlipperAppError

interface FlipperAppErrorHelper {
    suspend fun requestError(): FlipperAppError
}
