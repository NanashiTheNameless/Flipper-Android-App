package dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.protocolversion.model.FlipperSupportedState
import dev.namelessnanashi.flipperzero.core.data.SemVer
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val VERSION_WAITING_TIMEOUT = 30.toDuration(DurationUnit.SECONDS)

interface FVersionFeatureApi : FDeviceFeatureApi {
    fun getVersionInformationFlow(): StateFlow<SemVer?>

    fun getSupportedStateFlow(): StateFlow<FlipperSupportedState?>

    suspend fun isSupported(
        version: SemVer,
        timeout: Duration = VERSION_WAITING_TIMEOUT
    ): Boolean
}
