package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** Starts loading when needed, while preserving an existing in-flight request. */
suspend fun FRpcInfoFeatureApi.awaitRpcInformation(
    timeoutMillis: Long = 60_000
): FlipperRpcInformation = withTimeoutOrNull(timeoutMillis) {
    coroutineScope {
        invalidate(this)
        getRpcInformationFlow()
            .filterIsInstance<FlipperInformationStatus.Ready<FlipperRpcInformation>>()
            .first().data
    }
} ?: error("Device information did not finish loading. Check the Flipper connection and try again.")
