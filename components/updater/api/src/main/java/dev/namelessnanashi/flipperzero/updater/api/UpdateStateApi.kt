package dev.namelessnanashi.flipperzero.updater.api

import dev.namelessnanashi.flipperzero.updater.model.FlipperUpdateState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface UpdateStateApi {
    fun getFlipperUpdateState(
        scope: CoroutineScope
    ): Flow<FlipperUpdateState>
}
