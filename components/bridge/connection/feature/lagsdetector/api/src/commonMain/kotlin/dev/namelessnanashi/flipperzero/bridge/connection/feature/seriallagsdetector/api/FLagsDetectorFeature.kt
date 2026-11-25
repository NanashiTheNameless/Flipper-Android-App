package dev.namelessnanashi.flipperzero.bridge.connection.feature.seriallagsdetector.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * This service is designed to detect a flipper hangup and restart the connection.
 *
 * If we wait for a response from the flipper but get nothing,
 * we restart the connection to the flipper.
 */
interface FLagsDetectorFeature : FDeviceFeatureApi {
    /**
     * @param request for debug purposes
     */
    suspend fun <T> wrapPendingAction(request: FlipperRequest?, block: suspend () -> T): T

    /**
     * @param request for debug purposes
     */
    fun <T> wrapPendingAction(request: FlipperRequest?, flow: Flow<T>): Flow<T>

    fun notifyAboutAction()

    /**
     * True while a request waits for its answer (file transfers, sync, installs). Fork: lets
     * the background keep-alive run only while work is in flight.
     */
    fun hasPendingRequests(): Flow<Boolean> = flowOf(false)
}
