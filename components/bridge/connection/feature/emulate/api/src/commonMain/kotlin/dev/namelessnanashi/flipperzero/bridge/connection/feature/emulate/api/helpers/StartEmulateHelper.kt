package dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.helpers

import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.model.EmulateConfig
import kotlinx.coroutines.CoroutineScope

interface StartEmulateHelper {
    @Suppress("LongParameterList")
    suspend fun onStart(
        scope: CoroutineScope,
        config: EmulateConfig,
        onStop: suspend () -> Unit,
        onResultTime: (Long) -> Unit
    ): Boolean
}
