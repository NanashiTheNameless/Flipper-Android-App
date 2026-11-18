package dev.namelessnanashi.flipperzero.updater.api

import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import kotlinx.coroutines.flow.Flow

interface FlipperVersionProviderApi {
    fun getCurrentFlipperVersion(): Flow<FirmwareVersion?>
}
